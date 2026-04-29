package cn.odboy.pipeline.v3.dal.dataobject;

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
 * 流水线模板
 * </p>
 *
 * @author odboy
 * @since 2026-04-25
 */
@Getter
@Setter
@ToString
@TableName("pipeline_template")
@ApiModel(value = "PipelineTemplateTb对象", description = "流水线模板")
public class PipelineTemplateTb extends KitBaseUserTimeTb {

  /**
   * id
   */
  @ApiModelProperty("id")
  @TableId(value = "id", type = IdType.ASSIGN_ID)
  private String id;

  /**
   * 流水线名称
   */
  @ApiModelProperty("流水线名称")
  @TableField("pipeline_name")
  private String pipelineName;

  /**
   * 流水线环境(daily、stage、production)
   */
  @TableField("pipeline_env")
  @ApiModelProperty("流水线环境(daily、stage、production)")
  private String pipelineEnv;

  /**
   * 适用类型(front、backend)
   */
  @TableField("pipeline_type")
  @ApiModelProperty("适用类型(front、backend)")
  private String pipelineType;

  /**
   * 适用语言(java、php、nodejs、go、python、dotnet、cplus、移动端等)
   */
  @TableField("pipeline_language")
  @ApiModelProperty("适用语言(java、php、nodejs、go、python、dotnet、cplus、移动端等)")
  private String pipelineLanguage;

  /**
   * 模板内容
   */
  @TableField("template")
  @ApiModelProperty("模板内容")
  private String template;
}
