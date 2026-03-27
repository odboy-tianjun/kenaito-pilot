package cn.odboy.meta.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * K8s服务类型枚举
 *
 * @author odboy
 */
@Getter
@AllArgsConstructor
public enum K8sServiceTypeEnum {

  CLUSTER_IP("ClusterIP", "集群内部ServiceIP地址"),
  NODE_PORT("NodePort", "集群节点IP地址");

  private final String code;
  private final String name;
}