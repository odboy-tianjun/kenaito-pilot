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

import java.util.Date;

/**
 * <p>
 * 应用信息
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("app_info")
@ApiModel(value = "AppInfoTb对象", description = "应用信息")
public class AppInfoTb extends KitBaseUserTimeTb {

    /**
     * id
     */
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 应用名称
     */
    @TableField("app_name")
    @ApiModelProperty("应用名称")
    private String appName;

    /**
     * 开发语言
     */
    @ApiModelProperty("开发语言")
    @TableField("`language`")
    private String language;

    /**
     * 状态。0空应用1在线2已下线
     */
    @TableField("`status`")
    @ApiModelProperty("状态。0空应用1在线2已下线")
    private Integer status;

    /**
     * 下线时间
     */
    @ApiModelProperty("下线时间")
    @TableField("offline_time")
    private Date offlineTime;

    /**
     * 应用负责人
     */
    @TableField("`owner`")
    @ApiModelProperty("应用负责人")
    private String owner;

    /**
     * 应用负责人名称
     */
    @TableField("owner_name")
    @ApiModelProperty("应用负责人名称")
    private String ownerName;

    /**
     * 应用描述
     */
    @ApiModelProperty("应用描述")
    @TableField("`description`")
    private String description;

    /**
     * 应用级别。A核心应用 B非核心应用 C边缘应用
     */
    @TableField("`level`")
    @ApiModelProperty("应用级别。A核心应用 B非核心应用 C边缘应用")
    private String level;

    /**
     * 部署类型。static静态产物 backend 后端服务
     */
    @TableField("deploy_type")
    @ApiModelProperty("部署类型。static静态产物 backend 后端服务")
    private String deployType;

    /**
     * 归属gitlab站点
     */
    @TableField("gitlab_id")
    @ApiModelProperty("归属gitlab站点")
    private Long gitlabId;

    /**
     * git分组id
     */
    @TableField("git_group_id")
    @ApiModelProperty("git分组id")
    private Long gitGroupId;

    /**
     * git分组名称
     */
    @ApiModelProperty("git分组名称")
    @TableField("git_group_name")
    private String gitGroupName;

    /**
     * git项目id
     */
    @ApiModelProperty("git项目id")
    @TableField("git_project_id")
    private Long gitProjectId;

    /**
     * git项目名称
     */
    @ApiModelProperty("git项目名称")
    @TableField("git_project_name")
    private String gitProjectName;

    /**
     * git项目用户名
     */
    @ApiModelProperty("git项目用户名")
    @TableField("git_project_owner")
    private Long gitProjectOwner;

    /**
     * git项目地址
     */
    @TableField("git_web_url")
    @ApiModelProperty("git项目地址")
    private String gitWebUrl;
}
