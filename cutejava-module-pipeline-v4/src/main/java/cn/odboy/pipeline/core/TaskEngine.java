package cn.odboy.pipeline.core;

import cn.odboy.pipeline.constant.TaskStatusEnum;
import cn.odboy.pipeline.service.PipelineInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 任务执行引擎 职责： 1. 顺序执行操作链 2. 处理重试逻辑 3. 处理异步轮询
 *
 * 注意：本类不包含调度、持久化等业务逻辑，专注于执行
 */
@Slf4j
@Component
public class TaskEngine {

  @Autowired
  private PipelineInstanceService instanceService;

  /**
   * 执行任务操作列表 按顺序执行所有操作，如果某个操作失败则立即终止
   *
   * @param operations 操作链
   * @param context    任务上下文
   * @return 任务执行结果
   */
  public TaskResult execute(List<TaskOperation> operations, TaskContext context) {
    if (operations == null || operations.isEmpty()) {
      log.error("任务操作列表为空");
      return TaskResult.fail("UNKNOWN", "任务操作列表不能为空");
    }

    if (context == null) {
      log.error("任务上下文为空");
      return TaskResult.fail("UNKNOWN", "任务上下文不能为空");
    }

    log.info("开始执行任务链，共 {} 个节点，taskId: {}", operations.size(), context.getTaskId());

    for (TaskOperation operation : operations) {
      // 检查中断信号
      if (isInterrupted(context)) {
        log.warn("任务被中断，停止执行，taskId: {}", context.getTaskId());
        instanceService.forceCloseInstance(context);
        return TaskResult.fail(operation.name(), "任务被中断");
      }

      log.info("开始执行操作: {}", operation.name());
      instanceService.updateInstanceStatus(TaskStatusEnum.RUNNING, context);

      // 带重试的执行
      String status = executeWithRetry(operation, context);

      if (TaskStatusEnum.FAILURE.getCode().equals(status)) {
        log.error("操作执行失败: {}", operation.name());
        instanceService.updateInstanceStatus(TaskStatusEnum.FAILURE, context);
        return TaskResult.fail(operation.name(), "操作执行失败");
      }
    }

    instanceService.updateInstanceStatus(TaskStatusEnum.SUCCESS, context);
    log.info("所有操作执行完成，taskId: {}", context.getTaskId());
    return TaskResult.success("任务执行成功");
  }

  /**
   * 带重试机制的执行操作
   */
  private String executeWithRetry(TaskOperation operation, TaskContext context) {
    int maxRetries = operation.retryTimes();

    for (int i = 0; i < maxRetries; i++) {
      if (isInterrupted(context)) {
        log.warn("操作执行被中断: {}", operation.name());
        instanceService.forceCloseInstance(context);
        return TaskStatusEnum.FAILURE.getCode();
      }

      try {
        instanceService.updateInstanceNodeStatus(TaskStatusEnum.RUNNING, operation, context, "执行中");
        String status = operation.execute(context);

        if (TaskStatusEnum.SUCCESS.getCode().equals(status)) {
          instanceService.updateInstanceNodeStatus(TaskStatusEnum.SUCCESS, operation, context, "执行成功");
          return TaskStatusEnum.SUCCESS.getCode();
        }

        if (TaskStatusEnum.RUNNING.getCode().equals(status)) {
          return waitForCompletion(operation, context);
        }

        log.warn(
            "操作执行返回失败状态，操作名称：{}, 重试次数：{}/{}",
            operation.name(), i + 1, maxRetries
        );

        if (i == maxRetries - 1) {
          instanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, "达到最大重试次数");
          return TaskStatusEnum.FAILURE.getCode();
        }

      } catch (Exception e) {
        log.warn(
            "操作执行异常，操作名称：{}, 重试次数：{}/{}",
            operation.name(), i + 1, maxRetries, e
        );
        if (i == maxRetries - 1) {
          instanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, e.getMessage());
          return TaskStatusEnum.FAILURE.getCode();
        }
      }
    }

    instanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, "未知错误");
    return TaskStatusEnum.FAILURE.getCode();
  }

  /**
   * 等待异步操作完成
   */
  private String waitForCompletion(TaskOperation operation, TaskContext context) {
    while (true) {
      if (isInterrupted(context)) {
        log.warn("等待异步操作被中断: {}", operation.name());
        instanceService.forceCloseInstance(context);
        return TaskStatusEnum.FAILURE.getCode();
      }

      try {
        String status = operation.describe(context);

        if (TaskStatusEnum.SUCCESS.getCode().equals(status)) {
          instanceService.updateInstanceNodeStatus(TaskStatusEnum.SUCCESS, operation, context, TaskStatusEnum.SUCCESS.getMessage());
          return TaskStatusEnum.SUCCESS.getCode();
        }

        if (TaskStatusEnum.FAILURE.getCode().equals(status)) {
          instanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, TaskStatusEnum.FAILURE.getMessage());
          return TaskStatusEnum.FAILURE.getCode();
        }

        Thread.sleep(operation.pollInterval());

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("等待异步操作被中断，操作名称：{}", operation.name());
        instanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, "任务被关闭");
        return TaskStatusEnum.FAILURE.getCode();
      } catch (Exception e) {
        log.warn("查询状态异常，操作名称：{}", operation.name(), e);
        instanceService.updateInstanceNodeStatus(TaskStatusEnum.FAILURE, operation, context, e.getMessage());
        return TaskStatusEnum.FAILURE.getCode();
      }
    }
  }

  /**
   * 检查是否被中断
   */
  private boolean isInterrupted(TaskContext context) {
    // 检查上下文中的中断标志
    return context.getInterrupted() != null && context.getInterrupted().get();
  }
}
