package cn.odboy.gitlab.dal.dataobject;

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
 * Gitlab站点配置
 * </p>
 *
 * @author codegen
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
}
