package cn.odboy.kubernetes.initializer;

import cn.hutool.core.thread.ThreadUtil;
import cn.odboy.kubernetes.dal.dataobject.K8sNodeTb;
import cn.odboy.kubernetes.service.K8sNodeService;
import cn.odboy.meta.constant.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.VersionInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class K8sClientRepositoryInitializer implements InitializingBean, K8sClientRepository {

  @Autowired
  private K8sNodeService k8sNodeService;
  @Resource(name = "taskAsync")
  private ThreadPoolTaskExecutor taskExecutor;
  /**
   * 存放所有客户端
   */
  private final Map<String, KubernetesClient> clientMap = new ConcurrentHashMap<>();
  /**
   * 功能测试阶段建议设置为false
   */
  private static final boolean isLoopCheck = false;

  /**
   * 根据集群编码获取客户端
   *
   * @param clusterCode 集群编码
   * @return /
   */
  @Override
  public KubernetesClient getClient(String clusterCode) {
    K8sNodeTb k8sNode = k8sNodeService.getByClusterCode(clusterCode);
    Config config = Config.fromKubeconfig(k8sNode.getClusterClientConfig());
    return new KubernetesClientBuilder().withConfig(config).build();
  }

  @Override
  public void afterPropertiesSet() {
    if (isLoopCheck) {
      taskExecutor.execute(() -> {
        while (true) {
          K8sClientRepositoryInitializer.this.scanClients();
          ThreadUtil.sleep(5000);
        }
      });
    } else {
      this.scanClients();
    }
  }

  private void scanClients() {
    log.info("[begin]扫描并检测k8s客户端...");
    List<K8sNodeTb> k8sNodes = k8sNodeService.list();
    for (K8sNodeTb k8sNode : k8sNodes) {
      try {
        KubernetesClient clusterClient = this.getClient(k8sNode.getClusterCode());
        String masterUrl = clusterClient.getConfiguration().getMasterUrl();
        if (masterUrl != null && !masterUrl.isEmpty()) {
          java.net.URI uri = new java.net.URI(masterUrl);
          String host = uri.getHost();
          k8sNode.setClusterIp(host);
        }
        // 获取 Kubernetes 集群版本
        VersionInfo kubernetesVersion = clusterClient.getKubernetesVersion();
        if (kubernetesVersion != null) {
          k8sNode.setClusterVersion(kubernetesVersion.getGitVersion());
        }
        clientMap.put(k8sNode.getClusterCode(), clusterClient);
        k8sNodeService.update(
            null, new LambdaUpdateWrapper<K8sNodeTb>()
                .eq(K8sNodeTb::getId, k8sNode.getId())
                .set(K8sNodeTb::getHealthStatus, StatusEnum.ENABLED.getCode())
                .set(K8sNodeTb::getClusterIp, k8sNode.getClusterIp())
                .set(K8sNodeTb::getClusterVersion, k8sNode.getClusterVersion())
        );
      } catch (Exception e) {
        k8sNode.setErrorMessage(e.getMessage());
        KubernetesClient errorClient = clientMap.remove(k8sNode.getClusterCode());
        if (errorClient != null) {
          errorClient.close();
        }
        k8sNodeService.update(
            null, new LambdaUpdateWrapper<K8sNodeTb>()
                .eq(K8sNodeTb::getId, k8sNode.getId())
                .set(K8sNodeTb::getHealthStatus, StatusEnum.DISABLED.getCode())
                .set(K8sNodeTb::getErrorMessage, e.getMessage())
        );
      }
    }
    log.info("[finish]扫描并检测k8s客户端...");
  }
}
