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
 * @author codegen
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
