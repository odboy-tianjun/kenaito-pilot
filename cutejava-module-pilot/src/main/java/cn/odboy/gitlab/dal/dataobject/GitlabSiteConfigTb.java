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
package cn.odboy.gitlab.dal.dataobject;

import cn.odboy.base.KitBaseUserTimeTb;
import cn.odboy.base.KitObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * Gitlab站点配置
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("gitlab_site_config")
@ApiModel(value = "GitlabSiteConfigTb对象", description = "Gitlab站点配置")
public class GitlabSiteConfigTb extends KitBaseUserTimeTb {

  /**
   * id
   */
  @ApiModelProperty("id")
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * gitlab别名
   */
  @TableField("`name`")
  @ApiModelProperty("gitlab别名")
  private String name;

  /**
   * gitlab地址
   */
  @TableField("endpoint")
  @ApiModelProperty("gitlab地址")
  private String endpoint;

  /**
   * PersonAccessToken。用root用户的
   */
  @TableField("token")
  @ApiModelProperty("PersonAccessToken。用root用户的")
  private String token;

  /**
   * 默认分组名称。为空默认root
   */
  @TableField("default_group_name")
  @ApiModelProperty("默认分组名称。为空默认root")
  private String defaultGroupName;

  /**
   * 默认分组id(auto)
   */
  @TableField("default_group_id")
  @ApiModelProperty("默认分组id(auto)")
  private Long defaultGroupId;

  /**
   * 是否启用
   */
  @TableField("`status`")
  @ApiModelProperty("是否启用")
  private Boolean status;

  /**
   * 是否默认站点
   */
  @TableField("`master`")
  @ApiModelProperty("是否默认站点")
  private Boolean master;

  /**
   * 错误信息
   */
  @TableField("`error_message`")
  @ApiModelProperty("错误信息")
  private String errorMessage;

  @Getter
  @Setter
  public static class CreateArgs extends KitObject {

    @NotBlank(message = "Gitlab别名必填")
    private String name;
    @NotBlank(message = "Gitlab地址必填")
    private String endpoint;
    @NotBlank(message = "PersonAccessToken必填")
    private String token;
    private String defaultGroupName;
  }

  @Getter
  @Setter
  public static class UpdateArgs extends KitObject {

    @NotNull
    private Long id;
    @NotBlank(message = "Gitlab别名必填")
    private String name;
    @NotBlank(message = "Gitlab地址必填")
    private String endpoint;
    @NotBlank(message = "PersonAccessToken必填")
    private String token;
    private String defaultGroupName;
    private Boolean status;
    private Boolean master;
  }
}
