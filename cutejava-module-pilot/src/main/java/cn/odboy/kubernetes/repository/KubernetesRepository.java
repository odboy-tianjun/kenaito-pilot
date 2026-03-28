package cn.odboy.kubernetes.repository;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
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

  /**
   * 创建 Namespace，如果已存在则返回现有对象
   */
  Namespace createNamespace(KubernetesClient client, String name);

  /**
   * 创建 ClusterIP 类型的 Service
   *
   * @param client      Kubernetes 客户端
   * @param contextName 上下文名称
   * @param envCode     环境编码
   * @param servicePort 服务端口
   * @return 创建或已存在的 Service 对象
   */
  Service createClusterIPService(KubernetesClient client, String contextName, String envCode, Integer servicePort);

  /**
   * 从 YAML 文件创建或更新 StatefulSet
   */
  StatefulSet applyStatefulSet(KubernetesClient client, String yamlContent);
}
