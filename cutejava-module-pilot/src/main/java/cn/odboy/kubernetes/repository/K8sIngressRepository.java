package cn.odboy.kubernetes.repository;

import cn.odboy.kubernetes.dal.model.K8sCreateIngressArgs;
import cn.odboy.kubernetes.dal.model.K8sDeleteIngressArgs;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;

public interface K8sIngressRepository {

  /**
   * 创建 Ingress
   *
   * @param args /
   * @return /
   */
  Ingress createIngress(K8sCreateIngressArgs args);

  /**
   * 删除 Ingress
   *
   * @param args /
   * @return /
   */
  boolean deleteIngress(K8sDeleteIngressArgs args);
}
