package cn.odboy.pipeline;

import lombok.Data;
import java.util.Map;

/**
 * 节点定义模型
 */
@Data
public class NodeDefinition {

  /**
   * 业务编码
   */
  private String code;
  /**
   * 业务名称
   */
  private String name;
  /**
   * 节点类型：service、rpc
   */
  private String type;
  /**
   * 是否可以重试
   */
  private boolean retry;
  /**
   * 节点默认参数
   */
  private Map<String, Object> parameters;
}
