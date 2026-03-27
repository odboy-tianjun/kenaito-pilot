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
package cn.odboy.kubernetes.dal.mysql;

import cn.odboy.kubernetes.dal.dataobject.K8sPodSpecsTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * K8s Pod 规格 Mapper 接口
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@Mapper
public interface K8sPodSpecsMapper extends BaseMapper<K8sPodSpecsTb> {

}
