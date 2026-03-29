package cn.odboy.kubernetes.initializer;

import cn.hutool.core.thread.ThreadUtil;
import cn.odboy.constant.SystemConst;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.kubernetes.dal.dataobject.K8sNodeTb;
import cn.odboy.kubernetes.repository.KubernetesRepository;
import cn.odboy.kubernetes.service.K8sNodeService;
import cn.odboy.meta.constant.K8sLabelEnum;
import cn.odboy.meta.constant.StatusEnum;
import cn.odboy.meta.util.PilotNameUtil;
import cn.odboy.util.KitDateUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class KubernetesRepositoryInitializer implements InitializingBean, KubernetesRepository {

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
  private KubernetesClient getClient(String clusterCode) {
    K8sNodeTb k8sNode = k8sNodeService.getByClusterCode(clusterCode);
    Config config = Config.fromKubeconfig(k8sNode.getClusterClientConfig());
    return new KubernetesClientBuilder().withConfig(config).build();
  }

  @Override
  public Namespace getNamespaceByName(String clusterCode, String name) {
    KubernetesClient client = clientMap.get(clusterCode);
    try {
      return client.namespaces().withName(name).get();
    } catch (Exception e) {
      log.debug("根据名称查询namespace失败", e);
      throw new BadRequestException(e);
    }
  }

  @Override
  public Namespace createNamespace(String clusterCode, String name) {
    KubernetesClient client = clientMap.get(clusterCode);
    // 检查是否存在
    Namespace existing = client.namespaces().withName(name).get();
    if (existing != null) {
      log.debug("Namespace already exists: {}", name);
      return existing;
    }
    // 不存在则创建
    Namespace namespace = new NamespaceBuilder()
        .withNewMetadata()
        .withName(name)
        .addToLabels(K8sLabelEnum.CREATE_BY.getCode(), SystemConst.CURRENT_APP_NAME)
        .addToLabels(K8sLabelEnum.CREATE_AT.getCode(), KitDateUtil.getNowDateTimeMsStr())
        .endMetadata().build();
    try {
      Namespace created = client.namespaces().resource(namespace).create();
      log.debug("Namespace created: {}", name);
      return created;
    } catch (KubernetesClientException e) {
      // 处理并发创建的情况
      if (e.getCode() == 409) {
        log.debug("Namespace was created by another process: {}", name);
        return client.namespaces().withName(name).get();
      }
      throw new BadRequestException(e);
    }
  }

  @Override
  public Service createClusterIPService(String clusterCode, String contextName, String envCode, Integer servicePort) {
    KubernetesClient client = clientMap.get(clusterCode);
    String serviceName = PilotNameUtil.getServiceName(contextName, envCode);
    // 检查是否存在
    Service existing = client.services().inNamespace(contextName).withName(serviceName).get();
    if (existing != null) {
      log.debug("Service already exists: {}/{}", contextName, serviceName);
      return existing;
    }

    // 选择哪些pod
    Map<String, String> selectorLabels = new HashMap<>();
    selectorLabels.put(K8sLabelEnum.APP.getCode(), contextName);
    selectorLabels.put(K8sLabelEnum.ENV.getCode(), envCode);
    selectorLabels.put(K8sLabelEnum.RESOURCE_GROUP.getCode(), PilotNameUtil.getPodName(contextName, envCode));

    // 不存在则创建
    Service service = new ServiceBuilder()
        .withNewMetadata()
        .withName(serviceName)
        .withNamespace(contextName)
        .addToLabels(K8sLabelEnum.CREATE_BY.getCode(), SystemConst.CURRENT_APP_NAME)
        .addToLabels(K8sLabelEnum.CREATE_AT.getCode(), KitDateUtil.getNowDateTimeMsStr())
        .addToLabels(K8sLabelEnum.RESOURCE_GROUP.getCode(), serviceName)
        .endMetadata()
        .withNewSpec()
        // 服务类型
        .withType("ClusterIP")
        // kube-proxy 基于 spec.internalTrafficPolicy 的设置来过滤路由的目标服务端点。 当它的值设为 Local 时，只会选择节点本地的服务端点。 当它的值设为 Cluster 或缺省时，Kubernetes 会选择所有的服务端点
        // 将 internalTrafficPolicy: Local 改成Cluster 之后所有节点都可以可以curl 通nginx
        .withInternalTrafficPolicy("Cluster")
        // 指定服务期望的IP地址族的列表。IP地址族可以是IPv4或IPv6
        .withIpFamilies("IPv4")
        // 指定Service的IP家族策略。它的可选值有SingleStack、PreferDualStack和RequireDualStack
        // SingleStack: 表示服务只使用单个IP家族（IPv4或IPv6）
        // PreferDualStack: 表示服务优先使用双栈（IPv4和IPv6），但在无法提供双栈时会退回到单栈
        // RequireDualStack: 表示服务需要双栈支持，如果无法提供双栈，服务将无法创建
        .withIpFamilyPolicy("SingleStack")
        // 通过标签关联pod
        .withSelector(selectorLabels)
        // 指定选择后端 Pod 的方法。当 spec.sessionAffinity 设置为 ClientIP 时，来自同一个客户端的请求会被转发到同一个后端 Pod
        .withSessionAffinity("None")
        .withPorts(new ServicePortBuilder()
            // Service端口
            .withPort(80)
            // 容器端口
            .withTargetPort(new IntOrString(servicePort))
            // 协议
            .withProtocol("TCP")
            .build())
        .endSpec()
        .build();

    try {
      Service created = client.services().inNamespace(contextName).resource(service).create();
      log.debug("Service created: {}/{}", contextName, serviceName);
      return created;
    } catch (KubernetesClientException e) {
      // 处理并发创建的情况
      if (e.getCode() == 409) {
        log.debug("Service was created by another process: {}/{}", contextName, serviceName);
        return client.services().inNamespace(contextName).withName(serviceName).get();
      }
      throw new BadRequestException(e);
    }
  }

  @Override
  public StatefulSet applyStatefulSet(String clusterCode, String yamlContent) {
    try (KubernetesClient client = clientMap.get(clusterCode)) {
      // 加载 YAML 资源
      StatefulSet sts = client.apps().statefulSets()
          .load(yamlContent)
          .item();
      // 使用 Server-Side Apply 创建或更新
      // PatchContext 可以设置 fieldManager 和强制覆盖
      PatchContext patchContext = PatchContext.of(PatchType.SERVER_SIDE_APPLY);
      return client.apps().statefulSets()
//          .inNamespace(appName)
          .resource(sts)
          .patch(patchContext);
    } catch (Exception e) {
      throw new RuntimeException("Failed to apply StatefulSet", e);
    }
  }

  @Override
  public void afterPropertiesSet() {
    if (isLoopCheck) {
      ThreadUtil.execAsync(() -> {
        while (true) {
          this.scanClients();
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
        k8sNodeService.update(null, new LambdaUpdateWrapper<K8sNodeTb>()
            .eq(K8sNodeTb::getId, k8sNode.getId())
            .set(K8sNodeTb::getHealthStatus, StatusEnum.ENABLED.getCode())
            .set(K8sNodeTb::getClusterIp, k8sNode.getClusterIp())
            .set(K8sNodeTb::getClusterVersion, k8sNode.getClusterVersion())
        );
      } catch (Exception e) {
        k8sNode.setErrorMessage(e.getMessage());
        KubernetesClient errorClient = clientMap.remove(k8sNode.getClusterCode());
        errorClient.close();
        k8sNodeService.update(null, new LambdaUpdateWrapper<K8sNodeTb>()
            .eq(K8sNodeTb::getId, k8sNode.getId())
            .set(K8sNodeTb::getHealthStatus, StatusEnum.DISABLED.getCode())
            .set(K8sNodeTb::getErrorMessage, e.getMessage())
        );
      }
    }
    log.info("[finish]扫描并检测k8s客户端...");
  }
}
