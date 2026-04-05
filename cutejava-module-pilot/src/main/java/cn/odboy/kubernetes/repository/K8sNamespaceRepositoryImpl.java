package cn.odboy.kubernetes.repository;

import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.kubernetes.initializer.K8sClientRepository;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class K8sNamespaceRepositoryImpl implements K8sNamespaceRepository {

  @Autowired
  private K8sClientRepository k8sClientRepository;

  @Override
  public Namespace getNamespaceByName(String clusterCode, String name) {
    KubernetesClient client = k8sClientRepository.getClient(clusterCode);
    try {
      return client.namespaces().withName(name).get();
    } catch (Exception e) {
      log.debug("根据名称查询namespace失败", e);
      throw new BadRequestException(e);
    }
  }

  @Override
  public Namespace createNamespace(String clusterCode, String contextName) {
    KubernetesClient client = k8sClientRepository.getClient(clusterCode);
    // 检查是否存在
    Namespace existing = client.namespaces().withName(contextName).get();
    if (existing != null) {
      log.debug("Namespace already exists: {}", contextName);
      return existing;
    }
    // 不存在则创建
    Namespace namespace = new NamespaceBuilder()
        .withNewMetadata()
        .withName(contextName)
        .endMetadata().build();
    try {
      Namespace created = client.namespaces().resource(namespace).create();
      log.debug("Namespace created: {}", contextName);
      return created;
    } catch (KubernetesClientException e) {
      // 处理并发创建的情况
      if (e.getCode() == 409) {
        log.debug("Namespace was created by another process: {}", contextName);
        return client.namespaces().withName(contextName).get();
      }
      throw new BadRequestException(e);
    }
  }
}
