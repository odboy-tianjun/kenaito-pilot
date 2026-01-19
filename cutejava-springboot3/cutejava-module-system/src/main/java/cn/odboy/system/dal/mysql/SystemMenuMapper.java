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

import cn.odboy.system.dal.dataobject.SystemMenuTb;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 菜单 Mapper
 *
 * @author odboy
 */
@Mapper
public interface SystemMenuMapper extends BaseMapper<SystemMenuTb> {

  /**
   * 查询所有的菜单 -> TestPassed
   */
  default List<SystemMenuTb> listAll() {
    return selectList(null);
  }

  /**
   * 根据父菜单id统计菜单数量 -> TestPassed
   *
   * @param pid 父菜单id
   * @return /
   */
  default Long countMenuByPid(Long pid) {
    LambdaQueryWrapper<SystemMenuTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemMenuTb::getPid, pid);
    return selectCount(wrapper);
  }

  /**
   * 查询所有的根目录 -> TestPassed
   */
  default List<SystemMenuTb> listRootMenu() {
    LambdaQueryWrapper<SystemMenuTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.isNull(SystemMenuTb::getPid);
    wrapper.orderByAsc(SystemMenuTb::getMenuSort);
    return selectList(wrapper);
  }

  /**
   * 根据组件名称查询菜单是否存在 -> TestPassed
   *
   * @param componentName 组件名称
   * @return /
   */
  default boolean existMenuWithComponentName(String componentName) {
    LambdaQueryWrapper<SystemMenuTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemMenuTb::getComponentName, componentName);
    return exists(wrapper);
  }

  /**
   * 根据组件名称查询菜单是否存在 -> TestPassed
   *
   * @param componentName 组件名称
   * @return /
   */
  default boolean existMenuWithComponentNameNeSelf(String componentName, Long currentMenuId) {
    LambdaQueryWrapper<SystemMenuTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemMenuTb::getComponentName, componentName);
    wrapper.ne(SystemMenuTb::getId, currentMenuId);
    return exists(wrapper);
  }

  /**
   * 根据菜单标题查询菜单是否存在 -> TestPassed
   *
   * @param title 菜单标题
   * @return /
   */
  default boolean existMenuWithTitle(String title) {
    LambdaQueryWrapper<SystemMenuTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemMenuTb::getTitle, title);
    return exists(wrapper);
  }

  /**
   * 根据菜单标题查询菜单是否存在 -> TestPassed
   *
   * @param title 菜单标题
   * @return /
   */
  default boolean existMenuWithTitleNeSelf(String title, Long currentMenuId) {
    LambdaQueryWrapper<SystemMenuTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemMenuTb::getTitle, title);
    wrapper.ne(SystemMenuTb::getId, currentMenuId);
    return exists(wrapper);
  }

  /**
   * 根据菜单id更新子菜单数量
   *
   * @param menuId   菜单id
   * @param subCount 子菜单数量
   */
  default void updateMenuSubCntById(Long menuId, Long subCount) {
    LambdaUpdateWrapper<SystemMenuTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(SystemMenuTb::getId, menuId);
    wrapper.set(SystemMenuTb::getSubCount, subCount);
    update(null, wrapper);
  }
}
