package cn.odboy.kubernetes.repository;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;

public interface KubernetesRepository {

  KubernetesClient getClient(String clusterCode);

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

  /**
   * 创建 ClusterIP 类型的 Service
   *
   * @param clusterCode 集群编码
   * @param contextName 上下文名称
   * @param envCode     环境编码
   * @param servicePort 服务端口
   * @return 创建或已存在的 Service 对象
   */
  Service createClusterIPService(String clusterCode, String contextName, String envCode, Integer servicePort);

  /**
   * 从 YAML 文件创建或更新 StatefulSet
   *
   * @param clusterCode 集群编码
   * @param yamlContent statefulset负载内容
   */
  StatefulSet applyStatefulSet(String clusterCode, String yamlContent);
}
