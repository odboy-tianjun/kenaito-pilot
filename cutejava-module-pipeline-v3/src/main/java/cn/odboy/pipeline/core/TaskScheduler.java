package cn.odboy.pipeline.core;

import cn.odboy.pipeline.util.PipelineNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 任务调度器 职责： 1. 封装 Quartz 调度逻辑 2. 管理 Job 的触发、停止、删除 3. 生成唯一的 Job 标识
 */
@Slf4j
@Component
public class TaskScheduler {

  @Autowired
  private Scheduler scheduler;

  /**
   * 触发 Job 执行
   */
  public void triggerJob(List<NodeDefinition> nodes, TaskContext context) throws SchedulerException {
    String jobGroup = PipelineNameUtil.getJobGroup();
    String jobName = PipelineNameUtil.getJobName(context);

    // 构建 JobDetail
    JobDetail jobDetail = JobBuilder.newJob(TaskJob.class)
        .withIdentity(jobName, jobGroup)
        .withDescription("流水线执行Job: " + context.getTaskId())
        .build();

    // 设置 Job 数据
    jobDetail.getJobDataMap().put("nodes", nodes);
    jobDetail.getJobDataMap().put("context", context);

    // 构建立即执行的 Trigger
    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(jobName, jobGroup)
        .withDescription("流水线触发器: " + context.getTaskId())
        .startNow()
        .build();

    // 调度 Job
    scheduler.scheduleJob(jobDetail, trigger);
    log.info("流水线Job已加入调度队列，实例ID: {}", context.getTaskId());
  }

  /**
   * 停止 Job
   */
  public void stopJob(TaskContext context) {
    try {
      String jobGroup = PipelineNameUtil.getJobGroup();
      String jobName = PipelineNameUtil.getJobName(context);
      JobKey jobKey = new JobKey(jobName, jobGroup);

      // 尝试中断
      boolean interrupted = scheduler.interrupt(jobKey);
      if (interrupted) {
        log.info("成功发送中断信号给Job，taskId: {}", context.getTaskId());
      } else {
        log.warn("Job不支持中断或不存在，尝试删除Job，taskId: {}", context.getTaskId());
      }

      // 删除 Job
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
