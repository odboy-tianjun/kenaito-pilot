package cn.odboy.pipeline;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 流水线节点基类
 * 所有具体的流水线节点都需要继承此类
 */
@Slf4j
public abstract class BasePipelineNode implements InterruptableJob {

  @Resource
  private PipelineInstanceService pipelineInstanceService;

  @Resource
  private PipelineInstanceNodeService pipelineInstanceNodeService;

  protected volatile boolean interrupted = false;
  protected String instanceId;
  protected String nodeCode;
  protected PipelineContext context;

  /**
   * 节点具体执行逻辑（由子类实现）
   *
   * @param ctx 流水线上下文
   * @throws Exception 执行异常
   */
  public abstract void run(PipelineContext ctx) throws Exception;

  /**
   * 检查任务是否被中断
   *
   * @throws InterruptedException 中断异常
   */
  protected void checkInterrupted() throws InterruptedException {
    if (interrupted) {
      throw new InterruptedException("任务手动停止");
    }
  }

  /**
   * Quartz Job 执行入口
   *
   * @param ctx Job执行上下文
   * @throws JobExecutionException 执行异常
   */
  @Override
  public void execute(JobExecutionContext ctx) throws JobExecutionException {
    this.instanceId = ctx.getJobDetail().getJobDataMap().getString("instanceId");
    this.context = (PipelineContext) ctx.getJobDetail().getJobDataMap().get("context");
    this.nodeCode = getClass().getAnnotation(Service.class).value();

    try {
      // 更新节点状态为运行中
      pipelineInstanceNodeService.updateStatusRunning(instanceId, nodeCode);

      // 【修复】同步更新主表的当前节点信息
      updateCurrentNodeInfo(nodeCode, PipelineStatusEnum.RUNNING.getCode());

      // 执行节点业务逻辑
      run(context);

      // 更新节点状态为成功
      pipelineInstanceNodeService.updateStatusSuccess(instanceId, nodeCode);

      // 【修复】更新主表的当前节点状态为成功
      updateCurrentNodeInfo(nodeCode, PipelineStatusEnum.SUCCESS.getCode());

      // 检查所有节点是否都执行成功
      checkAllNodeSuccess();
    } catch (InterruptedException e) {
      // 任务被手动中断
      pipelineInstanceNodeService.updateStatusFailure(instanceId, nodeCode, "任务手动停止");
      pipelineInstanceService.updateStatusFailure(instanceId);
      throw new JobExecutionException(e);
    } catch (Exception e) {
      // 节点执行失败
      log.error("节点执行失败 instanceId={} nodeCode={}", instanceId, nodeCode, e);
      pipelineInstanceNodeService.updateStatusFailure(instanceId, nodeCode, e.getMessage());
      pipelineInstanceService.updateStatusFailure(instanceId);
      throw new JobExecutionException(e);
    }
  }

  /**
   * 检查所有节点是否都执行成功，如果全部成功则更新主表状态为SUCCESS
   */
  private void checkAllNodeSuccess() {
    List<PipelineInstanceNodeTb> nodeList = pipelineInstanceNodeService.listByInstanceId(instanceId);
    boolean allSuccess = nodeList.stream()
        .allMatch(node -> PipelineStatusEnum.SUCCESS.getCode().equals(node.getExecuteStatus()));

    if (allSuccess) {
      log.info("✅ 流水线全部执行完成：{}", instanceId);
      pipelineInstanceService.updateStatusSuccessByInstanceId(instanceId);
    }
  }

  /**
   * 更新主表的当前节点编码和状态
   *
   * @param nodeCode 节点编码
   * @param status 节点状态
   */
  private void updateCurrentNodeInfo(String nodeCode, String status) {
    pipelineInstanceService.updateCurrentNode(instanceId, nodeCode, status);
  }

  /**
   * Quartz 中断回调
   */
  @Override
  public void interrupt() {
    this.interrupted = true;
  }
}
