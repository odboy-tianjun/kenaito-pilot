package cn.odboy.kubernetes.initializer;

import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.kubernetes.dal.dataobject.K8sNodeTb;
import cn.odboy.kubernetes.repository.KubernetesRepository;
import cn.odboy.kubernetes.service.K8sNodeService;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class KubernetesRepositoryInitializer implements InitializingBean, KubernetesRepository {

  @Autowired
  private K8sNodeService k8sNodeService;
  @Resource(name = "taskAsync")
  private ThreadPoolTaskExecutor taskExecutor;

  @Override
  public KubernetesClient getClusterClient(String clusterCode) {
    K8sNodeTb k8sNode = k8sNodeService.getByClusterCode(clusterCode);
    Config config = Config.fromKubeconfig(k8sNode.getClusterConfig());
    return new KubernetesClientBuilder().withConfig(config).build();
  }

  @Override
  public Namespace getNamespaceByName(KubernetesClient clusterClient, String name) {
    try (clusterClient) {
      return clusterClient.namespaces().withName(name).get();
    } catch (Exception e) {
      throw new BadRequestException(e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    List<K8sNodeTb> k8sNodes = k8sNodeService.list();
    for (K8sNodeTb k8sNode : k8sNodes) {
      try (KubernetesClient clusterClient = this.getClusterClient(k8sNode.getClusterCode())) {
        this.getNamespaceByName(clusterClient, "default");
        k8sNode.setStatus(true);
        String masterUrl = clusterClient.getConfiguration().getMasterUrl();
        if (masterUrl != null && !masterUrl.isEmpty()) {
          java.net.URI uri = new java.net.URI(masterUrl);
          String host = uri.getHost();
          k8sNode.setClusterIp(host);
        }
        k8sNodeService.updateStatusById(k8sNode.getId(), true, "");
      } catch (Exception e) {
        k8sNode.setStatus(false);
        k8sNode.setErrorMessage(e.getMessage());
        k8sNodeService.updateStatusById(k8sNode.getId(), false, e.getMessage());
      }
    }
  }
}
