package cn.odboy.pipeline.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.odboy.framework.context.KitSpringBeanHolder;
import cn.odboy.pipeline.constant.PipelineStatusEnum;
import cn.odboy.pipeline.dal.model.NodeDefinition;
import cn.odboy.pipeline.service.PipelineInstanceService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务执行引擎 负责按顺序执行任务操作列表，支持重试和异步等待机制
 */
@Slf4j
public class TaskEngine {

  /**
   * 执行任务操作列表 按顺序执行所有操作，如果某个操作失败则立即终止
   *
   * @param nodes      节点定义
   * @param operations 任务操作列表
   * @param context    任务上下文
   * @return 任务执行结果
   */
  public TaskResult execute(List<NodeDefinition> nodes, List<TaskOperation> operations, TaskContext context) {
    if (CollUtil.isEmpty(operations)) {
      log.error("任务操作列表为空");
      return TaskResult.fail("TaskEngine", "任务操作列表不能为空");
    }

    if (context == null) {
      log.error("任务上下文为空");
      return TaskResult.fail("TaskEngine", "任务上下文不能为空");
    }

    PipelineInstanceService pipelineInstanceService = KitSpringBeanHolder.getBean(PipelineInstanceService.class);

    // 检查是否有同类型且执行中的
    pipelineInstanceService.checkExist(context);

    // 初始化实例和节点
    pipelineInstanceService.instanceInitWithNodeList(nodes, operations, context);

    for (TaskOperation operation : operations) {
      log.info("开始执行操作: {}", operation.name());
      pipelineInstanceService.updateInstanceStatus(PipelineStatusEnum.RUNNING, context);

      String status = executeWithRetry(operation, context);

      if (TaskStatusEnum.FAILURE.getCode().equals(status)) {
        log.error("操作执行失败: {}", operation.name());
        pipelineInstanceService.updateInstanceStatus(PipelineStatusEnum.FAILURE, context);
        return TaskResult.fail(operation.name(), "操作执行失败");
      }
    }

    pipelineInstanceService.updateInstanceStatus(PipelineStatusEnum.SUCCESS, context);
    log.info("所有操作执行完成");

    return TaskResult.success("任务执行成功");
  }

  /**
   * 带重试机制的执行操作 在指定重试次数内尝试执行操作，直到成功或达到最大重试次数
   *
   * @param operation 任务操作
   * @param context   任务上下文
   * @return 执行状态
   */
  private String executeWithRetry(TaskOperation operation, TaskContext context) {
    PipelineInstanceService pipelineInstanceService = KitSpringBeanHolder.getBean(PipelineInstanceService.class);

    int maxRetries = operation.retryTimes();

    for (int i = 0; i < maxRetries; i++) {
      try {
        pipelineInstanceService.updateInstanceNodeStatus(PipelineStatusEnum.RUNNING, operation, context, PipelineStatusEnum.RUNNING.getDesc());
        String status = operation.execute(context);

        if (TaskStatusEnum.SUCCESS.getCode().equals(status)) {
          pipelineInstanceService.updateInstanceNodeStatus(PipelineStatusEnum.SUCCESS, operation, context, PipelineStatusEnum.SUCCESS.getDesc());
          return TaskStatusEnum.SUCCESS.getCode();
        }

        if (TaskStatusEnum.RUNNING.getCode().equals(status)) {
          return waitForCompletion(operation, context);
        }

        // 记录失败状态的日志
        log.warn("操作执行返回失败状态，操作名称：{}, 重试次数：{}/{}", operation.name(), i + 1, maxRetries);
        if (i == maxRetries - 1) {
          pipelineInstanceService.updateInstanceNodeStatus(PipelineStatusEnum.FAILURE, operation, context, PipelineStatusEnum.FAILURE.getDesc());
          return TaskStatusEnum.FAILURE.getCode();
        }

      } catch (Exception e) {
        log.warn("操作执行异常，操作名称：{}, 重试次数：{}/{}", operation.name(), i + 1, maxRetries, e);
        if (i == maxRetries - 1) {
          pipelineInstanceService.updateInstanceNodeStatus(PipelineStatusEnum.FAILURE, operation, context, e.getMessage());
          return TaskStatusEnum.FAILURE.getCode();
        }
      }
    }

    pipelineInstanceService.updateInstanceNodeStatus(PipelineStatusEnum.FAILURE, operation, context, PipelineStatusEnum.FAILURE.getDesc());
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
    PipelineInstanceService pipelineInstanceService = KitSpringBeanHolder.getBean(PipelineInstanceService.class);

    while (true) {
      try {
        String status = operation.describe(context);

        if (TaskStatusEnum.SUCCESS.getCode().equals(status)) {
          pipelineInstanceService.updateInstanceNodeStatus(PipelineStatusEnum.SUCCESS, operation, context, PipelineStatusEnum.SUCCESS.getDesc());
          return TaskStatusEnum.SUCCESS.getCode();
        }

        if (TaskStatusEnum.FAILURE.getCode().equals(status)) {
          pipelineInstanceService.updateInstanceNodeStatus(PipelineStatusEnum.FAILURE, operation, context, PipelineStatusEnum.FAILURE.getDesc());
          return TaskStatusEnum.FAILURE.getCode();
        }

        Thread.sleep(operation.pollInterval());

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("等待异步操作被中断，操作名称：{}", operation.name());
        pipelineInstanceService.updateInstanceNodeStatus(PipelineStatusEnum.FAILURE, operation, context, PipelineStatusEnum.FAILURE.getDesc());
        return TaskStatusEnum.FAILURE.getCode();
      } catch (Exception e) {
        log.warn("查询状态异常，操作名称：{}", operation.name(), e);
        pipelineInstanceService.updateInstanceNodeStatus(PipelineStatusEnum.FAILURE, operation, context, PipelineStatusEnum.FAILURE.getDesc());
        return TaskStatusEnum.FAILURE.getCode();
      }
    }
  }
}
