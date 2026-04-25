package cn.odboy.pipeline.dal.dataobject;

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
 * pipeline上下文模版
 * </p>
 *
 * @author codegen
 * @since 2026-04-25
 */
@Getter
@Setter
@ToString
@TableName("pipeline_template_context")
@ApiModel(value = "PipelineTemplateContextTb对象", description = "pipeline上下文模版")
public class PipelineTemplateContextTb extends KitBaseUserCreateTimeTb {

  /**
   * id
   */
  @ApiModelProperty("id")
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 集群类型(k8s、host)
   */
  @TableField("cluster_type")
  @ApiModelProperty("集群类型(k8s、host)")
  private String clusterType;

  /**
   * 集群编码
   */
  @ApiModelProperty("集群编码")
  @TableField("cluster_code")
  private String clusterCode;

  /**
   * 上下文类型(deploy_app、deploy_database等)
   */
  @TableField("context_type")
  @ApiModelProperty("上下文类型(deploy_app、deploy_database等)")
  private String contextType;

  /**
   * 上下文名称(应用名称、数据库类型，比如mysql等)
   */
  @TableField("context_name")
  @ApiModelProperty("上下文名称(应用名称、数据库类型，比如mysql等)")
  private String contextName;

  /**
   * 流水线模板
   */
  @TableField("template")
  @ApiModelProperty("流水线模板")
  private String template;
}
