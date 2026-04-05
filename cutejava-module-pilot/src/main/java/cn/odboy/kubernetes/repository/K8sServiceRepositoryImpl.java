package cn.odboy.kubernetes.repository;

import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.kubernetes.dal.dataobject.K8sNodeTb;
import cn.odboy.kubernetes.initializer.K8sClientRepository;
import cn.odboy.kubernetes.service.K8sNodeService;
import cn.odboy.meta.constant.K8sLabelEnum;
import cn.odboy.meta.util.K8sNameUtil;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class K8sServiceRepositoryImpl implements K8sServiceRepository {

  @Autowired
  private K8sClientRepository k8sClientRepository;
  @Autowired
  private K8sNodeService k8sNodeService;

  @Override
  public Service createClusterIPService(String clusterCode, String contextName, Integer servicePort) {
    KubernetesClient client = k8sClientRepository.getClient(clusterCode);

    K8sNodeTb k8sNode = k8sNodeService.getByClusterCode(clusterCode);
    String clusterEnv = k8sNode.getClusterEnv();

    String serviceName = K8sNameUtil.getServiceName(contextName, clusterEnv);
    // 检查是否存在
    Service existing = client.services().inNamespace(contextName).withName(serviceName).get();
    if (existing != null) {
      log.debug("Service already exists: {}/{}", contextName, serviceName);
      return existing;
    }

    // 选择哪些pod
    Map<String, String> selectorLabels = new HashMap<>();
    selectorLabels.put(K8sLabelEnum.APP.getCode(), contextName);
    selectorLabels.put(K8sLabelEnum.ENV.getCode(), clusterEnv);
    selectorLabels.put(K8sLabelEnum.RESOURCE_GROUP.getCode(), K8sNameUtil.getPodName(contextName, clusterEnv));

    // 不存在则创建
    Service service = new ServiceBuilder()
        .withNewMetadata()
        .withName(serviceName)
        .withNamespace(contextName)
        .addToLabels(K8sLabelEnum.APP.getCode(), contextName)
        .addToLabels(K8sLabelEnum.ENV.getCode(), clusterEnv)
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
}
