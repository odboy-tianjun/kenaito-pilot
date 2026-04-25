package cn.odboy.pipeline;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
@AllArgsConstructor
public enum PipelineStatusEnum {
  PENDING("pending", "待执行"),
  RUNNING("running", "执行中"),
  SUCCESS("success", "执行成功"),
  FAILURE("failure", "执行失败");

  private final String code;
  private final String desc;
}
