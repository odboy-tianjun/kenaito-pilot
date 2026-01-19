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
package cn.odboy.system.dal.mysql;

import cn.odboy.system.dal.dataobject.SystemJobTb;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职位 Mapper
 *
 * @author odboy
 */
@Mapper
public interface SystemJobMapper extends BaseMapper<SystemJobTb> {

  /**
   * 根据岗位名称查询岗位是否存在 -> TestPassed
   */
  default boolean existJobWithName(String name) {
    LambdaQueryWrapper<SystemJobTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemJobTb::getName, name);
    return exists(wrapper);
  }

  /**
   * 根据岗位名称和岗位id查询岗位是否存在 -> TestPassed
   */
  default boolean existJobWithNameNeSelf(String name, Long currentJobId) {
    LambdaQueryWrapper<SystemJobTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemJobTb::getName, name);
    wrapper.ne(SystemJobTb::getId, currentJobId);
    return exists(wrapper);
  }
}
