package cn.odboy.kubernetes.repository;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClient;

public interface KubernetesRepository {

  KubernetesClient getClusterClient(String clusterCode);

  Namespace getNamespaceByName(KubernetesClient clusterClient, String name);
}
