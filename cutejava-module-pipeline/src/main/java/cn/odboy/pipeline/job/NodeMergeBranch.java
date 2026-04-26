package cn.odboy.pipeline.job;

import cn.hutool.core.thread.ThreadUtil;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.stereotype.Service;

/**
 * 合并代码逻辑
 */
@Service("node_merge_branch")
public class NodeMergeBranch implements InterruptableJob {

  @Override
  public void interrupt() throws UnableToInterruptJobException {

  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    ThreadUtil.sleep(2000);
  }
}
