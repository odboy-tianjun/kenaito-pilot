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

import cn.odboy.kubernetes.dal.dataobject.K8sServiceTb;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * K8s Service 负载均衡与服务发现 服务类
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
public interface K8sServiceService extends IService<K8sServiceTb> {

}
