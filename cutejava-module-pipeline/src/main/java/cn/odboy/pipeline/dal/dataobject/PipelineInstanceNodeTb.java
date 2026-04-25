package cn.odboy.pipeline.dal.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
  private String instanceId;

  /**
   * 节点业务编码（对应@Service的code）
   */
  private String bizCode;

  /**
   * 节点业务名称
   */
  private String bizName;

  /**
   * 节点开始执行时间
   */
  private Date startTime;

  /**
   * 节点结束执行时间
   */
  private Date finishTime;

  /**
   * 节点执行参数（节点运行时接收的参数，JSON格式存储）
   */
  private String executeParams;

  /**
   * 节点执行信息（执行日志、结果描述、异常信息等）
   */
  private String executeInfo;

  /**
   * 节点执行状态：pending-待执行 running-执行中 success-执行成功 failure-失败/停止
   */
  private String executeStatus;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;
}
