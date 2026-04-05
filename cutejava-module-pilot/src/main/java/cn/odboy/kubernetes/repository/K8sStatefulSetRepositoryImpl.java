package cn.odboy.kubernetes.repository;

import cn.hutool.core.util.StrUtil;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.kubernetes.dal.dataobject.K8sNodeTb;
import cn.odboy.kubernetes.dal.model.K8sCreateStatefulSetArgs;
import cn.odboy.kubernetes.dal.model.K8sDeleteStatefulSetArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetImageArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetReplicasArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetStrategyArgs;
import cn.odboy.kubernetes.initializer.K8sClientRepository;
import cn.odboy.kubernetes.service.K8sNodeService;
import cn.odboy.meta.constant.K8sLabelEnum;
import cn.odboy.meta.util.K8sNameUtil;
import cn.odboy.util.KitValidUtil;
import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.AffinityBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.EmptyDirVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.ExecActionBuilder;
import io.fabric8.kubernetes.api.model.HTTPGetActionBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.LabelSelectorRequirement;
import io.fabric8.kubernetes.api.model.LabelSelectorRequirementBuilder;
import io.fabric8.kubernetes.api.model.Lifecycle;
import io.fabric8.kubernetes.api.model.LifecycleBuilder;
import io.fabric8.kubernetes.api.model.LifecycleHandlerBuilder;
import io.fabric8.kubernetes.api.model.PodAffinityTermBuilder;
import io.fabric8.kubernetes.api.model.PodAntiAffinityBuilder;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.WeightedPodAffinityTerm;
import io.fabric8.kubernetes.api.model.WeightedPodAffinityTermBuilder;
import io.fabric8.kubernetes.api.model.apps.RollingUpdateStatefulSetStrategyBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSetUpdateStrategy;
import io.fabric8.kubernetes.api.model.apps.StatefulSetUpdateStrategyBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class K8sStatefulSetRepositoryImpl implements K8sStatefulSetRepository {

  @Autowired
  private K8sNodeService k8sNodeService;
  @Autowired
  private K8sNamespaceRepository k8sNamespaceRepository;
  @Autowired
  private K8sClientRepository k8sClientRepository;

  @Override
  public StatefulSet createStatefulSet(K8sCreateStatefulSetArgs args) {
    KitValidUtil.validate(args);

    K8sNodeTb k8sNode = k8sNodeService.getByClusterCode(args.getClusterCode());

    k8sNamespaceRepository.createNamespace(args.getClusterCode(), args.getContextName());

    if (StrUtil.isBlank(args.getImage())) {
      // 设置默认镜像
      args.setImage(k8sNode.getDefaultImage());
    }

    try (KubernetesClient client = k8sClientRepository.getClient(args.getClusterCode())) {
      StatefulSet statefulSet = new StatefulSetBuilder()
          .withApiVersion("apps/v1")
          .withKind("StatefulSet")
          .withNewMetadata()
          .withName(K8sNameUtil.getPodName(args.getContextName(), k8sNode.getClusterEnv()))
          .withNamespace(args.getContextName())
          // StatefulSet元数据中的labels：用于标识和管理 StatefulSet 本身
          .addToLabels(K8sLabelEnum.APP.getCode(), args.getContextName())
          .addToLabels(K8sLabelEnum.ENV.getCode(), k8sNode.getClusterEnv())
          .endMetadata()
          .withNewSpec()
          .withReplicas(args.getReplicas())
          .withPodManagementPolicy("OrderedReady")
          .withServiceName(K8sNameUtil.getServiceName(args.getContextName(), k8sNode.getClusterEnv()))
          .withNewSelector()
          // Selector中的matchLabels：用于将 StatefulSet 与其创建的 Pod 关联起来，确保 StatefulSet 只管理那些带有特定标签的 Pod
          .addToMatchLabels(K8sLabelEnum.APP.getCode(), args.getContextName())
          .addToMatchLabels(K8sLabelEnum.ENV.getCode(), k8sNode.getClusterEnv())
          .endSelector()
          .withNewTemplate()
          .withNewMetadata()
          // Pod模板中的labels：用于标识和管理由 StatefulSet 创建的 Pod
          .addToLabels(K8sLabelEnum.APP.getCode(), args.getContextName())
          .addToLabels(K8sLabelEnum.ENV.getCode(), k8sNode.getClusterEnv())
          .endMetadata()
          .withNewSpec()
          .withAffinity(this.createAffinity(args.getContextName()))
          .withContainers(this.createContainer(
              args.getContextName(), k8sNode.getClusterEnv(), args.getImage(), args.getPort(), args.getCpu(), args.getMemory(),
              args.getOpenLivenessCheck()
          ))
          .withDnsPolicy("ClusterFirst")
          .withRestartPolicy("Always")
          .withTerminationGracePeriodSeconds(30L)
          .withVolumes(this.createVolume(args.getContextName(), args.getVolumeSize()))
          .endSpec()
          .endTemplate()
          .withUpdateStrategy(this.createUpdateStrategy(args.getMaxUnavailable(), args.getReplicas()))
          .endSpec()
          .build();

      // 使用 Server-Side Apply 创建或更新
      PatchContext patchContext = PatchContext.of(PatchType.SERVER_SIDE_APPLY);

      return client.apps().statefulSets()
          .inNamespace(args.getContextName())
          .resource(statefulSet)
          .patch(patchContext);
    }
  }

  @Override
  public void deleteStatefulSet(K8sDeleteStatefulSetArgs args) {
    KitValidUtil.validate(args);

    K8sNodeTb k8sNode = k8sNodeService.getByClusterCode(args.getClusterCode());

    try (KubernetesClient client = k8sClientRepository.getClient(args.getClusterCode())) {
      StatefulSet statefulSet = client.apps().statefulSets()
          .inNamespace(args.getContextName())
          .withName(K8sNameUtil.getPodName(args.getContextName(), k8sNode.getClusterEnv()))
          .get();
      if (statefulSet == null) {
        throw new BadRequestException("statefulset not found");
      }

      client.apps().statefulSets()
          .inNamespace(args.getContextName())
          // 传播策略
          // BACKGROUND	立即删除 StatefulSet，后台删除 Pod（默认）
          // FOREGROUND	先删除 io.fabric8.kubernetes.api.model.Pod，再删除 StatefulSet
          // ORPHAN	只删除 io.fabric8.kubernetes.api.model.apps.StatefulSet，保留 Pod
          .withPropagationPolicy(DeletionPropagation.FOREGROUND)
          .delete();
    }
  }

  @Override
  public void updateStatefulSetReplicas(K8sUpdateStatefulSetReplicasArgs args) {
    KitValidUtil.validate(args);

    K8sNodeTb k8sNode = k8sNodeService.getByClusterCode(args.getClusterCode());

    try (KubernetesClient client = k8sClientRepository.getClient(args.getClusterCode())) {
      StatefulSet statefulSet = client.apps().statefulSets()
          .inNamespace(args.getContextName())
          .withName(K8sNameUtil.getPodName(args.getContextName(), k8sNode.getClusterEnv()))
          .get();
      if (statefulSet == null) {
        throw new BadRequestException("statefulset not found");
      }

      // 更新副本数
      int newReplicas = args.getReplicas();
      int currentReplicas = statefulSet.getSpec().getReplicas();
      if (currentReplicas != newReplicas) {
        statefulSet.getSpec().setReplicas(newReplicas);
        statefulSet.getSpec().getUpdateStrategy().getRollingUpdate().setPartition(newReplicas);

        // 应用更新
        Resource<StatefulSet> statefulSetResource = client.apps().statefulSets()
            .inNamespace(args.getContextName())
            .withName(K8sNameUtil.getPodName(args.getContextName(), k8sNode.getClusterEnv()));

        statefulSetResource.patch(statefulSet);

        log.debug("StatefulSet 副本数已更新为: {}", newReplicas);
      } else {
        log.debug("StatefulSet 副本数已经是: {}，无需更新", newReplicas);
      }
    }
  }

  @Override
  public void updateStatefulSetImage(K8sUpdateStatefulSetImageArgs args) {
    KitValidUtil.validate(args);

    K8sNodeTb k8sNode = k8sNodeService.getByClusterCode(args.getClusterCode());

    try (KubernetesClient client = k8sClientRepository.getClient(args.getClusterCode())) {
      StatefulSet statefulSet = client.apps().statefulSets()
          .inNamespace(args.getContextName())
          .withName(K8sNameUtil.getPodName(args.getContextName(), k8sNode.getClusterEnv()))
          .get();
      if (statefulSet == null) {
        throw new BadRequestException("statefulset not found");
      }

      // 修改容器镜像
      boolean imageUpdated = false;
      String newImage = args.getImageUrl();
      String bizContainerName = K8sNameUtil.getPodBizName(args.getContextName(), k8sNode.getClusterEnv());
      for (Container container : statefulSet.getSpec().getTemplate().getSpec().getContainers()) {
        if (container.getName().equals(bizContainerName)) {
          container.setImage(newImage);
          imageUpdated = true;
          log.debug("StatefulSet: {}, 容器镜像已更新为: {}", statefulSet.getMetadata().getName(), newImage);
          break;
        }
      }

      if (!imageUpdated) {
        throw new BadRequestException("容器名称 " + bizContainerName + " 未找到");
      }

      // 更新所有pod
      statefulSet.getSpec().getUpdateStrategy().getRollingUpdate().setPartition(0);

      // 应用更新
      Resource<StatefulSet> statefulSetResource = client.apps().statefulSets()
          .inNamespace(args.getContextName())
          .withName(K8sNameUtil.getPodName(args.getContextName(), k8sNode.getClusterEnv()));

      statefulSetResource.patch(statefulSet);

      log.debug("StatefulSet: {}, 镜像更新成功", statefulSet.getMetadata().getName());
    }
  }

  public void updateStatefulSetImageWithStrategy(K8sUpdateStatefulSetImageArgs imageArgs, K8sUpdateStatefulSetStrategyArgs strategyArgs) {
    KitValidUtil.validate(imageArgs);

    K8sNodeTb k8sNode = k8sNodeService.getByClusterCode(imageArgs.getClusterCode());

    try (KubernetesClient client = k8sClientRepository.getClient(imageArgs.getClusterCode())) {
      StatefulSet statefulSet = client.apps().statefulSets()
          .inNamespace(imageArgs.getContextName())
          .withName(K8sNameUtil.getPodName(imageArgs.getContextName(), k8sNode.getClusterEnv()))
          .get();
      if (statefulSet == null) {
        throw new BadRequestException("statefulset not found");
      }

      // 修改容器镜像
      boolean imageUpdated = false;
      String newImage = imageArgs.getImageUrl();
      String bizContainerName = K8sNameUtil.getPodBizName(imageArgs.getContextName(), k8sNode.getClusterEnv());
      for (Container container : statefulSet.getSpec().getTemplate().getSpec().getContainers()) {
        if (container.getName().equals(bizContainerName)) {
          container.setImage(newImage);
          imageUpdated = true;
          log.debug("StatefulSet: {}, 容器镜像已更新为: {}", statefulSet.getMetadata().getName(), newImage);
          break;
        }
      }

      if (!imageUpdated) {
        throw new BadRequestException("容器名称 " + bizContainerName + " 未找到");
      }

      // 更新所有pod
      statefulSet.getSpec().getUpdateStrategy().getRollingUpdate().setPartition(0);

      // 应用更新
      Resource<StatefulSet> statefulSetResource = client.apps().statefulSets()
          .inNamespace(imageArgs.getContextName())
          .withName(K8sNameUtil.getPodName(imageArgs.getContextName(), k8sNode.getClusterEnv()));

      statefulSetResource.patch(statefulSet);

      log.debug("StatefulSet: {}, 镜像更新成功", statefulSet.getMetadata().getName());
    }
  }

  /**
   * 创建反亲和性配置
   */
  private Affinity createAffinity(String appName) {
    LabelSelectorRequirement labelRequirement = new LabelSelectorRequirementBuilder()
        .withKey(K8sLabelEnum.APP.getCode())
        .withOperator("In")
        .withValues(appName)
        .build();

    LabelSelector labelSelector = new LabelSelectorBuilder()
        .withMatchExpressions(labelRequirement)
        .build();

    return new AffinityBuilder()
        .withPodAntiAffinity(new PodAntiAffinityBuilder()
            .withPreferredDuringSchedulingIgnoredDuringExecution(
                createWeightedPodAffinityTerm(labelSelector, "failure-domain.beta.kubernetes.io/zone", 50),
                createWeightedPodAffinityTerm(labelSelector, "kubernetes.io/hostname", 50)
            )
            .build())
        .build();
  }

  /**
   * 创建加权 Pod 亲和性项
   */
  private WeightedPodAffinityTerm createWeightedPodAffinityTerm(LabelSelector labelSelector, String topologyKey, int weight) {
    return new WeightedPodAffinityTermBuilder()
        .withWeight(weight)
        .withPodAffinityTerm(new PodAffinityTermBuilder()
            .withLabelSelector(labelSelector)
            .withTopologyKey(topologyKey)
            .build())
        .build();
  }

  /**
   * 创建容器
   */
  private Container createContainer(String appName, String env, String image, Integer port, String cpu, String memory, boolean openLivenessCheck) {
    ContainerBuilder containerBuilder = new ContainerBuilder()
        .withName(K8sNameUtil.getPodBizName(appName, env))
        .withImage(image)
        .withImagePullPolicy("Always")
        .withResources(this.createResources(cpu, memory))
        .withVolumeMounts(this.createVolumeMounts(appName));
    // 基础系统镜像，可能拉起来就退出了，特殊处理下
    if (image.contains("linux") || image.contains("centos") || image.contains("windows") || image.contains("ubuntu")) {
      containerBuilder.addToCommand("sh");
      containerBuilder.addToArgs("-c", "while true; do sleep 30d; done");
    }
    // 是否是应用镜像
    if (image.contains(appName)) {
      containerBuilder.withLifecycle(this.createLifecycle());
    }
    // 是否启用健康检查
    if (openLivenessCheck) {
      containerBuilder.withReadinessProbe(this.createReadinessProbe(port));
      containerBuilder.withLivenessProbe(this.createLivenessProbe(port));
    }
    return containerBuilder.build();
  }

  /**
   * 创建生命周期钩子
   */
  private Lifecycle createLifecycle() {
    return new LifecycleBuilder()
        .withPreStop(new LifecycleHandlerBuilder()
            .withExec(new ExecActionBuilder()
                .withCommand("/bin/sh", "-c", "/home/admin/stop.sh")
                .build())
            .build())
        .build();
  }

  /**
   * 创建存活探针
   */
  private Probe createLivenessProbe(Integer port) {
    return new ProbeBuilder()
        .withFailureThreshold(3)
        .withHttpGet(new HTTPGetActionBuilder()
            .withPath("/health/check")
            .withNewPort(port)
            .build())
        .withInitialDelaySeconds(30)
        .withPeriodSeconds(10)
        .withTimeoutSeconds(5)
        .build();
  }

  /**
   * 创建就绪探针
   */
  private Probe createReadinessProbe(Integer port) {
    return new ProbeBuilder()
        .withFailureThreshold(3)
        .withHttpGet(new HTTPGetActionBuilder()
            .withPath("/health/check")
            .withNewPort(port)
            .build())
        .withInitialDelaySeconds(30)
        .withPeriodSeconds(10)
        .withSuccessThreshold(1)
        .withTimeoutSeconds(1)
        .build();
  }

  /**
   * 创建资源配置
   */
  private ResourceRequirements createResources(String cpu, String memory) {
    return new ResourceRequirementsBuilder()
        .addToLimits("cpu", new Quantity(cpu))
        .addToLimits("memory", new Quantity(memory))
        .addToRequests("cpu", new Quantity(cpu))
        .addToRequests("memory", new Quantity(memory))
        .build();
  }

  /**
   * 创建卷挂载
   */
  private List<VolumeMount> createVolumeMounts(String appName) {
    return List.of(
        new VolumeMountBuilder()
            .withMountPath("/home/admin/logs")
            .withName(appName + "-volume")
            .withSubPath("logs")
            .build()
    );
  }

  /**
   * 创建存储卷
   */
  private List<Volume> createVolume(String appName, String volumeSize) {
    return List.of(
        new VolumeBuilder()
            .withName(appName + "-volume")
            .withEmptyDir(new EmptyDirVolumeSourceBuilder()
                .withSizeLimit(new Quantity(volumeSize))
                .build())
            .build()
    );
  }

  /**
   * 创建更新策略
   */
  private StatefulSetUpdateStrategy createUpdateStrategy(Integer maxUnavailable, Integer partition) {
    return new StatefulSetUpdateStrategyBuilder()
        .withType("RollingUpdate")
        .withRollingUpdate(new RollingUpdateStatefulSetStrategyBuilder()
            .withMaxUnavailable(new IntOrString(maxUnavailable))
            // 分区编号（partition）：指定了一个序号，只有序号大于或等于该分区编号的 Pod 才会被更新
            // 如果 partition = N，那么只有序号 >= N 的 Pod 会被更新
            // 如果 partition = 0，则所有 Pod 都会被更新（相当于没有分区）
            // 如果 partition > 副本总数，则不会有任何 Pod 被更新
            .withPartition(partition)
            .build())
        .build();
  }

  @Override
  public StatefulSet applyStatefulSet(String clusterCode, String yamlContent) {
    try (KubernetesClient client = k8sClientRepository.getClient(clusterCode)) {
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
}
