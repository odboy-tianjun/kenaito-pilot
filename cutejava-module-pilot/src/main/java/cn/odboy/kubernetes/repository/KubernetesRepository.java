package cn.odboy.kubernetes.repository;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClient;

public interface KubernetesRepository {

  /**
   * 根据集群编码获取客户端
   *
   * @param clusterCode 集群编码
   * @return /
   */
  KubernetesClient getClusterClient(String clusterCode);

  /**
   * 根据命名空间名称查询
   *
   * @param clusterClient /
   * @param name          命名空间名称
   * @return /
   */
  Namespace getNamespaceByName(KubernetesClient clusterClient, String name);
}
