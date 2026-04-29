package cn.odboy.pipeline.v3.core;

import cn.odboy.pipeline.v3.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.v3.service.PipelineInstanceService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 流水线恢复服务 职责： 1. 应用启动时扫描未完成的实例 2. 自动恢复执行（从失败的节点继续）
 */
@Slf4j
@Component
public class PipelineRecoveryService implements InitializingBean {

  private final PipelineInstanceService instanceService;
  private final TaskScheduler scheduler;

  public PipelineRecoveryService(
      PipelineInstanceService instanceService,
      TaskScheduler scheduler
  ) {
    this.instanceService = instanceService;
    this.scheduler = scheduler;
  }

  @Override
  public void afterPropertiesSet() {
    List<PipelineInstanceTb> failureInstances = instanceService.listFailure();

    if (failureInstances.isEmpty()) {
      log.info("未发现需要恢复的流水线实例");
      return;
    }

    log.info("发现 {} 个需要恢复的流水线实例，开始自动恢复...", failureInstances.size());

    int successCount = 0;
    int failCount = 0;

    for (PipelineInstanceTb instance : failureInstances) {
      try {
        recoverInstance(instance);
        successCount++;
        log.info(
            "恢复流水线实例成功, instanceId={}, currentNode={}",
            instance.getId(), instance.getCurrentNodeCode()
        );
      } catch (Exception e) {
        failCount++;
        log.error(
            "恢复流水线实例失败, instanceId={}, currentNode={}",
            instance.getId(), instance.getCurrentNodeCode(), e
        );
      }
    }

    log.info("流水线实例恢复完成，成功: {}, 失败: {}", successCount, failCount);
  }

  /**
   * 恢复单个实例 流程：解析数据 → 设置重试标识 → 触发调度
   */
  private void recoverInstance(PipelineInstanceTb instance) {
    // 1. 解析实例数据
    List<NodeDefinition> nodes = JSON.parseArray(instance.getNodeJson(), NodeDefinition.class);
    TaskContext context = JSON.parseObject(instance.getContextJson(), TaskContext.class);

    if (nodes == null || nodes.isEmpty()) {
      throw new IllegalArgumentException("实例节点数据为空, instanceId=" + instance.getId());
    }

    if (context == null) {
      throw new IllegalArgumentException("实例上下文数据为空, instanceId=" + instance.getId());
    }

    // 2. 设置重试标识（从当前节点继续执行）
    String currentNodeCode = instance.getCurrentNodeCode();
    if (currentNodeCode == null || currentNodeCode.isEmpty()) {
      log.warn("实例没有当前节点信息，将从第一个节点重新开始, instanceId={}", instance.getId());
      // 如果没有当前节点，从头开始执行
    } else {
      context.setRetryBizCode(currentNodeCode);
      log.info("实例将从节点 [{}] 继续执行, instanceId={}", currentNodeCode, instance.getId());
    }

    // 3. 重新触发调度
    try {
      scheduler.triggerJob(nodes, context);
      log.info("实例恢复调度触发成功, instanceId={}", instance.getId());
    } catch (SchedulerException e) {
      log.error("实例恢复调度触发失败, instanceId={}", instance.getId(), e);
      throw new RuntimeException("恢复调度失败", e);
    }
  }
}
