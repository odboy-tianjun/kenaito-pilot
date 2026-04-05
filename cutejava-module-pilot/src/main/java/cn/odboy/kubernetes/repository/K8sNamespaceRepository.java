package cn.odboy.kubernetes.repository;

import io.fabric8.kubernetes.api.model.Namespace;

public interface K8sNamespaceRepository {

  /**
   * 根据命名空间名称查询
   *
   * @param clusterCode 集群编码
   * @param name        命名空间名称
   * @return /
   */
  Namespace getNamespaceByName(String clusterCode, String name);

  /**
   * 创建 Namespace，如果已存在则返回现有对象
   *
   * @param clusterCode 集群编码
   * @param name        命名空间名称
   * @return /
   */
  Namespace createNamespace(String clusterCode, String name);
}
