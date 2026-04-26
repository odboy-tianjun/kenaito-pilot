package cn.odboy.pipeline.job;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.stereotype.Service;

/**
 * 部署java逻辑
 *
 * @author odboy
 * @date 2026-04-26
 */
@Service("node_deploy_java")
public class NodeDeployJava implements InterruptableJob {

  @Override
  public void interrupt() throws UnableToInterruptJobException {

  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

  }
}
