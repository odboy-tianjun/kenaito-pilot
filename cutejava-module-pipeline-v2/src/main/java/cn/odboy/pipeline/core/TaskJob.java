package cn.odboy.pipeline.core;

import cn.hutool.core.util.StrUtil;
import cn.odboy.framework.context.KitSpringBeanHolder;
import cn.odboy.pipeline.constant.TaskStatusEnum;
import cn.odboy.pipeline.service.PipelineInstanceService;
import com.alibaba.fastjson2.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

@Slf4j
public class TaskJob implements InterruptableJob {

  private final PipelineInstanceService pipelineInstanceService;

  private final AtomicBoolean interrupted = new AtomicBoolean(false);

  public TaskJob() {
    pipelineInstanceService = KitSpringBeanHolder.getBean(PipelineInstanceService.class);
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    log.info("收到中断信号");
    interrupted.set(true);
  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    JobDataMap mergedJobDataMap = jobExecutionContext.getMergedJobDataMap();
    List<NodeDefinition> nodes = getMapValue(mergedJobDataMap, "nodes");
    TaskContext context = getMapValue(mergedJobDataMap, "context");
    log.info("开始执行操作前。nodes: {}, context: {}", JSON.toJSONString(nodes), JSON.toJSONString(context));

    // 设置操作
    List<TaskOperation> operations = new ArrayList<>();
    for (NodeDefinition nodeDefinition : nodes) {
      TaskOperation operation = KitSpringBeanHolder.getBean(nodeDefinition.getCode());
      operations.add(operation);
    }

    // 是否重试模式
    String retryBizCode = context.getRetryBizCode();
    if (StrUtil.isBlank(retryBizCode)) {
      // 初始化实例和节点
      pipelineInstanceService.initInstanceNode(nodes, operations, context);
    }

    List<TaskOperation> operationsToExecute = operations;

    if (StrUtil.isNotBlank(context.getRetryBizCode())) {
      log.info("检测到重试标识，从节点 {} 开始执行", context.getRetryBizCode());

      int startIndex = -1;
      for (int i = 0; i < nodes.size(); i++) {
        if (nodes.get(i).getCode().equals(context.getRetryBizCode())) {
          startIndex = i;
          break;
        }
      }

      if (startIndex == -1) {
        log.error("未找到重试节点: {}", context.getRetryBizCode());
        pipelineInstanceService.updateInstanceStatus(TaskStatusEnum.FAILURE, context);
        throw new JobExecutionException("未找到重试节点: " + context.getRetryBizCode());
      }

      operationsToExecute = operations.subList(startIndex, operations.size());
      log.info("将执行 {} 个节点，从索引 {} 开始", operationsToExecute.size(), startIndex);

      // 重置重试标识
      context.setRetryBizCode(null);
    }

    for (TaskOperation operation : operationsToExecute) {
      if (interrupted.get()) {
        log.warn("Job被中断，停止执行，taskId: {}", context.getTaskId());
        pipelineInstanceService.forceCloseInstance(context);
        return;
      }

      log.info("开始执行操作: {}", operation.name());
      pipelineInstanceService.updateInstanceStatus(TaskStatusEnum.RUNNING, context);

      String status = executeWithRetry(operation, context);

      if (TaskStatusEnum.FAILURE.getCode().equals(status)) {
        log.error("操作执行失败: {}", operation.name());
        pipelineInstanceService.updateInstanceStatus(TaskStatusEnum.FAILURE, context);
        return;
      }
    }

    pipelineInstanceService.updateInstanceStatus(TaskStatusEnum.SUCCESS, context);
    log.info("所有操作执行完成");
  }

  /**
   * 带重试机制的执行操作 在指定重试次数内尝试执行操作，直到成功或达到最大重试次数
   *
   * @param operation  任务操作
   * @param context    任务上下文
   * @return 执行状态
   */
  private String executeWithRetry(TaskOperation operation, TaskContext context) {
    int maxRetries = operation.retryTimes();

    for (int i = 0; i < maxRetries; i++) {
      if (interrupted.get()) {
        log.warn("操作执行被中断: {}", operation.name());
        pipelineInstanceService.forceCloseInstance(context);
        return TaskStatusEnum.FAILURE.getCode();
      }

      try {
        pipelineInstanceService.updateInstanceNodeStatus(TaskStatusEnum.RUNNING, operation, context, TaskStatusEnum.RUNNING.getMessage());
        String status = operation.execute(context);

        if (TaskStatusEnum.SUCCESS.getCode().equals(status)) {
          pipelineInstanceService.updateInstanceNodeStatus(TaskStatusEnum.SUCCESS, operation, context, TaskStatusEnum.SUCCESS.getMessage());
          return TaskStatusEnum.SUCCESS.getCode();
        }

        if (TaskStatusEnum.RUNNING.getCode().equals(status)) {
          return waitForCompletion(operation, context);
        }

        // 记录失败状态的日志
        log.warn("操作执行返回失败状态，操作名称：{}, 重试次数：{}/{}", operation.name(), i + 1, maxRetries);
        if (i == maxRetries - 1) {
          pipelineInstanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, TaskStatusEnum.FAILURE.getMessage());
          return TaskStatusEnum.FAILURE.getCode();
        }

      } catch (Exception e) {
        log.warn("操作执行异常，操作名称：{}, 重试次数：{}/{}", operation.name(), i + 1, maxRetries, e);
        if (i == maxRetries - 1) {
          pipelineInstanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, e.getMessage());
          return TaskStatusEnum.FAILURE.getCode();
        }
      }
    }

    pipelineInstanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, TaskStatusEnum.FAILURE.getMessage());
    return TaskStatusEnum.FAILURE.getCode();
  }

  /**
   * 等待异步操作完成 通过轮询方式持续检查操作状态，直到成功、失败或被中断
   *
   * @param operation 任务操作
   * @param context   任务上下文
   * @return 最终执行状态
   */
  private String waitForCompletion(TaskOperation operation, TaskContext context) {
    while (true) {
      if (interrupted.get()) {
        log.warn("等待异步操作被中断: {}", operation.name());
        pipelineInstanceService.forceCloseInstance(context);
        return TaskStatusEnum.FAILURE.getCode();
      }

      try {
        String status = operation.describe(context);

        if (TaskStatusEnum.SUCCESS.getCode().equals(status)) {
          pipelineInstanceService.updateInstanceNodeStatus(TaskStatusEnum.SUCCESS, operation, context, TaskStatusEnum.SUCCESS.getMessage());
          return TaskStatusEnum.SUCCESS.getCode();
        }

        if (TaskStatusEnum.FAILURE.getCode().equals(status)) {
          pipelineInstanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, TaskStatusEnum.FAILURE.getMessage());
          return TaskStatusEnum.FAILURE.getCode();
        }

        Thread.sleep(operation.pollInterval());

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("等待异步操作被中断，操作名称：{}", operation.name());
        pipelineInstanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, TaskStatusEnum.FAILURE.getMessage());
        return TaskStatusEnum.FAILURE.getCode();
      } catch (Exception e) {
        log.warn("查询状态异常，操作名称：{}", operation.name(), e);
        pipelineInstanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, TaskStatusEnum.FAILURE.getMessage());
        return TaskStatusEnum.FAILURE.getCode();
      }
    }
  }

  @SuppressWarnings("all")
  public <T> T getMapValue(JobDataMap mergedJobDataMap, String key) {
    Object o = mergedJobDataMap.get(key);
    return (T) o;
  }
}
