package cn.odboy.kubernetes.initializer;

import io.fabric8.kubernetes.client.KubernetesClient;

public interface K8sClientRepository {

  KubernetesClient getClient(String clusterCode);

}
