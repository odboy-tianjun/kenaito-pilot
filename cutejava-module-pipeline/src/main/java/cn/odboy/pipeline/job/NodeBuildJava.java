package cn.odboy.pipeline.job;

import cn.hutool.core.thread.ThreadUtil;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.stereotype.Service;

/**
 * 构建java逻辑
 *
 * @author odboy
 * @date 2026-04-26
 */
@Service("node_build_java")
public class NodeBuildJava implements InterruptableJob {

  @Override
  public void interrupt() throws UnableToInterruptJobException {

  }

  @Override
  public void execute(JobExecutionContext ctx) throws JobExecutionException {
    String tags = ctx.getMergedJobDataMap().getString("tags");
    String job = ctx.getMergedJobDataMap().getString("job");
    String version = ctx.getMergedJobDataMap().getString("version");
    // 构建逻辑
    ThreadUtil.sleep(3000);
  }
}
