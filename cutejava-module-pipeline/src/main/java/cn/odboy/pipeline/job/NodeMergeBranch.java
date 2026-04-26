package cn.odboy.pipeline.job;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * 合并代码节点 - 演示如何实现一个流水线节点
 */
@Slf4j
@Service("node_merge_branch")
public class NodeMergeBranch implements InterruptableJob {

  private volatile boolean interrupted = false;

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
    log.info("开始执行代码合并");

    try {
      // 模拟合并逻辑
      ThreadUtil.sleep(2000);

      if (interrupted) {
        log.warn("合并节点被中断");
        return;
      }

      execute(context.getMergedJobDataMap());

    } catch (Exception e) {
      log.error("代码合并失败", e);
      throw new JobExecutionException("合并失败", e);
    }
  }

  /**
   * 提供一个简化的execute方法供PipelineExecutor调用
   *
   * @param params 执行参数
   */
  public void execute(Map<String, Object> params) {
    log.info("执行代码合并节点，参数: {}", params);

    try {
      // 模拟合并逻辑
      ThreadUtil.sleep(2000);

      throw new RuntimeException("测试节点异常");

      //      log.info("代码合并成功");

    } catch (Exception e) {
      log.error("代码合并失败", e);
      throw new RuntimeException("合并失败: " + e.getMessage(), e);
    }
  }
}
