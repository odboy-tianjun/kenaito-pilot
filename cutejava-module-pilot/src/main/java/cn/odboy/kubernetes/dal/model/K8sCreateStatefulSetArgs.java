package cn.odboy.kubernetes.dal.model;

import cn.odboy.base.KitObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class K8sCreateStatefulSetArgs extends KitObject {

  // 应用名称
  private String contextName;
  // 环境标识
  private String envCode;
  // 副本数
  private Integer replicas;
  // 镜像地址
  private String image;
  // 服务端口
  private Integer port;
  // CPU资源
  private String cpu;
  // 内存资源
  private String memory;
  // 存储卷大小
  private String volumeSize;
  // 最大不可用Pod数
  private Integer maxUnavailable;
  // 分区更新数
  private Integer partition;
}
