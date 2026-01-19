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

import cn.odboy.system.dal.dataobject.SystemRoleTb;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 角色 Mapper
 *
 * @author odboy
 */
@Mapper
public interface SystemRoleMapper extends BaseMapper<SystemRoleTb> {

  /**
   * 根据角色id查询角色级别列表
   *
   * @param roleIds 角色id
   * @return /
   */
  List<Integer> listRoleLevelByRoleIds(@Param("roleIds") List<Long> roleIds);

  /**
   * 查询角色名称是否存在
   *
   * @param name 角色名称
   * @return /
   */
  default boolean existRoleWithName(String name) {
    LambdaQueryWrapper<SystemRoleTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemRoleTb::getName, name);
    return exists(wrapper);
  }

  /**
   * 查询角色名称是否存在（排除本身）
   *
   * @param name 角色名称
   * @return /
   */
  default boolean existRoleWithNameNeSelf(String name, Long roleId) {
    LambdaQueryWrapper<SystemRoleTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemRoleTb::getName, name);
    wrapper.eq(SystemRoleTb::getId, roleId);
    return exists(wrapper);
  }
}
