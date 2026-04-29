package cn.odboy.pipeline.v3.service.impl;

import cn.odboy.pipeline.v3.constant.TaskStatusEnum;
import cn.odboy.pipeline.v3.core.NodeDefinition;
import cn.odboy.pipeline.v3.core.TaskContext;
import cn.odboy.pipeline.v3.core.TaskOperation;
import cn.odboy.pipeline.v3.dal.dataobject.PipelineInstanceNodeTb;
import cn.odboy.pipeline.v3.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.v3.dal.mysql.PipelineInstanceMapper;
import cn.odboy.pipeline.v3.service.PipelineInstanceNodeService;
import cn.odboy.pipeline.v3.service.PipelineInstanceService;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 流水线实例服务实现类 职责： 1. 实例的 CRUD 操作 2. 状态管理 3. 冲突检测 4. 节点记录管理
 */
@Slf4j
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
    wrapper.set(PipelineInstanceTb::getUpdateTime, new Date());
    update(wrapper);

    log.debug("更新实例状态成功, instanceId={}, status={}", context.getTaskId(), taskStatusEnum.getCode());
  }

  @Override
  public void updateInstanceNodeStatus(
      TaskStatusEnum taskStatusEnum, TaskOperation operation,
      TaskContext context, String executeInfo
  ) {
    String code = operation.getClass().getAnnotation(Service.class).value();

    // 更新实例当前节点状态
    LambdaUpdateWrapper<PipelineInstanceTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceTb::getId, context.getTaskId());
    wrapper.set(PipelineInstanceTb::getCurrentNodeCode, code);
    wrapper.set(PipelineInstanceTb::getCurrentNodeStatus, taskStatusEnum.getCode());
    wrapper.set(PipelineInstanceTb::getContextJson, JSON.toJSONString(context));
    wrapper.set(PipelineInstanceTb::getUpdateTime, new Date());
    update(wrapper);

    // 更新节点执行状态
    LambdaUpdateWrapper<PipelineInstanceNodeTb> nodeWrapper = new LambdaUpdateWrapper<>();
    nodeWrapper.eq(PipelineInstanceNodeTb::getInstanceId, context.getTaskId());
    nodeWrapper.eq(PipelineInstanceNodeTb::getBizCode, code);
    nodeWrapper.set(PipelineInstanceNodeTb::getExecuteStatus, taskStatusEnum.getCode());

    if (TaskStatusEnum.RUNNING.equals(taskStatusEnum)) {
      nodeWrapper.set(PipelineInstanceNodeTb::getStartTime, new Date());
      nodeWrapper.set(PipelineInstanceNodeTb::getExecuteInfo, executeInfo);
      pipelineInstanceNodeService.update(nodeWrapper);
      return;
    }

    if (!TaskStatusEnum.PENDING.equals(taskStatusEnum)) {
      nodeWrapper.set(PipelineInstanceNodeTb::getFinishTime, new Date());
      nodeWrapper.set(PipelineInstanceNodeTb::getExecuteInfo, executeInfo);
      pipelineInstanceNodeService.update(nodeWrapper);
    }

    log.debug(
        "更新节点状态成功, instanceId={}, nodeCode={}, status={}",
        context.getTaskId(), code, taskStatusEnum.getCode()
    );
  }

  @Override
  public void initInstanceNode(List<NodeDefinition> nodes, List<TaskOperation> operations, TaskContext context) {
    TaskOperation firstOperation = operations.getFirst();
    String currentNodeCode = firstOperation.getClass().getAnnotation(Service.class).value();

    // 创建实例记录
    PipelineInstanceTb pipelineInstance = new PipelineInstanceTb();
    pipelineInstance.setId(context.getTaskId());
    pipelineInstance.setClusterType(context.getClusterType());
    pipelineInstance.setClusterCode(context.getClusterCode());
    pipelineInstance.setContextType(context.getContextType());
    pipelineInstance.setContextName(context.getContextName());
    pipelineInstance.setCurrentNodeCode(currentNodeCode);
    pipelineInstance.setCurrentNodeStatus(TaskStatusEnum.PENDING.getCode());
    pipelineInstance.setStatus(TaskStatusEnum.PENDING.getCode());
    pipelineInstance.setContextJson(JSON.toJSONString(context));
    pipelineInstance.setNodeJson(JSON.toJSONString(nodes));
    pipelineInstance.setForceClose(false);
    pipelineInstance.setCreateTime(new Date());
    pipelineInstance.setUpdateTime(new Date());
    save(pipelineInstance);

    // 创建节点记录
    List<PipelineInstanceNodeTb> nodeRecords = new ArrayList<>();
    for (NodeDefinition node : nodes) {
      PipelineInstanceNodeTb nodeRecord = new PipelineInstanceNodeTb();
      nodeRecord.setInstanceId(context.getTaskId());
      nodeRecord.setBizCode(node.getCode());
      nodeRecord.setBizName(node.getName());
      nodeRecord.setStartTime(null);
      nodeRecord.setFinishTime(null);
      nodeRecord.setExecuteInfo(TaskStatusEnum.PENDING.getMessage());
      nodeRecord.setExecuteStatus(TaskStatusEnum.PENDING.getCode());
      nodeRecord.setCreateTime(new Date());
      nodeRecord.setUpdateTime(new Date());
      nodeRecords.add(nodeRecord);
    }
    pipelineInstanceNodeService.saveBatch(nodeRecords);

    log.info("初始化流水线实例成功, instanceId={}, 节点数={}", context.getTaskId(), nodes.size());
  }

  @Override
  public void forceCloseInstance(TaskContext context) {
    LambdaUpdateWrapper<PipelineInstanceTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceTb::getId, context.getTaskId());
    wrapper.set(PipelineInstanceTb::getForceClose, true);
    wrapper.set(PipelineInstanceTb::getStatus, TaskStatusEnum.FAILURE.getCode());
    wrapper.set(PipelineInstanceTb::getUpdateTime, new Date());
    update(wrapper);

    log.warn("强制关闭流水线实例, instanceId={}", context.getTaskId());
  }

  @Override
  public List<PipelineInstanceTb> listFailure() {
    LambdaQueryWrapper<PipelineInstanceTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.ne(PipelineInstanceTb::getStatus, TaskStatusEnum.SUCCESS.getCode());
    wrapper.eq(PipelineInstanceTb::getForceClose, false);
    wrapper.orderByDesc(PipelineInstanceTb::getCreateTime);
    return list(wrapper);
  }

  @Override
  public List<PipelineInstanceTb> listConflicting(TaskContext context) {
    LambdaQueryWrapper<PipelineInstanceTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PipelineInstanceTb::getClusterType, context.getClusterType());
    wrapper.eq(PipelineInstanceTb::getClusterCode, context.getClusterCode());
    wrapper.eq(PipelineInstanceTb::getContextType, context.getContextType());
    wrapper.eq(PipelineInstanceTb::getContextName, context.getContextName());
    wrapper.ne(PipelineInstanceTb::getStatus, TaskStatusEnum.SUCCESS.getCode());
    wrapper.ne(PipelineInstanceTb::getStatus, TaskStatusEnum.FAILURE.getCode());
    wrapper.eq(PipelineInstanceTb::getForceClose, false);
    wrapper.orderByDesc(PipelineInstanceTb::getCreateTime);
    return list(wrapper);
  }

  @Override
  public void removeByInstanceId(String instanceId) {
    // 删除节点记录
    LambdaQueryWrapper<PipelineInstanceNodeTb> nodeWrapper = new LambdaQueryWrapper<>();
    nodeWrapper.eq(PipelineInstanceNodeTb::getInstanceId, instanceId);
    pipelineInstanceNodeService.remove(nodeWrapper);

    log.debug("删除实例节点记录成功, instanceId={}", instanceId);
  }
}
