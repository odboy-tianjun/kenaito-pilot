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

import java.math.BigDecimal;

/**
 * <p>
 * K8s Pod 规格
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("k8s_pod_specs")
@ApiModel(value = "K8sPodSpecsTb对象", description = "K8s Pod 规格")
public class K8sPodSpecsTb extends KitBaseUserTimeTb {

  /**
   * id
   */
  @ApiModelProperty("id")
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * CPU核心数。毫核m
   */
  @TableField("cpu_size")
  @ApiModelProperty("CPU核心数。毫核m")
  private BigDecimal cpuSize;

  /**
   * 内存数。单位Gi
   */
  @TableField("memory_size")
  @ApiModelProperty("内存数。单位Gi")
  private Long memorySize;

  /**
   * 磁盘容量。单位Gi
   */
  @TableField("disk_size")
  @ApiModelProperty("磁盘容量。单位Gi")
  private Long diskSize;

  /**
   * 备注
   */
  @TableField("remark")
  @ApiModelProperty("备注")
  private String remark;

  /**
   * 是否启用
   */
  @TableField("`status`")
  @ApiModelProperty("是否启用")
  private Boolean status;
}
