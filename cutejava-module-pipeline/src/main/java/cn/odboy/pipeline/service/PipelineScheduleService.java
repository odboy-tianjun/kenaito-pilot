package cn.odboy.pipeline.service;

import cn.odboy.pipeline.constant.PipelineStatusEnum;
import cn.odboy.pipeline.core.PipelineContext;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.dal.model.NodeDefinition;
import cn.odboy.pipeline.dal.mysql.PipelineInstanceMapper;
import cn.odboy.pipeline.dal.mysql.PipelineInstanceNodeMapper;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.KeyMatcher;
import org.quartz.listeners.JobChainingJobListener;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineScheduleService {

  private final Scheduler scheduler;
  private final ApplicationContext ctx;

  @Resource
  private PipelineInstanceMapper instanceMapper;
  @Resource
  private PipelineInstanceNodeMapper nodeMapper;

  // ====================== 启动流水线 ======================
  public String startPipeline(List<NodeDefinition> nodes, Map<String, Object> inputParams) throws SchedulerException {
    PipelineInstanceTb instance = new PipelineInstanceTb();
    instance.setStatus(PipelineStatusEnum.PENDING.getCode());
    instance.setRuntimeParams(JSON.toJSONString(inputParams));
    instance.setCurrentNodeCode(nodes.getFirst().getCode());
    instance.setCurrentNodeStatus(PipelineStatusEnum.PENDING.getCode());
    instance.setNodeJson(JSON.toJSONString(nodes));
    instance.setCreateTime(new Date());
    instance.setUpdateTime(new Date());
    instanceMapper.insert(instance);

    String instanceId = instance.getId();
    for (NodeDefinition n : nodes) {
      PipelineInstanceNodeTb node = new PipelineInstanceNodeTb();
      node.setInstanceId(instanceId);
      node.setBizCode(n.getCode());
      node.setBizName(n.getName());
      node.setExecuteStatus(PipelineStatusEnum.PENDING.getCode());
      node.setCreateTime(new Date());
      node.setUpdateTime(new Date());
      nodeMapper.insert(node);
    }

    PipelineContext context = new PipelineContext();
    context.setTaskStatus(PipelineStatusEnum.PENDING.getCode());
    context.getParams().putAll(inputParams);
    nodes.forEach(d -> {
      if (d.getParameters() != null)
        context.getParams().putAll(d.getParameters());
    });

    instance.setContextJson(JSON.toJSONString(context));
    instanceMapper.updateById(instance);

    for (int i = 0; i < nodes.size(); i++) {
      NodeDefinition node = nodes.get(i);
      String code = node.getCode();

      JobDataMap dataMap = new JobDataMap();
      dataMap.put("instanceId", instanceId);
      dataMap.put("context", context);

      Class<? extends Job> jobClass = this.getJobClass(code);
      JobDetail job = JobBuilder.newJob(jobClass)
          .withIdentity(code, instanceId)
          .usingJobData(dataMap)
          .build();

      Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(code, instanceId)
          .startNow()
          .build();

      scheduler.scheduleJob(job, trigger);

      if (i > 0) {
        JobKey prev = JobKey.jobKey(nodes.get(i - 1).getCode(), instanceId);
        JobChainingJobListener chain = new JobChainingJobListener("chain_" + instanceId);
        chain.addJobChainLink(prev, job.getKey());
        scheduler.getListenerManager().addJobListener(chain, KeyMatcher.keyEquals(prev));
      }
    }

    instance.setStatus(PipelineStatusEnum.RUNNING.getCode());
    instance.setUpdateTime(new Date());
    instanceMapper.updateById(instance);
    return instanceId;
  }

  // ====================== 宕机恢复 ======================
  public void recoverPipeline(String instanceId) throws SchedulerException {
    PipelineInstanceTb instance = instanceMapper.selectById(instanceId);
    List<NodeDefinition> allNodes = JSON.parseArray(instance.getNodeJson(), NodeDefinition.class);
    PipelineContext context = JSON.parseObject(instance.getContextJson(), PipelineContext.class);

    List<PipelineInstanceNodeTb> nodes = nodeMapper.selectByInstanceId(instanceId);
    NodeDefinition breakNode = null;

    for (PipelineInstanceNodeTb n : nodes) {
      if (PipelineStatusEnum.PENDING.getCode().equals(n.getExecuteStatus()) ||
          PipelineStatusEnum.RUNNING.getCode().equals(n.getExecuteStatus())) {
        breakNode = allNodes.stream().filter(d -> d.getCode().equals(n.getBizCode())).findFirst().orElse(null);
        break;
      }
    }

    if (breakNode == null) {
      instance.setStatus(PipelineStatusEnum.SUCCESS.getCode());
      instanceMapper.updateById(instance);
      return;
    }

    int idx = allNodes.indexOf(breakNode);
    List<NodeDefinition> recoverList = allNodes.subList(idx, allNodes.size());

    for (int i = 0; i < recoverList.size(); i++) {
      NodeDefinition node = recoverList.get(i);
      String code = node.getCode();
      JobKey key = JobKey.jobKey(code, instanceId);
      if (scheduler.checkExists(key))
        continue;

      JobDataMap dataMap = new JobDataMap();
      dataMap.put("instanceId", instanceId);
      dataMap.put("context", context);

      Class<? extends Job> jobClass = this.getJobClass(code);
      JobDetail job = JobBuilder.newJob(jobClass)
          .withIdentity(key)
          .usingJobData(dataMap)
          .build();

      Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(code, instanceId)
          .startNow()
          .build();

      scheduler.scheduleJob(job, trigger);

      if (i > 0) {
        JobKey prev = JobKey.jobKey(recoverList.get(i - 1).getCode(), instanceId);
        JobChainingJobListener chain = new JobChainingJobListener("chain_" + instanceId);
        chain.addJobChainLink(prev, job.getKey());
        scheduler.getListenerManager().addJobListener(chain, KeyMatcher.keyEquals(prev));
      }
    }

    instance.setStatus(PipelineStatusEnum.RUNNING.getCode());
    instanceMapper.updateById(instance);
  }

  // ====================== 停止节点 ======================
  public void stopNode(String instanceId, String nodeCode) throws SchedulerException {
    JobKey key = JobKey.jobKey(nodeCode, instanceId);
    if (scheduler.checkExists(key))
      scheduler.interrupt(key);
    scheduler.unscheduleJob(TriggerKey.triggerKey(nodeCode, instanceId));
    scheduler.deleteJob(key);
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Job> getJobClass(String beanName) {
    return (Class<? extends Job>) ctx.getBean(beanName).getClass();
  }
}
