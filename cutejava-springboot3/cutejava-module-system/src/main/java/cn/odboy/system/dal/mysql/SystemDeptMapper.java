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

import cn.odboy.system.dal.dataobject.SystemDeptTb;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门 Mapper
 *
 * @author odboy
 */
@Mapper
public interface SystemDeptMapper extends BaseMapper<SystemDeptTb> {

  default void updateDeptSubCountById(long count, Long deptId) {
    LambdaUpdateWrapper<SystemDeptTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(SystemDeptTb::getId, deptId);
    wrapper.set(SystemDeptTb::getSubCount, count);
    update(null, wrapper);
  }

  /**
   * 根据父部门id统计数量
   *
   * @param pid 父部门id
   * @return /
   */
  default long countDeptByPid(Long pid) {
    LambdaQueryWrapper<SystemDeptTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemDeptTb::getPid, pid);
    return selectCount(wrapper);
  }

  /**
   * 获取所有部门id与部门的映射关系
   *
   * @return /
   */
  default Map<Long, SystemDeptTb> getAllDeptMap() {
    LambdaQueryWrapper<SystemDeptTb> wrapper = new LambdaQueryWrapper<>();
    return selectList(wrapper).stream().collect(Collectors.toMap(SystemDeptTb::getId, i -> i, (m, n) -> m));
  }

  /**
   * 查询所有根部门
   *
   * @return /
   */
  default List<SystemDeptTb> listRootDept() {
    LambdaQueryWrapper<SystemDeptTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.isNull(SystemDeptTb::getPid);
    return selectList(wrapper);
  }
}
