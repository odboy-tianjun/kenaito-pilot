package cn.odboy.pipeline.core.impl;

import cn.odboy.pipeline.core.PipelineExecutionJob;
import cn.odboy.pipeline.core.PipelineScheduleService;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.dal.model.NodeDefinition;
import cn.odboy.pipeline.service.PipelineInstanceService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 流水线调度服务实现 - 集成Quartz实现立即顺序执行
 */
@Slf4j
@Service
public class PipelineScheduleServiceImpl implements PipelineScheduleService {

  @Autowired
  private Scheduler scheduler;

  @Autowired
  private PipelineInstanceService pipelineInstanceService;

  /**
   * 启动流水线 - 立即执行
   *
   * @param nodes       节点定义列表
   * @param inputParams 输入参数
   * @return 实例ID
   */
  @Override
  public String startPipeline(List<NodeDefinition> nodes, Map<String, Object> inputParams) {
    log.info("准备启动流水线，节点数量: {}", nodes.size());

    // 1. 创建流水线实例记录
    String instanceId = createPipelineInstance(nodes, inputParams);
    log.info("创建流水线实例成功，实例ID: {}", instanceId);

    // 2. 创建并触发Quartz Job
    try {
      triggerPipelineJob(instanceId, nodes, inputParams);
      log.info("流水线Job触发成功，实例ID: {}", instanceId);
    } catch (SchedulerException e) {
      log.error("触发流水线Job失败，实例ID: {}", instanceId, e);
      throw new RuntimeException("触发流水线执行失败", e);
    }

    return instanceId;
  }

  /**
   * 创建流水线实例记录
   *
   * @param nodes       节点定义列表
   * @param inputParams 输入参数
   * @return 实例ID
   */
  private String createPipelineInstance(List<NodeDefinition> nodes, Map<String, Object> inputParams) {
    PipelineInstanceTb instance = new PipelineInstanceTb();
    instance.setRuntimeParams(JSON.toJSONString(inputParams));
    instance.setNodeJson(JSON.toJSONString(nodes));
    // 初始化当前节点信息为空，避免数据库字段无默认值错误
    instance.setCurrentNodeCode("");
    instance.setCurrentNodeStatus("");
    instance.setCreateTime(new Date());
    instance.setUpdateTime(new Date());

    // 保存实例
    pipelineInstanceService.save(instance);

    return instance.getId();
  }

  /**
   * 触发流水线Job执行
   *
   * @param instanceId  实例ID
   * @param nodes       节点定义列表
   * @param inputParams 输入参数
   * @throws SchedulerException 调度异常
   */
  private void triggerPipelineJob(String instanceId, List<NodeDefinition> nodes, Map<String, Object> inputParams)
      throws SchedulerException {
    // 生成唯一的Job名称和组名
    String jobName = "PIPELINE_JOB_" + instanceId;
    String jobGroup = "PIPELINE_GROUP";
    String triggerName = "PIPELINE_TRIGGER_" + instanceId;

    // 构建JobDetail
    JobDetail jobDetail = JobBuilder.newJob(PipelineExecutionJob.class)
        .withIdentity(jobName, jobGroup)
        .withDescription("流水线执行Job: " + instanceId)
        .build();

    // 设置Job数据
    jobDetail.getJobDataMap().put(PipelineExecutionJob.JOB_KEY_INSTANCE_ID, instanceId);
    jobDetail.getJobDataMap().put(PipelineExecutionJob.JOB_KEY_NODES, JSON.toJSONString(nodes));
    jobDetail.getJobDataMap().put(PipelineExecutionJob.JOB_KEY_PARAMS, inputParams != null ? JSON.toJSONString(inputParams) : "");

    // 构建立即执行的Trigger
    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(triggerName, jobGroup)
        .withDescription("流水线触发器: " + instanceId)
        .startNow() // 立即执行
        .build();

    // 调度Job
    scheduler.scheduleJob(jobDetail, trigger);
    log.info("流水线Job已加入调度队列，实例ID: {}", instanceId);
  }
}
