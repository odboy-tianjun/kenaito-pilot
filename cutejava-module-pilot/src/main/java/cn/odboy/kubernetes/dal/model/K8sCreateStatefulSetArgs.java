package cn.odboy.kubernetes.dal.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class K8sCreateStatefulSetArgs {

  @NotBlank(message = "参数 集群编码 必填")
  private String clusterCode;
  @NotBlank(message = "参数 应用名称 必填")
  private String contextName;
  // 副本数
  private Integer replicas = 1;
  // 镜像地址
  private String image;
  // 服务端口
  private Integer port = 8000;
  // CPU资源
  private String cpu = "0.5";
  // 内存资源
  private String memory = "1Gi";
  // 存储卷大小
  private String volumeSize = "30Gi";
  // 最大不可用Pod数
  private Integer maxUnavailable = 1;
  // 开启就绪与存活检测
  private Boolean openLivenessCheck = false;
}
