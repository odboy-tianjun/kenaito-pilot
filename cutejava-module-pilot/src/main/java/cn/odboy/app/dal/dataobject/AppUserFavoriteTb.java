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
package cn.odboy.app.dal.dataobject;

import cn.odboy.base.KitObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 用户收藏的应用
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("app_user_favorite")
@ApiModel(value = "AppUserFavoriteTb对象", description = "用户收藏的应用")
public class AppUserFavoriteTb extends KitObject {

  /**
   * 用户名
   */
  @ApiModelProperty("用户名")
  @TableField(value = "username")
  private String username;

  /**
   * 应用名称
   */
  @ApiModelProperty("应用名称")
  @TableField(value = "app_name")
  private String appName;
}
