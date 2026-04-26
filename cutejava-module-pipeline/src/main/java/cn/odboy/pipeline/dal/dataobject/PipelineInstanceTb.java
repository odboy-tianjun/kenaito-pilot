package cn.odboy.pipeline.dal.dataobject;

import cn.odboy.pipeline.constant.PipelineStatusEnum;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 流水线实例表
 */
@Data
@TableName("pipeline_instance")
public class PipelineInstanceTb {

  /**
   * 实例ID（主键）
   */
  @TableId(type = IdType.ASSIGN_ID)
  private String id;

  /**
   * 创建人
   */
  @TableField(value = "create_by", fill = FieldFill.INSERT)
  private String createBy;

  /**
   * 创建时间
   */
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  /**
   * 修改时间
   */
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  /**
   * 流水线状态（参考PipelineStatusEnum） PENDING-待执行 RUNNING-执行中 SUCCESS-执行成功 FAIL-执行失败
   */
  @TableField("status")
  private String status;

  /**
   * 运行时入参（JSON格式）
   */
  @TableField("runtime_params")
  private String runtimeParams;

  /**
   * 当前执行到的节点编码 用于宕机恢复时定位执行断点
   */
  @TableField("current_node_code")
  private String currentNodeCode;

  /**
   * 当前执行到的节点状态
   */
  @TableField("current_node_status")
  private String currentNodeStatus;

  /**
   * 流水线上下文（JSON格式） 存储完整的PipelineContext，包含所有节点的入参/出参
   */
  @TableField("context_json")
  private String contextJson;

  /**
   * 节点上下文（JSON格式） 存储完整的节点定义数据
   */
  @TableField("node_json")
  private String nodeJson;

  // 初始化状态
  public PipelineInstanceTb() {
    this.status = PipelineStatusEnum.PENDING.getCode();
  }
}
