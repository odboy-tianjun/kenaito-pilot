package cn.odboy.pipeline.v2.service;

import cn.odboy.pipeline.v2.constant.TaskStatusEnum;
import cn.odboy.pipeline.v2.core.TaskContext;
import cn.odboy.pipeline.v2.core.TaskOperation;
import cn.odboy.pipeline.v2.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.v2.core.NodeDefinition;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 流水线实例 服务类
 * </p>
 *
 * @author odboy
 * @since 2026-04-25
 */
public interface PipelineInstanceService extends IService<PipelineInstanceTb> {

  void updateInstanceStatus(TaskStatusEnum taskStatusEnum, TaskContext context);

  void updateInstanceNodeStatus(TaskStatusEnum taskStatusEnum, TaskOperation operation, TaskContext context, String executeInfo);

  void initInstanceNode(List<NodeDefinition> nodes, List<TaskOperation> operations, TaskContext context);

  void forceCloseInstance(TaskContext context);

  List<PipelineInstanceTb> listFailure();
}
