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
package cn.odboy.kubernetes.dal.dataobject;

import cn.odboy.base.KitBaseUserTimeTb;
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
 * K8s 节点
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("k8s_node")
@ApiModel(value = "K8sNodeTb对象", description = "K8s 节点")
public class K8sNodeTb extends KitBaseUserTimeTb {

  /**
   * id
   */
  @ApiModelProperty("id")
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 集群编码
   */
  @ApiModelProperty("集群编码")
  @TableField("cluster_code")
  private String clusterCode;

  /**
   * 集群名称
   */
  @ApiModelProperty("集群名称")
  @TableField("cluster_name")
  private String clusterName;

  /**
   * 集群环境
   */
  @ApiModelProperty("集群环境")
  @TableField("cluster_env")
  private String clusterEnv;

  /**
   * 集群IP地址
   */
  @TableField("cluster_ip")
  @ApiModelProperty("集群IP地址")
  private String clusterIp;

  /**
   * 集群版本
   */
  @TableField("cluster_version")
  @ApiModelProperty("集群版本")
  private String clusterVersion;

  /**
   * 集群配置
   */
  @ApiModelProperty("集群配置")
  @TableField("cluster_config")
  private String clusterConfig;

  /**
   * 应用配置
   */
  @ApiModelProperty("应用配置")
  @TableField("app_config")
  private String appConfig;

  /**
   * 默认应用镜像
   */
  @ApiModelProperty("默认应用镜像")
  @TableField("default_app_image")
  private String defaultAppImage;

  /**
   * 是否启用
   */
  @TableField("`status`")
  @ApiModelProperty("是否启用")
  private Boolean status;

  /**
   * 异常信息
   */
  @TableField("`error_message`")
  @ApiModelProperty("异常信息")
  private String errorMessage;
}
