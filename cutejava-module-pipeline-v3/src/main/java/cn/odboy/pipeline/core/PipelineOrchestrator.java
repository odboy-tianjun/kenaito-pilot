package cn.odboy.pipeline.core;

import cn.hutool.core.collection.CollUtil;
import cn.odboy.framework.context.KitSpringBeanHolder;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.service.PipelineInstanceService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 流水线编排器 职责： 1. 对外提供统一的流水线执行入口 2. 协调调度器和执行引擎 3. 管理实例生命周期
 */
@Slf4j
@Component
public class PipelineOrchestrator {

  @Autowired
  private PipelineInstanceService instanceService;
  @Autowired
  private TaskScheduler scheduler;

  /**
   * 执行流水线（对外统一入口） 流程：准备环境 -> 触发调度 -> 异步执行
   */
  public void executePipeline(List<NodeDefinition> nodes, TaskContext context) {
    if (CollUtil.isEmpty(nodes)) {
      throw new BadRequestException("任务操作列表不能为空");
    }
    if (context == null) {
      throw new BadRequestException("任务上下文不能为空");
    }

    // 1. 准备执行环境（停止旧实例、初始化记录、构建操作链）
    prepareExecution(nodes, context);

    // 2. 触发调度（异步执行）
    try {
      scheduler.triggerJob(nodes, context);
      log.info("流水线Job已触发，实例ID: {}", context.getTaskId());
    } catch (SchedulerException e) {
      log.error("触发Job失败，实例ID: {}", context.getTaskId(), e);
      throw new RuntimeException("触发流水线执行失败", e);
    }
  }

  /**
   * 重试流水线（对外统一入口）
   */
  public void retryPipeline(String instanceId, String retryBizCode) {
    PipelineInstanceTb instance = instanceService.getById(instanceId);
    if (instance == null) {
      throw new BadRequestException("流水线实例不存在");
    }

    List<NodeDefinition> nodes = JSON.parseArray(instance.getNodeJson(), NodeDefinition.class);
    TaskContext context = JSON.parseObject(instance.getContextJson(), TaskContext.class);
    context.setRetryBizCode(retryBizCode);

    // 触发调度（Job内部会识别重试标识）
    try {
      scheduler.triggerJob(nodes, context);
      log.info("流水线重试Job已触发，实例ID: {}", instanceId);
    } catch (SchedulerException e) {
      log.error("触发重试Job失败，实例ID: {}", instanceId, e);
      throw new RuntimeException("触发重试失败", e);
    }
  }

  /**
   * 准备执行环境（内部方法） - 停止相同上下文的运行中实例 - 初始化新的实例记录 - 构建操作链
   */
  private void prepareExecution(List<NodeDefinition> nodes, TaskContext context) {
    // 停止同类变更
    stopConflictingInstances(context);

    // 构建操作链
    List<TaskOperation> operations = buildOperations(nodes);

    // 初始化实例记录
    instanceService.initInstanceNode(nodes, operations, context);
  }

  // ==================== 私有方法 ====================

  /**
   * 停止冲突的实例
   */
  private void stopConflictingInstances(TaskContext context) {
    List<PipelineInstanceTb> conflictingInstances = instanceService.listConflicting(context);
    for (PipelineInstanceTb instance : conflictingInstances) {
      try {
        TaskContext oldContext = JSON.parseObject(instance.getContextJson(), TaskContext.class);
        if (oldContext != null) {
          scheduler.stopJob(oldContext);
        }
        instanceService.removeByInstanceId(instance.getId());
        instanceService.removeById(instance.getId());
      } catch (Exception e) {
        log.warn("停止冲突实例失败, instanceId={}", instance.getId(), e);
      }
    }
  }

  /**
   * 构建操作链
   */
  private List<TaskOperation> buildOperations(List<NodeDefinition> nodes) {
    List<TaskOperation> operations = new ArrayList<>();
    for (NodeDefinition node : nodes) {
      TaskOperation operation = KitSpringBeanHolder.getBean(node.getCode());
      if (operation == null) {
        throw new BadRequestException("未找到节点操作实现: " + node.getCode());
      }
      operations.add(operation);
    }
    return operations;
  }
}
