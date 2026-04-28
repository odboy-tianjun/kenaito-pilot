package cn.odboy.pipeline.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务节点类型
 */
@Getter
@AllArgsConstructor
public enum TaskNodeTypeEnum {
  SERVICE("service", "执行内部方法"),
  RPC("rpc", "调用外部接口");

  private final String code;
  private final String message;
}
