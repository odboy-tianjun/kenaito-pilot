package cn.odboy.kubernetes.dal.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class K8sDeleteIngressArgs {

  @NotBlank(message = "参数 集群编码 必填")
  private String clusterCode;
  @NotBlank(message = "参数 上下文名称 必填")
  private String contextName;
  @NotBlank(message = "参数 Ingress名称 必填")
  private String ingressName;
}
