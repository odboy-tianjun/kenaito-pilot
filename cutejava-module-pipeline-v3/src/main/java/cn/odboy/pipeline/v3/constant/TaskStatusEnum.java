package cn.odboy.pipeline.v3.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态 定义任务执行过程中可能的状态值
 */
@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
  PENDING("pending", "任务待执行"),
  RUNNING("running", "任务执行中"),
  SUCCESS("success", "任务执行成功"),
  FAILURE("failure", "任务执行失败");

  private final String code;
  private final String message;
}
