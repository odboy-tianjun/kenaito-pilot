package cn.odboy.pipeline.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态常量接口 定义任务执行过程中可能的状态值
 */
@Getter
@AllArgsConstructor
public enum TaskStatusEnum {

  RUNNING("running", "任务正在执行中"),
  SUCCESS("success", "任务执行成功"),
  FAILURE("failure", "任务执行失败");

  private final String code;
  private final String message;
}
