package cn.odboy.kubernetes.repository;

import cn.odboy.kubernetes.dal.model.K8sCreateStatefulSetArgs;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;

public interface KubernetesStatefulSetRepository {

  /**
   * 创建 StatefulSet
   *
   * @param args 、
   * @return /
   */
  StatefulSet createStatefulSet(K8sCreateStatefulSetArgs args);
}
