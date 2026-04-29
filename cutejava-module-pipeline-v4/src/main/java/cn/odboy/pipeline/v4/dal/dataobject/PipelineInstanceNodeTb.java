package cn.odboy.pipeline.v4.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 流水线节点实例表
 */
@Data
@TableName("pipeline_instance_node")
public class PipelineInstanceNodeTb {

  @TableId(type = IdType.ASSIGN_ID)
  private String id;

  /**
   * 所属流水线实例ID（关联pipeline_instance.id）
   */
  @TableField(value = "instance_id")
  private String instanceId;

  /**
   * 节点业务编码（对应@Service的code）
   */
  @TableField(value = "biz_code")
  private String bizCode;

  /**
   * 节点业务名称
   */
  @TableField(value = "biz_name")
  private String bizName;

  /**
   * 节点开始执行时间
   */
  @TableField(value = "start_time")
  private Date startTime;

  /**
   * 节点结束执行时间
   */
  @TableField(value = "finish_time")
  private Date finishTime;

  /**
   * 节点执行信息（执行日志、结果描述、异常信息等）
   */
  @TableField(value = "execute_info")
  private String executeInfo;
  /**
   * 节点执行状态：pending-待执行 running-执行中 success-执行成功 failure-失败/停止
   */
  @TableField(value = "execute_status")
  private String executeStatus;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;
}
