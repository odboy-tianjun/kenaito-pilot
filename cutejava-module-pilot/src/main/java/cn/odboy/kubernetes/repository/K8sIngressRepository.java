package cn.odboy.kubernetes.repository;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;

public interface K8sIngressRepository {

  /**
   * 创建 Ingress
   *
   * @param clusterCode 集群编码
   * @param contextName 上下文名称
   * @param host        域名
   * @param path        路径，默认为 '/'
   * @param serviceName Service 名称
   * @param servicePort Service 端口
   * @return /
   */
  Ingress createIngress(String clusterCode, String contextName, String host, String path, String serviceName, Integer servicePort);

  /**
   * 删除 Ingress
   *
   * @param clusterCode 集群编码
   * @param contextName 上下文名称
   * @param ingressName Ingress名称
   * @return /
   */
  boolean deleteIngress(String clusterCode, String contextName, String ingressName);
}
