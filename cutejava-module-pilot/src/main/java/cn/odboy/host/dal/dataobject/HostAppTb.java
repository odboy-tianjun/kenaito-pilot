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
package cn.odboy.host.dal.dataobject;

import cn.odboy.base.KitBaseUserCreateTimeTb;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 主机、应用关联关系
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("host_app")
@ApiModel(value = "HostAppTb对象", description = "主机、应用关联关系")
public class HostAppTb extends KitBaseUserCreateTimeTb {

  /**
   * id
   */
  @ApiModelProperty("id")
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 主机id
   */
  @TableField("host_id")
  @ApiModelProperty("主机id")
  private Long hostId;

  /**
   * 主机集群id
   */
  @ApiModelProperty("主机集群id")
  @TableField("cluster_id")
  private Long clusterId;

  /**
   * 应用名称
   */
  @TableField("app_name")
  @ApiModelProperty("应用名称")
  private String appName;
}
