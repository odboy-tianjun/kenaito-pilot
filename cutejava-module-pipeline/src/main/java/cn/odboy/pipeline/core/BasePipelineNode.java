package cn.odboy.pipeline.core;

import cn.odboy.pipeline.service.PipelineInstanceNodeService;
import cn.odboy.pipeline.service.PipelineInstanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

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

  public abstract void run(PipelineContext ctx) throws Exception;

  protected void checkInterrupted() throws InterruptedException {
    if (interrupted) {
      throw new InterruptedException("任务手动停止");
    }
  }

  @Override
  public void execute(JobExecutionContext ctx) throws JobExecutionException {
    this.instanceId = ctx.getJobDetail().getJobDataMap().getString("instanceId");
    this.context = (PipelineContext) ctx.getJobDetail().getJobDataMap().get("context");
    this.nodeCode = getClass().getAnnotation(Service.class).value();

    try {
      pipelineInstanceNodeService.updateStatusRunning(this.instanceId, this.nodeCode);
      run(context);
      pipelineInstanceNodeService.updateStatusSuccess(this.instanceId, this.nodeCode);
    } catch (InterruptedException e) {
      pipelineInstanceNodeService.updateStatusFailure(this.instanceId, this.nodeCode, "任务手动停止");
      pipelineInstanceService.updateStatusFailure(this.instanceId);
      throw new JobExecutionException(e);
    } catch (Exception e) {
      log.error("节点执行失败", e);
      pipelineInstanceNodeService.updateStatusFailure(this.instanceId, this.nodeCode, e.getMessage());
      pipelineInstanceService.updateStatusFailure(this.instanceId);
      throw new JobExecutionException(e);
    }
  }

  @Override
  public void interrupt() {
    this.interrupted = true;
  }
}
