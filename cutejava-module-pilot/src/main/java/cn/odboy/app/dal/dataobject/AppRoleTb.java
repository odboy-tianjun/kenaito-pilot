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


/**
 * <p>
 * 应用角色
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("app_role")
@ApiModel(value = "AppRoleTb对象", description = "应用角色")
public class AppRoleTb extends KitBaseUserTimeTb {

    /**
     * id
     */
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码
     */
    @TableField("role_code")
    @ApiModelProperty("角色编码")
    private String roleCode;

    /**
     * 角色名称
     */
    @TableField("role_name")
    @ApiModelProperty("角色名称")
    private String roleName;

    /**
     * 角色说明
     */
    @ApiModelProperty("角色说明")
    @TableField("role_remark")
    private String roleRemark;
}
