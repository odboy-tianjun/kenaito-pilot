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

import cn.hutool.core.io.resource.ResourceUtil;
import cn.odboy.constant.SystemConst;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * kubectl version Client Version: v1.34.1 Kustomize Version: v5.7.1 Server Version: v1.34.1
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KubernetesTests {

  /**
   * 创建 Namespace，如果已存在则返回现有对象
   */
  public Namespace createNamespace(KubernetesClient client, String namespaceName) {
    // 先检查是否存在
    Namespace existing = client.namespaces().withName(namespaceName).get();
    if (existing != null) {
      log.debug("Namespace already exists: {}", namespaceName);
      return existing;
    }

    // 不存在则创建
    Namespace namespace = new NamespaceBuilder()
        .withNewMetadata()
          .withName(namespaceName)
          .addToLabels("created-by", SystemConst.CURRENT_APP_NAME)
          .addToAnnotations("created-at", String.valueOf(System.currentTimeMillis()))
        .endMetadata().build();

    try {
      Namespace created = client.namespaces().resource(namespace).create();
      log.debug("Namespace created: {}", namespaceName);
      return created;
    } catch (KubernetesClientException e) {
      // 处理并发创建的情况
      if (e.getCode() == 409) {
        log.debug("Namespace was created by another process: {}", namespaceName);
        return client.namespaces().withName(namespaceName).get();
      }
      throw e;
    }
  }

  @Test
  public void testReadConfig() {
    String kubeConfigContent = ResourceUtil.readUtf8Str("classpath:kubernetes/config");
    Config config = Config.fromKubeconfig(kubeConfigContent);
    try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
      this.createNamespace(client, "test01");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
