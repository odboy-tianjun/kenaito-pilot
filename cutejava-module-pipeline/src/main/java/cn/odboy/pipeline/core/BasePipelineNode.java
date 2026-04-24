package cn.odboy.pipeline.core;

import cn.odboy.pipeline.constant.PipelineStatusEnum;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.dal.mysql.PipelineInstanceMapper;
import cn.odboy.pipeline.dal.mysql.PipelineInstanceNodeMapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;
import java.util.Date;

@Slf4j
public abstract class BasePipelineNode implements InterruptableJob {

  @Resource
  private PipelineInstanceNodeMapper nodeMapper;

  @Resource
  private PipelineInstanceMapper instanceMapper;

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
      updateNode(PipelineStatusEnum.RUNNING.getCode(), null, new Date(), null);
      run(context);
      updateNode(PipelineStatusEnum.SUCCESS.getCode(), null, null, new Date());
    } catch (InterruptedException e) {
      updateNode(PipelineStatusEnum.FAILURE.getCode(), "任务手动停止", null, new Date());
      updateInstance(PipelineStatusEnum.FAILURE.getCode());
      throw new JobExecutionException(e);
    } catch (Exception e) {
      log.error("节点执行失败", e);
      updateNode(PipelineStatusEnum.FAILURE.getCode(), e.getMessage(), null, new Date());
      updateInstance(PipelineStatusEnum.FAILURE.getCode());
      throw new JobExecutionException(e);
    }
  }

  private void updateNode(String status, String info, Date start, Date end) {
    PipelineInstanceNodeTb n = new PipelineInstanceNodeTb();
    n.setExecuteStatus(status);
    n.setExecuteInfo(info);
    n.setStartTime(start);
    n.setFinishTime(end);
    n.setUpdateTime(new Date());

    new LambdaUpdateChainWrapper<>(nodeMapper)
        .eq(PipelineInstanceNodeTb::getInstanceId, instanceId)
        .eq(PipelineInstanceNodeTb::getBizCode, nodeCode)
        .update(n);
  }

  private void updateInstance(String status) {
    PipelineInstanceTb inst = new PipelineInstanceTb();
    inst.setStatus(status);
    inst.setUpdateTime(new Date());
    new LambdaUpdateChainWrapper<>(instanceMapper)
        .eq(PipelineInstanceTb::getId, instanceId)
        .update(inst);
  }

  @Override
  public void interrupt() {
    this.interrupted = true;
  }
}
