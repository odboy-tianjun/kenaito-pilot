package cn.odboy.kubernetes.repository;

import cn.odboy.kubernetes.dal.model.K8sCreateStatefulSetArgs;
import cn.odboy.kubernetes.dal.model.K8sDeleteStatefulSetArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetImageArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetReplicasArgs;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;

public interface KubernetesStatefulSetRepository {

  /**
   * 创建 StatefulSet
   *
   * @param args 、
   * @return /
   */
  StatefulSet createStatefulSet(K8sCreateStatefulSetArgs args);

  /**
   * 删除 StatefulSet
   *
   * @param args 、
   */
  void deleteStatefulSet(K8sDeleteStatefulSetArgs args);

  /**
   * 更新 StatefulSet 副本数
   *
   * @param args /
   */
  void updateStatefulSetReplicas(K8sUpdateStatefulSetReplicasArgs args);

  /**
   * 更新 StatefulSet 镜像地址
   *
   * @param args /
   */
  void updateStatefulSetImage(K8sUpdateStatefulSetImageArgs args);
}
