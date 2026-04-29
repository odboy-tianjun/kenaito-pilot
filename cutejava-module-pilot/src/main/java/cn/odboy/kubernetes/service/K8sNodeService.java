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
package cn.odboy.kubernetes.service;

import cn.odboy.kubernetes.dal.dataobject.K8sNodeTb;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * K8s 节点 服务类
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
public interface K8sNodeService extends IService<K8sNodeTb> {

  /**
   * 根据集群编码查询集群
   *
   * @param clusterCode 集群编码
   * @return /
   */
  K8sNodeTb getByClusterCode(String clusterCode);

  List<K8sNodeTb> listEnable();

}
