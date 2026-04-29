package cn.odboy.pipeline.service;

import cn.odboy.pipeline.constant.TaskStatusEnum;
import cn.odboy.pipeline.core.NodeDefinition;
import cn.odboy.pipeline.core.TaskContext;
import cn.odboy.pipeline.core.TaskOperation;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 流水线实例服务 职责： 1. 实例的 CRUD 操作 2. 状态管理 3. 冲突检测
 */
public interface PipelineInstanceService extends IService<PipelineInstanceTb> {

  /**
   * 更新实例状态
   */
  void updateInstanceStatus(TaskStatusEnum taskStatusEnum, TaskContext context);

  /**
   * 更新节点状态
   */
  void updateInstanceNodeStatus(
      TaskStatusEnum taskStatusEnum, TaskOperation operation,
      TaskContext context, String executeInfo
  );

  /**
   * 初始化实例和节点记录
   */
  void initInstanceNode(List<NodeDefinition> nodes, List<TaskOperation> operations, TaskContext context);

  /**
   * 强制关闭实例（中断场景）
   */
  void forceCloseInstance(TaskContext context);

  /**
   * 查询失败的实例（用于启动恢复）
   */
  List<PipelineInstanceTb> listFailure();

  /**
   * 查询冲突的实例（相同上下文的运行中实例）
   */
  List<PipelineInstanceTb> listConflicting(TaskContext context);

  /**
   * 根据实例ID删除实例和节点记录
   */
  void removeByInstanceId(String instanceId);
}
