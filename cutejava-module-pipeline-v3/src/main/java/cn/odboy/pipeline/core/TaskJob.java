package cn.odboy.pipeline.core;

import cn.odboy.framework.context.KitSpringBeanHolder;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Quartz Job 适配器 职责： 1. 接收 Quartz 调度触发 2. 解析 Job 数据 3. 构建操作链并委托给 TaskEngine 执行
 *
 * 注意：本类不包含任何业务逻辑，仅作为调度层与执行层的桥梁
 */
@Slf4j
public class TaskJob implements InterruptableJob {

  private static final AtomicBoolean interrupted = new AtomicBoolean(false);

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    log.info("收到中断信号");
    interrupted.set(true);
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      // 解析 Job 数据
      JobDataMap jobDataMap = context.getMergedJobDataMap();
      List<NodeDefinition> nodes = getJobData(jobDataMap, "nodes");
      TaskContext taskContext = getJobData(jobDataMap, "context");

      // 将中断标志传递给 context
      taskContext.setInterrupted(interrupted);

      log.info(
          "开始执行流水线，实例ID: {}, 节点数: {}",
          taskContext.getTaskId(), nodes.size()
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

      // 执行任务链
      TaskResult result = engine.execute(operations, taskContext);

      if (result.isSuccess()) {
        log.info("流水线执行成功，实例ID: {}", taskContext.getTaskId());
      } else {
        log.error(
            "流水线执行失败，实例ID: {}, 失败操作: {}",
            taskContext.getTaskId(), result.getFailedOperation()
        );
      }

    } catch (Exception e) {
      log.error("流水线执行异常", e);
      throw new JobExecutionException("流水线执行失败", e);
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
