package cn.odboy.pipeline.v4.core;

import cn.odboy.framework.context.KitSpringBeanHolder;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Quartz Job 适配器
 * 职责：
 * 1. 接收 Quartz 调度触发
 * 2. 解析 Job 数据
 * 3. 构建操作链并委托给 TaskEngine 执行
 *
 * 注意：本类不包含任何业务逻辑，仅作为调度层与执行层的桥梁
 */
@Slf4j
public class TaskJob implements InterruptableJob {

  /**
   * 存储每个任务的中断标志
   * Key: taskId
   * Value: 中断标志
   */
  private static final ConcurrentHashMap<String, AtomicBoolean> INTERRUPT_FLAGS = new ConcurrentHashMap<>();

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    log.info("收到中断信号");
    
    // 从 JobExecutionContext 获取 taskId（集群模式下需要从上下文中获取）
    // 注意：interrupt() 方法无法直接访问 JobExecutionContext
    // 所以这里只能依赖 Thread.interrupt()
    // 真正的中断标志设置应该在 TaskScheduler 中通过数据库或其他方式传递
    
    // Quartz 会通过 Thread.interrupt() 中断线程
    Thread.currentThread().interrupt();
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      // 解析 Job 数据
      JobDataMap jobDataMap = context.getMergedJobDataMap();
      List<NodeDefinition> nodes = getJobData(jobDataMap, "nodes");
      TaskContext taskContext = getJobData(jobDataMap, "context");

      String taskId = taskContext.getTaskId();

      // 为当前任务创建独立的中断标志
      AtomicBoolean interruptFlag = new AtomicBoolean(false);
      INTERRUPT_FLAGS.put(taskId, interruptFlag);

      // 将中断标志传递给 context
      taskContext.setInterrupted(interruptFlag);

      log.info(
          "开始执行流水线，实例ID: {}, 节点数: {}",
          taskId, nodes.size()
      );

      // 获取执行引擎
      TaskEngine engine = KitSpringBeanHolder.getBean(TaskEngine.class);

      // 构建操作链
      List<TaskOperation> operations = buildOperations(nodes);

      // 如果是重试模式，裁剪操作链
      if (taskContext.getRetryBizCode() != null) {
        log.info("检测到重试标识，从节点 {} 开始执行", taskContext.getRetryBizCode());
        operations = extractOperationsFromNode(operations, nodes, taskContext.getRetryBizCode());
      }

      try {
        // 执行任务链
        TaskResult result = engine.execute(operations, taskContext);

        if (result.isSuccess()) {
          log.info("流水线执行成功，实例ID: {}", taskId);
        } else {
          log.error(
              "流水线执行失败，实例ID: {}, 失败操作: {}",
              taskId, result.getFailedOperation()
          );
        }
      } finally {
        // 清理中断标志
        INTERRUPT_FLAGS.remove(taskId);
        log.debug("清理任务中断标志，taskId: {}", taskId);
      }

    } catch (Exception e) {
      log.error("流水线执行异常", e);
      throw new JobExecutionException("流水线执行失败", e);
    }
  }

  /**
   * 设置任务的中断标志（供 TaskScheduler 调用）
   */
  public static void setInterruptFlag(String taskId) {
    AtomicBoolean flag = INTERRUPT_FLAGS.get(taskId);
    if (flag != null) {
      flag.set(true);
      log.info("设置任务中断标志，taskId: {}", taskId);
    } else {
      log.warn("未找到任务的中断标志，可能任务已结束或未开始，taskId: {}", taskId);
    }
  }

  /**
   * 构建操作链
   */
  private List<TaskOperation> buildOperations(List<NodeDefinition> nodes) {
    List<TaskOperation> operations = new java.util.ArrayList<>();
    for (NodeDefinition node : nodes) {
      TaskOperation operation = KitSpringBeanHolder.getBean(node.getCode());
      if (operation == null) {
        throw new IllegalArgumentException("未找到节点操作实现: " + node.getCode());
      }
      operations.add(operation);
    }
    return operations;
  }

  /**
   * 从指定节点开始提取操作链
   */
  private List<TaskOperation> extractOperationsFromNode(
      List<TaskOperation> allOperations,
      List<NodeDefinition> nodes,
      String retryBizCode
  ) {
    int startIndex = -1;
    for (int i = 0; i < nodes.size(); i++) {
      if (nodes.get(i).getCode().equals(retryBizCode)) {
        startIndex = i;
        break;
      }
    }

    if (startIndex == -1) {
      throw new IllegalArgumentException("未找到重试节点: " + retryBizCode);
    }

    log.info("重试模式：从节点 [{}] 开始执行，索引={}", retryBizCode, startIndex);
    return allOperations.subList(startIndex, allOperations.size());
  }

  @SuppressWarnings("unchecked")
  private <T> T getJobData(JobDataMap jobDataMap, String key) {
    Object value = jobDataMap.get(key);
    if (value == null) {
      throw new IllegalArgumentException("Job数据缺失: " + key);
    }
    return (T) value;
  }
}
