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

import cn.odboy.kubernetes.dal.model.K8sCreateStatefulSetArgs;
import cn.odboy.kubernetes.dal.model.K8sDeleteStatefulSetArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetImageArgs;
import cn.odboy.kubernetes.dal.model.K8sUpdateStatefulSetReplicasArgs;
import cn.odboy.kubernetes.repository.KubernetesRepository;
import cn.odboy.kubernetes.repository.KubernetesStatefulSetRepository;
import io.fabric8.kubernetes.api.model.Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KubernetesTests {

  @Autowired
  private KubernetesRepository kubernetesRepository;
  @Autowired
  private KubernetesStatefulSetRepository kubernetesStatefulSetRepository;

  @Test
  public void test1() {
    Service service = kubernetesRepository.createClusterIPService("local_k8s_28", "kenaito-pilot", "daily", 8000);
    System.err.println(service);
  }

  @Test
  public void testCreateStatefulSet() {
    K8sCreateStatefulSetArgs createStatefulSetArgs = new K8sCreateStatefulSetArgs();
    createStatefulSetArgs.setClusterCode("local_k8s_31");
    createStatefulSetArgs.setContextName("kenaito-pilot");
    kubernetesStatefulSetRepository.createStatefulSet(createStatefulSetArgs);
  }

  @Test
  public void testUpdateStatefulSetReplicas() {
    K8sUpdateStatefulSetReplicasArgs updateStatefulReplicasArgs = new K8sUpdateStatefulSetReplicasArgs();
    updateStatefulReplicasArgs.setClusterCode("local_k8s_31");
    updateStatefulReplicasArgs.setContextName("kenaito-pilot");
    updateStatefulReplicasArgs.setReplicas(4);
    kubernetesStatefulSetRepository.updateStatefulSetReplicas(updateStatefulReplicasArgs);
  }

  @Test
  public void testUpdateStatefulSetImage() {
    K8sUpdateStatefulSetImageArgs updateStatefulImageArgs = new K8sUpdateStatefulSetImageArgs();
    updateStatefulImageArgs.setClusterCode("local_k8s_31");
    updateStatefulImageArgs.setContextName("kenaito-pilot");
    updateStatefulImageArgs.setImageUrl("registry.cn-shanghai.aliyuncs.com/odboy/kenaito-cicd:system-alinux3-git");
    kubernetesStatefulSetRepository.updateStatefulSetImage(updateStatefulImageArgs);
  }

  @Test
  public void testDeleteStatefulSet() {
    K8sDeleteStatefulSetArgs deleteStatefulSetArgs = new K8sDeleteStatefulSetArgs();
    deleteStatefulSetArgs.setClusterCode("local_k8s_31");
    deleteStatefulSetArgs.setContextName("kenaito-pilot");
    kubernetesStatefulSetRepository.deleteStatefulSet(deleteStatefulSetArgs);
  }
}
