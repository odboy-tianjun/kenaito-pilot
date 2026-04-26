package cn.odboy.pipeline.job;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.stereotype.Service;

/**
 * 初始化逻辑
 */
@Service("node_init")
public class NodeInit implements InterruptableJob {

  @Override
  public void interrupt() throws UnableToInterruptJobException {

  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

  }
}
