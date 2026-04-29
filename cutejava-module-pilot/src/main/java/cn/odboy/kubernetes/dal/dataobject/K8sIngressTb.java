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
 * K8s Ingress 反向代理
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("k8s_ingress")
@ApiModel(value = "K8sIngressTb对象", description = "K8s Ingress 反向代理")
public class K8sIngressTb extends KitBaseUserTimeTb {

  /**
   * id
   */
  @ApiModelProperty("id")
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * Ingress名称。命名规则：{appName}-{envCode}-ing
   */
  @TableField("`name`")
  @ApiModelProperty("Ingress名称。命名规则：{appName}-{envCode}-ing")
  private String name;

  /**
   * Ingress命名空间
   */
  @TableField("`namespace`")
  @ApiModelProperty("Ingress命名空间")
  private String namespace;

  /**
   * 域名
   */
  @TableField("`host`")
  @ApiModelProperty("域名")
  private String host;

  /**
   * 匹配路径。默认为'/'
   */
  @TableField("`path`")
  @ApiModelProperty("匹配路径。默认为'/'")
  private String path;

  /**
   * Ingress路由规则中的路径类型(Exact、Prefix、ImplementationSpecific
   */
  @TableField("path_type")
  @ApiModelProperty("Ingress路由规则中的路径类型(Exact、Prefix、ImplementationSpecific")
  private String pathType;

  /**
   * 目标Service
   */
  @TableField("service_name")
  @ApiModelProperty("目标Service")
  private String serviceName;

  /**
   * 服务端口。指向Service的应用服务端口
   */
  @TableField("service_port")
  @ApiModelProperty("服务端口。指向Service的应用服务端口")
  private Integer servicePort;
}
