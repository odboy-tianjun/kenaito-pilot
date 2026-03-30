package cn.odboy.kubernetes.initializer;

import cn.odboy.kubernetes.dal.model.K8sCreateStatefulSetArgs;
import cn.odboy.kubernetes.repository.KubernetesStatefulSetRepository;
import cn.odboy.meta.util.PilotNameUtil;
import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.AffinityBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
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
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KubernetesStatefulSetRepositoryImpl implements KubernetesStatefulSetRepository {

  @Override
  public StatefulSet createStatefulSet(K8sCreateStatefulSetArgs args) {
    try (KubernetesClient client = new KubernetesClientBuilder().build()) {
      // 构建 StatefulSet
      StatefulSet statefulSet = new StatefulSetBuilder()
          .withApiVersion("apps/v1")
          .withKind("StatefulSet")
          .withNewMetadata()
          .withName(PilotNameUtil.getPodName(args.getContextName(), args.getEnvCode()))
          .withNamespace(args.getContextName())
          .endMetadata()
          .withNewSpec()
          .withReplicas(args.getReplicas())
          .withPodManagementPolicy("OrderedReady")
          .withServiceName(PilotNameUtil.getServiceName(args.getContextName(), args.getEnvCode()))
          .withNewSelector()
          .addToMatchLabels("pilot-app", args.getContextName())
          .addToMatchLabels("pilot-env", args.getEnvCode())
          .endSelector()
          .withNewTemplate()
          .withNewMetadata()
          .addToLabels("pilot-app", args.getContextName())
          .addToLabels("pilot-env", args.getEnvCode())
          .endMetadata()
          .withNewSpec()
          .withAffinity(createAffinity(args.getContextName()))
          .withContainers(createContainer(args.getContextName(), args.getEnvCode(), args.getImage(), args.getPort(), args.getCpu(), args.getMemory()))
          .withDnsPolicy("ClusterFirst")
          .withRestartPolicy("Always")
          .withTerminationGracePeriodSeconds(30L)
          .withVolumes(createVolume(args.getContextName(), args.getVolumeSize()))
          .endSpec()
          .endTemplate()
          .withUpdateStrategy(createUpdateStrategy(args.getMaxUnavailable(), args.getPartition()))
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

  /**
   * 创建反亲和性配置
   */
  private Affinity createAffinity(String appName) {
    LabelSelectorRequirement labelRequirement = new LabelSelectorRequirementBuilder()
        .withKey("pilot-app")
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
  private Container createContainer(String appName, String env, String image, Integer port, String cpu, String memory) {
    return new ContainerBuilder()
        .withName(PilotNameUtil.getPodBizName(appName, env))
        .withImage(image)
        .withImagePullPolicy("Always")
        .withLifecycle(this.createLifecycle())
        .withLivenessProbe(this.createLivenessProbe(port))
        .withReadinessProbe(this.createReadinessProbe(port))
        .withResources(this.createResources(cpu, memory))
        .withVolumeMounts(this.createVolumeMounts(appName))
        .build();
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
            .withPartition(partition)
            .build())
        .build();
  }
}
