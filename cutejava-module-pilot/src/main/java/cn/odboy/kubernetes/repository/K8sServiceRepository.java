package cn.odboy.kubernetes.repository;

import io.fabric8.kubernetes.api.model.Service;

public interface K8sServiceRepository {

  /**
   * 创建 ClusterIP 类型的 Service
   *
   * @param clusterCode 集群编码
   * @param contextName 上下文名称
   * @param servicePort 服务端口
   * @return 创建或已存在的 Service 对象
   */
  Service createClusterIPService(String clusterCode, String contextName, Integer servicePort);
}
