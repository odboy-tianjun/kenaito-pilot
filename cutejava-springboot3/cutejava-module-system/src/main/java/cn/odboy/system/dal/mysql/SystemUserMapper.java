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

import cn.odboy.system.dal.dataobject.SystemUserTb;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Set;

/**
 * 用户 Mapper
 *
 * @author odboy
 */
@Mapper
public interface SystemUserMapper extends BaseMapper<SystemUserTb> {

  default void updateUserPasswordByUsername(String username, String password) {
    LambdaUpdateWrapper<SystemUserTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(SystemUserTb::getUsername, username);
    wrapper.set(SystemUserTb::getPassword, password);
    update(null, wrapper);
  }

  default void updateUserEmailByUsername(String username, String email) {
    LambdaUpdateWrapper<SystemUserTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(SystemUserTb::getUsername, username);
    wrapper.set(SystemUserTb::getEmail, email);
    update(null, wrapper);
  }

  default void updateUserPasswordByUserIds(String password, Set<Long> userIds) {
    LambdaUpdateWrapper<SystemUserTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.in(SystemUserTb::getId, userIds);
    wrapper.set(SystemUserTb::getPassword, password);
    update(null, wrapper);
  }

  /**
   * 查询手机号是否存在
   *
   * @param phone 手机号
   * @return /
   */
  default boolean existUserWithPhone(String phone) {
    LambdaQueryWrapper<SystemUserTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemUserTb::getPhone, phone);
    wrapper.eq(SystemUserTb::getEnabled, 1);
    return exists(wrapper);
  }

  /**
   * 查询邮箱是否存在
   *
   * @param email 邮箱
   * @return /
   */
  default boolean existUserWithEmail(String email) {
    LambdaQueryWrapper<SystemUserTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemUserTb::getEmail, email);
    wrapper.eq(SystemUserTb::getEnabled, 1);
    return exists(wrapper);
  }

  /**
   * 查询用户名是否存在
   *
   * @param username 用户名
   * @return /
   */
  default boolean existUserWithUsername(String username) {
    LambdaQueryWrapper<SystemUserTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemUserTb::getUsername, username);
    wrapper.eq(SystemUserTb::getEnabled, 1);
    return exists(wrapper);
  }

  /**
   * 查询手机号是否存在（排除指定用户）
   *
   * @param phone         手机号
   * @param currentUserId 指定用户id
   * @return /
   */
  default boolean existUserWithPhoneNeSelf(String phone, Long currentUserId) {
    LambdaQueryWrapper<SystemUserTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemUserTb::getPhone, phone);
    wrapper.ne(SystemUserTb::getId, currentUserId);
    return exists(wrapper);
  }

  /**
   * 查询邮箱是否存在（排除指定用户）
   *
   * @param email         邮箱
   * @param currentUserId 指定用户id
   * @return /
   */
  default boolean existUserWithEmailNeSelf(String email, Long currentUserId) {
    LambdaQueryWrapper<SystemUserTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemUserTb::getEmail, email);
    wrapper.ne(SystemUserTb::getId, currentUserId);
    return exists(wrapper);
  }

  /**
   * 查询用户名是否存在（排除指定用户）
   *
   * @param username      用户名
   * @param currentUserId 指定用户id
   * @return /
   */
  default boolean existUserWithUsernameNeSelf(String username, Long currentUserId) {
    LambdaQueryWrapper<SystemUserTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemUserTb::getUsername, username);
    wrapper.ne(SystemUserTb::getId, currentUserId);
    return exists(wrapper);
  }

  List<String> listUsernameByIds(@Param("userIds") Set<Long> userIds);
}
