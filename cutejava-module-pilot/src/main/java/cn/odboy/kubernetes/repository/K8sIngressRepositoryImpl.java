package cn.odboy.kubernetes.repository;

import cn.hutool.core.util.StrUtil;
import cn.odboy.kubernetes.dal.dataobject.K8sNodeTb;
import cn.odboy.kubernetes.dal.model.K8sCreateIngressArgs;
import cn.odboy.kubernetes.dal.model.K8sDeleteIngressArgs;
import cn.odboy.kubernetes.initializer.K8sClientRepository;
import cn.odboy.kubernetes.service.K8sNodeService;
import cn.odboy.meta.util.K8sNameUtil;
import cn.odboy.util.KitDateUtil;
import cn.odboy.util.KitValidUtil;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class K8sIngressRepositoryImpl implements K8sIngressRepository {

  @Autowired
  private K8sClientRepository k8sClientRepository;
  @Autowired
  private K8sNodeService k8sNodeService;

  @Override
  public Ingress createIngress(K8sCreateIngressArgs args) {
    KitValidUtil.validate(args);

    K8sNodeTb k8sNode = k8sNodeService.getByClusterCode(args.getClusterCode());
    String versionCode = KitDateUtil.getNowDateTimeStr();
    String ingressName = K8sNameUtil.getIngressName(args.getContextName(), k8sNode.getClusterEnv(), versionCode);
    log.debug("创建ingress, name={}", ingressName);

    String currentPath = "/";
    if (StrUtil.isNotBlank(args.getPath())) {
      currentPath = args.getPath();
    }

    try (KubernetesClient client = k8sClientRepository.getClient(args.getClusterCode())) {
      Ingress ingress = new IngressBuilder()
//          .withApiVersion("networking.k8s.io/v1")
          .withKind("Ingress")
          .withNewMetadata()
          .withName(ingressName)
          .withNamespace(args.getContextName())
          .addToAnnotations("nginx.ingress.kubernetes.io/app.name", args.getContextName())
          .endMetadata()
          .withNewSpec()
          .addNewRule()
          .withHost(args.getHost())
          .withNewHttp()
          .addNewPath()
          .withPath(currentPath)
          .withPathType("ImplementationSpecific")
          .withNewBackend()
          .withNewService()
          .withName(args.getServiceName())
          .withNewPort()
          .withNumber(80)
          .endPort()
          .endService()
          .endBackend()
          .endPath()
          .endHttp()
          .endRule()
          .endSpec()
          .build();

      // 使用 Server-Side Apply 创建或更新
      PatchContext patchContext = PatchContext.of(PatchType.SERVER_SIDE_APPLY);

      return client.network().v1().ingresses()
          .inNamespace(args.getContextName())
          .resource(ingress)
          .patch(patchContext);
    }
  }

  @Override
  public boolean deleteIngress(K8sDeleteIngressArgs args) {
    KitValidUtil.validate(args);

    try (KubernetesClient client = k8sClientRepository.getClient(args.getClusterCode())) {
      Ingress ingress = client.network().v1().ingresses()
          .inNamespace(args.getContextName())
          .withName(args.getIngressName())
          .get();

      if (ingress == null) {
        log.debug("Ingress not exist: {}", args.getIngressName());
        return true;
      }

      List<StatusDetails> deleted = client.network().v1().ingresses()
          .inNamespace(args.getContextName())
          .withName(args.getIngressName())
          .delete();
      if (deleted != null && !deleted.isEmpty()) {
        log.debug("Ingress deleted: {}", args.getIngressName());
        return true;
      } else {
        log.debug("Ingress not found: {}", args.getIngressName());
        return false;
      }
    }
  }
}
