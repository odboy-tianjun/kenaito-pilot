package cn.odboy.pipeline.service.impl;

import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.pipeline.constant.PipelineStatusEnum;
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
  public void updateInstanceStatus(PipelineStatusEnum pipelineStatusEnum, TaskContext context) {
    LambdaUpdateWrapper<PipelineInstanceTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceTb::getId, context.getTaskId());
    wrapper.set(PipelineInstanceTb::getStatus, pipelineStatusEnum.getCode());
    wrapper.set(PipelineInstanceTb::getContextJson, JSON.toJSONString(context));
    update(wrapper);
  }

  @Override
  public void updateInstanceNodeStatus(PipelineStatusEnum pipelineStatusEnum, TaskOperation operation, TaskContext context, String executeInfo) {
    String code = operation.getClass().getAnnotation(Service.class).value();

    LambdaUpdateWrapper<PipelineInstanceTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceTb::getId, context.getTaskId());
    wrapper.set(PipelineInstanceTb::getCurrentNodeCode, code);
    wrapper.set(PipelineInstanceTb::getCurrentNodeStatus, pipelineStatusEnum.getCode());
    wrapper.set(PipelineInstanceTb::getContextJson, JSON.toJSONString(context));
    update(wrapper);

    LambdaUpdateWrapper<PipelineInstanceNodeTb> wrapper2 = new LambdaUpdateWrapper<>();
    wrapper2.eq(PipelineInstanceNodeTb::getInstanceId, context.getTaskId());
    wrapper2.eq(PipelineInstanceNodeTb::getBizCode, code);
    wrapper2.set(PipelineInstanceNodeTb::getExecuteStatus, pipelineStatusEnum.getCode());
    if (PipelineStatusEnum.RUNNING.equals(pipelineStatusEnum)) {
      wrapper2.set(PipelineInstanceNodeTb::getStartTime, new Date());
      wrapper2.set(PipelineInstanceNodeTb::getExecuteInfo, executeInfo);
      pipelineInstanceNodeService.update(wrapper2);
      return;
    }
    if (!PipelineStatusEnum.PENDING.equals(pipelineStatusEnum)) {
      wrapper2.set(PipelineInstanceNodeTb::getFinishTime, new Date());
      wrapper2.set(PipelineInstanceNodeTb::getExecuteInfo, executeInfo);
      pipelineInstanceNodeService.update(wrapper2);
    }
  }

  @Override
  public void instanceInitWithNodeList(List<NodeDefinition> nodes, List<TaskOperation> operations, TaskContext context) {
    TaskOperation operationsFirst = operations.getFirst();
    String currentNodeCode = operationsFirst.getClass().getAnnotation(Service.class).value();

    PipelineInstanceTb pipelineInstance = new PipelineInstanceTb();
    pipelineInstance.setId(context.getTaskId());
    pipelineInstance.setClusterType(context.getClusterType());
    pipelineInstance.setClusterCode(context.getClusterCode());
    pipelineInstance.setContextType(context.getContextType());
    pipelineInstance.setContextName(context.getContextName());
    pipelineInstance.setCurrentNodeCode(currentNodeCode);
    pipelineInstance.setCurrentNodeStatus(PipelineStatusEnum.PENDING.getCode());
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
      pipelineInstanceNode.setExecuteInfo(PipelineStatusEnum.PENDING.getDesc());
      pipelineInstanceNode.setExecuteStatus(PipelineStatusEnum.PENDING.getCode());
      nodeTbs.add(pipelineInstanceNode);
    }
    pipelineInstanceNodeService.saveBatch(nodeTbs);
  }

  @Override
  public void checkExist(TaskContext context) {
    LambdaQueryWrapper<PipelineInstanceTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PipelineInstanceTb::getClusterType, context.getClusterType());
    wrapper.eq(PipelineInstanceTb::getClusterCode, context.getClusterCode());
    wrapper.eq(PipelineInstanceTb::getContextType, context.getContextType());
    wrapper.eq(PipelineInstanceTb::getContextName, context.getContextName());

    List<String> status = new ArrayList<>();
    status.add(PipelineStatusEnum.PENDING.getCode());
    status.add(PipelineStatusEnum.RUNNING.getCode());
    wrapper.in(PipelineInstanceTb::getStatus, status);

    boolean exists = exists(wrapper);
    if (exists) {
      throw new BadRequestException("已有相同变更执行中");
    }
  }
}
