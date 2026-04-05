package cn.odboy.kubernetes.dal.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class K8sCreateIngressArgs {

  @NotBlank(message = "参数 集群编码 必填")
  private String clusterCode;
  @NotBlank(message = "参数 上下文名称 必填")
  private String contextName;
  @NotBlank(message = "参数 域名 必填")
  private String host;
  private String path;
  @NotBlank(message = "参数 Service名称 必填")
  private String serviceName;
}
