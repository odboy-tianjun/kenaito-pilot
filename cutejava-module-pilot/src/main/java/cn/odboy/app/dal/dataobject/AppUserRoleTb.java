package cn.odboy.app.dal.dataobject;

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
 * 应用角色、用户关联
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@TableName("app_user_role")
@ApiModel(value = "AppUserRoleTb对象", description = "应用角色、用户关联")
public class AppUserRoleTb extends KitBaseUserCreateTimeTb {

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
   * 用户名
   */
  @TableField("username")
  @ApiModelProperty("用户名")
  private String username;
}
