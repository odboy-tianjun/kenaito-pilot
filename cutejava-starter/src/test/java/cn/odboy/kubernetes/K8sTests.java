/*
 * Copyright 2021-2026 Odboy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.odboy.kubernetes;

import cn.odboy.kubernetes.dal.model.K8sCreateIngressArgs;
import cn.odboy.kubernetes.dal.model.K8sCreateStatefulSetArgs;
import cn.odboy.kubernetes.dal.model.K8sDeleteStatefulSetArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetImageArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetReplicasArgs;
import cn.odboy.kubernetes.repository.K8sIngressRepository;
import cn.odboy.kubernetes.repository.K8sServiceRepository;
import cn.odboy.kubernetes.repository.K8sStatefulSetRepository;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class K8sTests {

  @Autowired
  private K8sStatefulSetRepository k8sStatefulSetRepository;
  @Autowired
  private K8sServiceRepository k8sServiceRepository;
  @Autowired
  private K8sIngressRepository k8sIngressRepository;

  /**
   * Test Pass
   */
  @Test
  public void testCreateStatefulSet() {
    K8sCreateStatefulSetArgs createStatefulSetArgs = new K8sCreateStatefulSetArgs();
    createStatefulSetArgs.setClusterCode("local_k8s_28");
    createStatefulSetArgs.setContextName("kenaito-pilot");
    createStatefulSetArgs.setImage("registry.cn-shanghai.aliyuncs.com/odboy/kenaito-cicd:system-alinux3");
    k8sStatefulSetRepository.createStatefulSet(createStatefulSetArgs);
  }

  /**
   * Test Pass
   */
  @Test
  public void testUpdateStatefulSetReplicas() {
    K8sUpdateStatefulSetReplicasArgs updateStatefulReplicasArgs = new K8sUpdateStatefulSetReplicasArgs();
    updateStatefulReplicasArgs.setClusterCode("local_k8s_28");
    updateStatefulReplicasArgs.setContextName("kenaito-pilot");
    updateStatefulReplicasArgs.setReplicas(4);
    k8sStatefulSetRepository.updateStatefulSetReplicas(updateStatefulReplicasArgs);
  }

  /**
   * Test Pass
   */
  @Test
  public void testUpdateStatefulSetImage() {
    K8sUpdateStatefulSetImageArgs updateStatefulImageArgs = new K8sUpdateStatefulSetImageArgs();
    updateStatefulImageArgs.setClusterCode("local_k8s_28");
    updateStatefulImageArgs.setContextName("kenaito-pilot");
    updateStatefulImageArgs.setImageUrl("registry.cn-shanghai.aliyuncs.com/odboy/kenaito-cicd:system-alinux3-git");
    k8sStatefulSetRepository.updateStatefulSetImage(updateStatefulImageArgs);
  }

  /**
   * Test Pass
   */
  @Test
  public void testDeleteStatefulSet() {
    K8sDeleteStatefulSetArgs deleteStatefulSetArgs = new K8sDeleteStatefulSetArgs();
    deleteStatefulSetArgs.setClusterCode("local_k8s_28");
    deleteStatefulSetArgs.setContextName("kenaito-pilot");
    k8sStatefulSetRepository.deleteStatefulSet(deleteStatefulSetArgs);
  }

  /**
   * Test Pass
   */
  @Test
  public void testFull() {
    String clusterCode = "local_k8s_28";
    String contextName = "kenaito-pilot";
    String imageName = "registry.cn-shanghai.aliyuncs.com/odboy/kenaito-cicd:system-alinux3";
    Integer replicas = 3;
    Integer servicePort = 22;

    // 创建StatefulSet
    K8sCreateStatefulSetArgs createStatefulSetArgs = new K8sCreateStatefulSetArgs();
    createStatefulSetArgs.setClusterCode(clusterCode);
    createStatefulSetArgs.setContextName(contextName);
    createStatefulSetArgs.setImage(imageName);
    createStatefulSetArgs.setReplicas(replicas);
    StatefulSet statefulSet = k8sStatefulSetRepository.createStatefulSet(createStatefulSetArgs);
    System.err.println("=================== StatefulSet ==============================");
    System.err.println(statefulSet);
    // 创建Service
    Service service = k8sServiceRepository.createClusterIPService(clusterCode, contextName, servicePort);
    System.err.println("=================== Service ==============================");
    System.err.println(service);
    // 创建ingress
    K8sCreateIngressArgs createIngressArgs = new K8sCreateIngressArgs();
    createIngressArgs.setClusterCode(clusterCode);
    createIngressArgs.setContextName(contextName);
    createIngressArgs.setHost("kenaito-pilot-api.com");
    createIngressArgs.setPath("/");
    createIngressArgs.setServiceName(service.getMetadata().getName());
    Ingress ingress = k8sIngressRepository.createIngress(createIngressArgs);
    System.err.println("=================== Ingress ==============================");
    System.err.println(ingress);
  }
}
