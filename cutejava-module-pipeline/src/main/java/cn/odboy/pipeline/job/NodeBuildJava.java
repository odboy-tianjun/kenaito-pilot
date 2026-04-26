package cn.odboy.pipeline.job;

import cn.hutool.core.thread.ThreadUtil;
import cn.odboy.framework.server.core.ConfigurerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * 构建Java节点 - 演示如何实现一个流水线节点
 *
 * @author odboy
 * @date 2026-04-26
 */
@Slf4j
@Service("node_build_java")
public class NodeBuildJava implements InterruptableJob {

  private volatile boolean interrupted = false;

  public NodeBuildJava(ConfigurerAdapter configurerAdapter) {
  }

  /**
   * 中断任务执行
   */
  @Override
  public void interrupt() throws UnableToInterruptJobException {
    log.info("收到中断信号");
    this.interrupted = true;
  }

  /**
   * 执行节点逻辑（Quartz Job方式）
   *
   * @param context Job执行上下文
   * @throws JobExecutionException 执行异常
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String tags = context.getMergedJobDataMap().getString("tags");
    String job = context.getMergedJobDataMap().getString("job");
    String version = context.getMergedJobDataMap().getString("version");

    log.info("开始构建Java项目，tags: {}, job: {}, version: {}", tags, job, version);

    try {
      // 模拟构建逻辑
      ThreadUtil.sleep(3000);

      if (interrupted) {
        log.warn("构建节点被中断");
        return;
      }

      execute(context.getMergedJobDataMap());

    } catch (Exception e) {
      log.error("Java项目构建失败", e);
      throw new JobExecutionException("构建失败", e);
    }
  }

  /**
   * 提供一个简化的execute方法供PipelineExecutor调用
   *
   * @param params 执行参数
   */
  public void execute(Map<String, Object> params) {
    log.info("执行Java构建节点，参数: {}", params);

    try {
      // 获取参数
      String tags = params != null ? (String) params.get("tags") : null;
      String job = params != null ? (String) params.get("job") : null;
      String version = params != null ? (String) params.get("version") : null;

      log.info("开始构建Java项目，tags: {}, job: {}, version: {}", tags, job, version);

      // 模拟构建逻辑
      ThreadUtil.sleep(3000);

      log.info("Java项目构建成功");

    } catch (Exception e) {
      log.error("Java项目构建失败", e);
      throw new RuntimeException("构建失败: " + e.getMessage(), e);
    }
  }
}
