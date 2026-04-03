package cn.odboy.kubernetes.dal.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class K8sDeleteStatefulSetArgs {

  @NotBlank(message = "参数 集群编码 必填")
  private String clusterCode;
  @NotBlank(message = "参数 应用名称 必填")
  private String contextName;
}
