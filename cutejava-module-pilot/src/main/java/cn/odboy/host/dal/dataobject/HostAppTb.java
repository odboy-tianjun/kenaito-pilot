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
 * @author codegen
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
   * 归属集群
   */
  @ApiModelProperty("归属集群")
  @TableField("cluster_code")
  private String clusterCode;

  /**
   * 应用名称
   */
  @TableField("app_name")
  @ApiModelProperty("应用名称")
  private String appName;
}
