package cn.odboy.pipeline;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.KeyMatcher;
import org.quartz.listeners.JobChainingJobListener;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 流水线调度服务
 * 负责流水线的启动、恢复、重试等调度逻辑
 */
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
  @Resource
  private PipelineInstanceNodeService nodeService;

  /**
   * 启动流水线
   *
   * @param nodes 节点定义列表
   * @param inputParams 输入参数
   * @return 实例ID
   * @throws SchedulerException 调度异常
   */
  public String startPipeline(List<NodeDefinition> nodes, Map<String, Object> inputParams) throws SchedulerException {
    // 创建流水线实例
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

    // 创建节点实例记录
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

    // 创建流水线上下文
    PipelineContext context = new PipelineContext();
    context.setTaskStatus(PipelineStatusEnum.PENDING.getCode());
    context.getParams().putAll(inputParams);
    nodes.forEach(d -> {
      if (d.getParameters() != null)
        context.getParams().putAll(d.getParameters());
    });

    instance.setContextJson(JSON.toJSONString(context));
    instanceMapper.updateById(instance);

    // ======================
    // 【核心修复】创建链式监听器
    // ======================
    String chainListenerName = "CHAIN_" + instanceId;
    JobChainingJobListener jobChain = new JobChainingJobListener(chainListenerName);
    scheduler.getListenerManager().addJobListener(jobChain);

    // 注册所有Job和Trigger
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

      // 【修复】只有第一个节点立即执行，后续节点通过链式触发
      Trigger trigger;
      if (i == 0) {
        trigger = TriggerBuilder.newTrigger()
            .withIdentity(code, instanceId)
            .startNow()
            .build();
      } else {
        // 后续节点不设置触发时间，完全依赖链式触发
        trigger = TriggerBuilder.newTrigger()
            .withIdentity(code, instanceId)
            .build();
      }

      scheduler.scheduleJob(job, trigger);

      // 【修复】建立链式关系：前一个节点执行完后触发当前节点
      if (i > 0) {
        JobKey prevKey = JobKey.jobKey(nodes.get(i - 1).getCode(), instanceId);
        JobKey currentKey = JobKey.jobKey(code, instanceId);
        jobChain.addJobChainLink(prevKey, currentKey);
      }
    }

    // 更新主表状态为运行中
    instance.setStatus(PipelineStatusEnum.RUNNING.getCode());
    instanceMapper.updateById(instance);

    log.info("✅ 流水线启动成功：{}, 节点数：{}", instanceId, nodes.size());
    return instanceId;
  }

  /**
   * 恢复未完成的流水线
   *
   * @param instanceId 实例ID
   * @throws SchedulerException 调度异常
   */
  public void recoverPipeline(String instanceId) throws SchedulerException {
    PipelineInstanceTb instance = instanceMapper.selectById(instanceId);
    List<NodeDefinition> allNodes = JSON.parseArray(instance.getNodeJson(), NodeDefinition.class);
    PipelineContext context = JSON.parseObject(instance.getContextJson(), PipelineContext.class);

    List<PipelineInstanceNodeTb> nodes = nodeMapper.selectByInstanceId(instanceId);
    NodeDefinition breakNode = null;

    // 查找第一个未完成（PENDING或RUNNING）的节点
    for (PipelineInstanceNodeTb n : nodes) {
      if (PipelineStatusEnum.PENDING.getCode().equals(n.getExecuteStatus()) ||
          PipelineStatusEnum.RUNNING.getCode().equals(n.getExecuteStatus())) {
        breakNode = allNodes.stream().filter(d -> d.getCode().equals(n.getBizCode())).findFirst().orElse(null);
        break;
      }
    }

    // 如果没有未完成的节点，说明全部执行成功
    if (breakNode == null) {
      log.info("✅ 流水线已全部完成，无需恢复：{}", instanceId);
      instance.setStatus(PipelineStatusEnum.SUCCESS.getCode());
      instanceMapper.updateById(instance);
      return;
    }

    log.info("🔄 开始恢复流水线：{}, 断点节点：{}", instanceId, breakNode.getCode());

    int idx = allNodes.indexOf(breakNode);
    List<NodeDefinition> recoverList = allNodes.subList(idx, allNodes.size());

    // 创建链式监听器
    String chainListenerName = "CHAIN_RECOVER_" + instanceId;
    JobChainingJobListener jobChain = new JobChainingJobListener(chainListenerName);
    scheduler.getListenerManager().addJobListener(jobChain);

    // 重新调度未完成的节点
    for (int i = 0; i < recoverList.size(); i++) {
      NodeDefinition node = recoverList.get(i);
      String code = node.getCode();
      JobKey key = JobKey.jobKey(code, instanceId);

      // 如果Job已存在，跳过
      if (scheduler.checkExists(key)) {
        log.warn("⚠️ Job已存在，跳过：{}", code);
        continue;
      }

      JobDataMap dataMap = new JobDataMap();
      dataMap.put("instanceId", instanceId);
      dataMap.put("context", context);

      Class<? extends Job> jobClass = this.getJobClass(code);
      JobDetail job = JobBuilder.newJob(jobClass)
          .withIdentity(key)
          .usingJobData(dataMap)
          .build();

      // 【修复】只有第一个恢复节点立即执行
      Trigger trigger;
      if (i == 0) {
        trigger = TriggerBuilder.newTrigger()
            .withIdentity(code, instanceId)
            .startNow()
            .build();
      } else {
        trigger = TriggerBuilder.newTrigger()
            .withIdentity(code, instanceId)
            .build();
      }

      scheduler.scheduleJob(job, trigger);

      // 建立链式关系
      if (i > 0) {
        JobKey prev = JobKey.jobKey(recoverList.get(i - 1).getCode(), instanceId);
        jobChain.addJobChainLink(prev, key);
      }
    }

    // 更新主表状态为运行中
    instance.setStatus(PipelineStatusEnum.RUNNING.getCode());
    instance.setUpdateTime(new Date());
    instanceMapper.updateById(instance);

    log.info("✅ 流水线恢复成功：{}", instanceId);
  }

  /**
   * 手动重试失败的节点
   *
   * @param instanceId 实例ID
   * @param nodeCode 节点编码
   * @throws SchedulerException 调度异常
   */
  public void manualRetryNode(String instanceId, String nodeCode) throws SchedulerException {
    PipelineInstanceTb instance = instanceMapper.selectById(instanceId);
    List<NodeDefinition> nodeDefList = JSON.parseArray(instance.getNodeJson(), NodeDefinition.class);

    NodeDefinition nodeDef = nodeDefList.stream().filter(d -> d.getCode().equals(nodeCode)).findFirst().orElse(null);
    if (nodeDef == null || !nodeDef.isRetry()) {
      throw new RuntimeException("该节点不允许手动重试");
    }

    List<PipelineInstanceNodeTb> nodeList = nodeMapper.selectByInstanceId(instanceId);
    PipelineInstanceNodeTb targetNode = nodeList.stream().filter(p -> p.getBizCode().equals(nodeCode)).findFirst().orElse(null);
    if (targetNode == null || !PipelineStatusEnum.FAILURE.getCode().equals(targetNode.getExecuteStatus())) {
      throw new RuntimeException("只能重试【FAILURE】状态的节点");
    }

    // 重置节点状态为PENDING
    nodeService.updateStatusPending(instanceId, nodeCode);

    // 删除旧的Job和Trigger
    JobKey jobKey = JobKey.jobKey(nodeCode, instanceId);
    TriggerKey triggerKey = TriggerKey.triggerKey(nodeCode, instanceId);
    if (scheduler.checkExists(jobKey)) {
      scheduler.unscheduleJob(triggerKey);
      scheduler.deleteJob(jobKey);
    }

    // 重新创建Job
    PipelineContext context = JSON.parseObject(instance.getContextJson(), PipelineContext.class);
    JobDataMap dataMap = new JobDataMap();
    dataMap.put("instanceId", instanceId);
    dataMap.put("context", context);

    JobDetail job = JobBuilder.newJob(getJobClass(nodeCode))
        .withIdentity(jobKey)
        .usingJobData(dataMap)
        .build();

    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(triggerKey)
        .startNow()
        .build();

    scheduler.scheduleJob(job, trigger);

    // 更新主表状态为运行中
    instance.setStatus(PipelineStatusEnum.RUNNING.getCode());
    instance.setUpdateTime(new Date());
    instanceMapper.updateById(instance);

    log.info("✅ 节点重试成功：{}, 节点：{}", instanceId, nodeCode);
  }

  /**
   * 停止指定节点
   *
   * @param instanceId 实例ID
   * @param nodeCode 节点编码
   * @throws SchedulerException 调度异常
   */
  public void stopNode(String instanceId, String nodeCode) throws SchedulerException {
    JobKey key = JobKey.jobKey(nodeCode, instanceId);
    if (scheduler.checkExists(key)) {
      scheduler.interrupt(key);
    }

    scheduler.unscheduleJob(TriggerKey.triggerKey(nodeCode, instanceId));
    scheduler.deleteJob(key);

    log.info("✅ 节点已停止：{}, 节点：{}", instanceId, nodeCode);
  }

  /**
   * 根据Bean名称获取Job类
   *
   * @param beanName Bean名称
   * @return Job类
   */
  @SuppressWarnings("unchecked")
  private Class<? extends Job> getJobClass(String beanName) {
    return (Class<? extends Job>) ctx.getBean(beanName).getClass();
  }
}
