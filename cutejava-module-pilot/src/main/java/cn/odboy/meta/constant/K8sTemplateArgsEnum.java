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

  IMAGE("PilotImage", "使用中的镜像"),
  PORT("PilotPort", "服务提供的端口"),
  REPLICAS("PilotReplicas", "服务使用的副本数量"),
  PARTITION("PilotPartition", "服务更新时指定保留的旧版本 Pod 数量"),
  MAX_UNAVAILABLE("PilotMaxUnavailable", "服务更新时最大不可用的 Pod 数量"),
  CPU("PilotCpu", "服务所需的CPU数量"),
  MEMORY_SIZE("PilotMemorySize", "服务所需的内存数量，单位Gi"),
  DISK_SIZE("PilotDiskSize", "服务使用的存储大小，单位Gi");
  private final String code;
  private final String name;
}
