package cn.odboy.kubernetes.repository;

import cn.odboy.kubernetes.dal.model.K8sCreateStatefulSetArgs;
import cn.odboy.kubernetes.dal.model.K8sDeleteStatefulSetArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetImageArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetReplicasArgs;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;

public interface K8sStatefulSetRepository {

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

  /**
   * 从 YAML 文件创建或更新 StatefulSet
   *
   * @param clusterCode 集群编码
   * @param yamlContent statefulset负载内容
   */
  StatefulSet applyStatefulSet(String clusterCode, String yamlContent);
}
