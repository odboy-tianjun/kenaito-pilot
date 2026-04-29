package cn.odboy.pipeline.service.impl;

import cn.odboy.pipeline.constant.TaskStatusEnum;
import cn.odboy.pipeline.core.TaskContext;
import cn.odboy.pipeline.core.TaskOperation;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.core.NodeDefinition;
import cn.odboy.pipeline.dal.mysql.PipelineInstanceMapper;
import cn.odboy.pipeline.service.PipelineInstanceNodeService;
import cn.odboy.pipeline.service.PipelineInstanceService;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 流水线实例 服务实现类
 * </p>
 *
 * @author odboy
 * @since 2026-04-25
 */
@Service
public class PipelineInstanceServiceImpl extends ServiceImpl<PipelineInstanceMapper, PipelineInstanceTb> implements PipelineInstanceService {

  @Autowired
  private PipelineInstanceNodeService pipelineInstanceNodeService;

  @Override
  public void updateInstanceStatus(TaskStatusEnum taskStatusEnum, TaskContext context) {
    LambdaUpdateWrapper<PipelineInstanceTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceTb::getId, context.getTaskId());
    wrapper.set(PipelineInstanceTb::getStatus, taskStatusEnum.getCode());
    wrapper.set(PipelineInstanceTb::getContextJson, JSON.toJSONString(context));
    update(wrapper);
  }

  @Override
  public void updateInstanceNodeStatus(TaskStatusEnum taskStatusEnum, TaskOperation operation, TaskContext context, String executeInfo) {
    String code = operation.getClass().getAnnotation(Service.class).value();

    LambdaUpdateWrapper<PipelineInstanceTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceTb::getId, context.getTaskId());
    wrapper.set(PipelineInstanceTb::getCurrentNodeCode, code);
    wrapper.set(PipelineInstanceTb::getCurrentNodeStatus, taskStatusEnum.getCode());
    wrapper.set(PipelineInstanceTb::getContextJson, JSON.toJSONString(context));
    update(wrapper);

    LambdaUpdateWrapper<PipelineInstanceNodeTb> wrapper2 = new LambdaUpdateWrapper<>();
    wrapper2.eq(PipelineInstanceNodeTb::getInstanceId, context.getTaskId());
    wrapper2.eq(PipelineInstanceNodeTb::getBizCode, code);
    wrapper2.set(PipelineInstanceNodeTb::getExecuteStatus, taskStatusEnum.getCode());
    if (TaskStatusEnum.RUNNING.equals(taskStatusEnum)) {
      wrapper2.set(PipelineInstanceNodeTb::getStartTime, new Date());
      wrapper2.set(PipelineInstanceNodeTb::getExecuteInfo, executeInfo);
      pipelineInstanceNodeService.update(wrapper2);
      return;
    }
    if (!TaskStatusEnum.PENDING.equals(taskStatusEnum)) {
      wrapper2.set(PipelineInstanceNodeTb::getFinishTime, new Date());
      wrapper2.set(PipelineInstanceNodeTb::getExecuteInfo, executeInfo);
      pipelineInstanceNodeService.update(wrapper2);
    }
  }

  @Override
  public void initInstanceNode(List<NodeDefinition> nodes, List<TaskOperation> operations, TaskContext context) {
    TaskOperation operationsFirst = operations.getFirst();
    String currentNodeCode = operationsFirst.getClass().getAnnotation(Service.class).value();

    PipelineInstanceTb pipelineInstance = new PipelineInstanceTb();
    pipelineInstance.setId(context.getTaskId());
    pipelineInstance.setClusterType(context.getClusterType());
    pipelineInstance.setClusterCode(context.getClusterCode());
    pipelineInstance.setContextType(context.getContextType());
    pipelineInstance.setContextName(context.getContextName());
    pipelineInstance.setCurrentNodeCode(currentNodeCode);
    pipelineInstance.setCurrentNodeStatus(TaskStatusEnum.PENDING.getCode());
    pipelineInstance.setContextJson(JSON.toJSONString(context.getData()));
    pipelineInstance.setNodeJson(JSON.toJSONString(nodes));
    save(pipelineInstance);

    List<PipelineInstanceNodeTb> nodeTbs = new ArrayList<>();
    for (NodeDefinition node : nodes) {
      PipelineInstanceNodeTb pipelineInstanceNode = new PipelineInstanceNodeTb();
      pipelineInstanceNode.setInstanceId(context.getTaskId());
      pipelineInstanceNode.setBizCode(node.getCode());
      pipelineInstanceNode.setBizName(node.getName());
      pipelineInstanceNode.setStartTime(null);
      pipelineInstanceNode.setFinishTime(null);
      pipelineInstanceNode.setExecuteInfo(TaskStatusEnum.PENDING.getMessage());
      pipelineInstanceNode.setExecuteStatus(TaskStatusEnum.PENDING.getCode());
      nodeTbs.add(pipelineInstanceNode);
    }
    pipelineInstanceNodeService.saveBatch(nodeTbs);
  }

  @Override
  public void forceCloseInstance(TaskContext context) {
    LambdaUpdateWrapper<PipelineInstanceTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceTb::getId, context.getTaskId());
    wrapper.set(PipelineInstanceTb::getForceClose, true);
    update(wrapper);
  }

  @Override
  public List<PipelineInstanceTb> listFailure() {
    LambdaQueryWrapper<PipelineInstanceTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.ne(PipelineInstanceTb::getStatus, TaskStatusEnum.SUCCESS.getCode());
    wrapper.eq(PipelineInstanceTb::getForceClose, false);
    return list(wrapper);
  }
}
