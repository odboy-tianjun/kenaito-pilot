package cn.odboy.pipeline.core;

/**
 * 任务操作接口 定义单个任务操作的基本行为，支持同步执行和异步等待
 */
public interface TaskOperation {

  /**
   * 执行任务操作
   *
   * @param context 任务上下文
   * @return 执行状态（success/failure/running）
   */
  String execute(TaskContext context);

  /**
   * 查询异步任务的当前状态 默认实现直接返回成功，适用于同步操作
   *
   * @param context 任务上下文
   * @return 当前状态
   */
  default String describe(TaskContext context) {
    return TaskStatusEnum.SUCCESS.getCode();
  }

  /**
   * 获取操作名称
   *
   * @return 操作名称，用于日志和错误报告
   */
  String name();

  /**
   * 获取重试次数 默认重试3次
   *
   * @return 最大重试次数
   */
  default int retryTimes() {
    return 3;
  }

  /**
   * 获取轮询间隔（毫秒） 用于异步任务状态查询的等待时间 默认1000毫秒
   *
   * @return 轮询间隔时间（毫秒）
   */
  default long pollInterval() {
    return 1000;
  }
}
