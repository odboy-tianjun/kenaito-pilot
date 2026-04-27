package cn.odboy.pipeline.core;

import cn.hutool.core.collection.CollUtil;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.dal.model.NodeDefinition;
import cn.odboy.pipeline.service.PipelineInstanceService;
import cn.odboy.pipeline.util.PipelineNameUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务执行引擎 负责按顺序执行任务操作列表，支持重试和异步等待机制
 */
@Slf4j
@Component
public class TaskEngine {

  @Autowired
  private Scheduler scheduler;
  @Autowired
  private PipelineInstanceService pipelineInstanceService;

  /**
   * 执行任务操作列表 按顺序执行所有操作，如果某个操作失败则立即终止
   *
   * @param nodes   节点定义
   * @param context 任务上下文
   * @return 任务执行结果
   */
  public TaskResult execute(List<NodeDefinition> nodes, TaskContext context) {
    if (CollUtil.isEmpty(nodes)) {
      log.error("任务操作列表为空");
      return TaskResult.fail("TaskEngine", "任务操作列表不能为空");
    }

    if (context == null) {
      log.error("任务上下文为空");
      return TaskResult.fail("TaskEngine", "任务上下文不能为空");
    }

    // 检查是否有同类型且执行中的
    pipelineInstanceService.checkExist(context);

    // 任务调度
    try {
      triggerJob(nodes, context);
      log.info("流水线Job触发成功，实例ID: {}", context.getTaskId());
      return TaskResult.success("任务执行成功");
    } catch (SchedulerException e) {
      log.error("触发流水线Job失败，实例ID: {}", context.getTaskId(), e);
      throw new RuntimeException("触发流水线执行失败", e);
    }
  }

  public TaskResult retry(String instanceId, String retryBizCode) {
    PipelineInstanceTb pipelineInstance = pipelineInstanceService.getOne(new LambdaQueryWrapper<PipelineInstanceTb>()
        .eq(PipelineInstanceTb::getId, instanceId)
        .orderByDesc(PipelineInstanceTb::getUpdateTime)
        .last("LIMIT 1")
    );
    if (pipelineInstance == null) {
      throw new BadRequestException("流水线实例不存在");
    }

    List<NodeDefinition> nodes = JSON.parseArray(pipelineInstance.getNodeJson(), NodeDefinition.class);
    TaskContext context = JSON.parseObject(pipelineInstance.getContextJson(), TaskContext.class);

    // 任务调度
    try {
      retryJob(nodes, context, retryBizCode);
      log.info("流水线Job重试成功，实例ID: {}", context.getTaskId());
      return TaskResult.success("流水线节点 " + retryBizCode + " 重试成功");
    } catch (SchedulerException e) {
      log.error("重试流水线Job失败，实例ID: {}, retryBizCode: {}", context.getTaskId(), retryBizCode, e);
      throw new RuntimeException("流水线节点 " + retryBizCode + " 重试失败", e);
    }
  }


  /**
   * 触发Job执行
   *
   * @param nodes   节点定义
   * @param context 任务上下文
   */
  private void triggerJob(List<NodeDefinition> nodes, TaskContext context) throws SchedulerException {
    // 生成唯一的Job名称和组名
    String jobGroup = PipelineNameUtil.getJobGroup();
    String jobName = PipelineNameUtil.getJobName(context);

    // 构建JobDetail
    JobDetail jobDetail = JobBuilder.newJob(TaskJob.class)
        .withIdentity(jobName, jobGroup)
        .withDescription("流水线执行Job: " + context.getTaskId())
        .build();

    // 设置Job数据
    jobDetail.getJobDataMap().put("nodes", nodes);
    jobDetail.getJobDataMap().put("context", context);

    // 构建立即执行的Trigger
    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(jobName, jobGroup)
        .withDescription("流水线触发器: " + context.getTaskId())
        .startNow() // 立即执行
        .build();

    // 调度Job
    scheduler.scheduleJob(jobDetail, trigger);
    log.info("流水线Job已加入调度队列，实例ID: {}", context.getTaskId());
  }

  /**
   * Job节点重试
   *
   * @param nodes        节点定义
   * @param context      任务上下文
   * @param retryBizCode 重试节点编码
   */
  private void retryJob(List<NodeDefinition> nodes, TaskContext context, String retryBizCode) throws SchedulerException {
    stopJob(context);
    context.setRetryBizCode(retryBizCode);
    triggerJob(nodes, context);
  }

  /**
   * 停止Job
   *
   * @param context 任务上下文
   */
  public void stopJob(TaskContext context) {
    try {
      String jobGroup = PipelineNameUtil.getJobGroup();
      String jobName = PipelineNameUtil.getJobName(context);

      JobKey jobKey = new JobKey(jobName, jobGroup);

      boolean interrupted = scheduler.interrupt(jobKey);

      if (interrupted) {
        log.info("成功发送中断信号给Job，taskId: {}", context.getTaskId());
      } else {
        log.warn("Job不支持中断或不存在，尝试删除Job，taskId: {}", context.getTaskId());
      }

      boolean deleted = scheduler.deleteJob(jobKey);

      if (deleted) {
        log.info("成功停止Job，taskId: {}", context.getTaskId());
      } else {
        log.warn("Job不存在或已执行完成，taskId: {}", context.getTaskId());
      }
    } catch (UnableToInterruptJobException e) {
      log.error("中断Job失败，taskId: {}", context.getTaskId(), e);
      throw new RuntimeException("中断任务失败", e);
    } catch (SchedulerException e) {
      log.error("停止Job失败，taskId: {}", context.getTaskId(), e);
      throw new RuntimeException("停止任务失败", e);
    }
  }
}
