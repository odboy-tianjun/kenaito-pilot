package cn.odboy.pipeline.service;

import cn.odboy.pipeline.constant.TaskStatusEnum;
import cn.odboy.pipeline.core.TaskContext;
import cn.odboy.pipeline.core.TaskOperation;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.core.NodeDefinition;
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

  void instanceInitWithNodeList(List<NodeDefinition> nodes, List<TaskOperation> operations, TaskContext context);

  void checkExist(TaskContext context);
}
