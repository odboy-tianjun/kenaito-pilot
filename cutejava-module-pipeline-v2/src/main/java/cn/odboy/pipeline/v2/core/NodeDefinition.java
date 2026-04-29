package cn.odboy.pipeline.v2.core;

import cn.odboy.base.KitObject;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 节点定义模型
 */
@Getter
@Setter
public class NodeDefinition extends KitObject {

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
   * 远程调用配置
   */
  private RpcConfig rpcConfig;
  /**
   * 是否可以重试
   */
  private boolean retry;
  /**
   * 节点默认参数
   */
  private Map<String, Object> parameters;

  @Getter
  @Setter
  public static class RpcConfig extends KitObject {

    /**
     * 请求类型(Get、Post)
     */
    private String requestType;
    /**
     * 执行任务请求
     */
    private String execute;
    /**
     * 查询明细请求
     */
    private String describe;
  }
}
