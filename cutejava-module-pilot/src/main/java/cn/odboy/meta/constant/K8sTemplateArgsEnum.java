package cn.odboy.meta.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * K8s模板参数枚举
 *
 * @author odboy
 */
@Getter
@AllArgsConstructor
public enum K8sTemplateArgsEnum {

  IMAGE("pilot-image", "使用中的镜像"),
  PORT("pilot-port", "服务提供的端口"),
  VOLUME_SIZE("pilot-volume-size", "服务使用的临时存储大小，单位Mi"),
  REPLICAS("pilot-replicas", "服务使用的副本数量"),
  PARTITION("pilot-partition", "服务更新时指定保留的旧版本 Pod 数量"),
  MAX_UNAVAILABLE("pilot-max-unavailable", "服务更新时最大不可用的 Pod 数量"),
  CPU("pilot-cpu", "服务所需的CPU数量"),
  MEMORY("pilot-memory", "服务所需的内存数量，单位Mi");
  private final String code;
  private final String name;
}
