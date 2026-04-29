package cn.odboy.pipeline.core;

import lombok.Data;

/**
 * 任务执行结果 封装任务执行的最终状态和相关信息
 */
@Data
public class TaskResult {

  /**
   * 是否执行成功
   */
  private boolean success;

  /**
   * 结果消息描述
   */
  private String message;

  /**
   * 失败的操作名称（仅在失败时有值）
   */
  private String failedOperation;

  /**
   * 创建成功结果
   *
   * @param message 成功消息
   * @return 成功的任务结果对象
   */
  public static TaskResult success(String message) {
    TaskResult result = new TaskResult();
    result.setSuccess(true);
    result.setMessage(message);
    result.setFailedOperation(null);
    return result;
  }

  /**
   * 创建失败结果
   *
   * @param operation 失败的操作名称
   * @param message   失败消息
   * @return 失败的任务结果对象
   */
  public static TaskResult fail(String operation, String message) {
    TaskResult result = new TaskResult();
    result.setSuccess(false);
    result.setFailedOperation(operation);
    result.setMessage(message);
    return result;
  }
}
