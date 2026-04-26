package cn.odboy.pipeline.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * 初始化节点示例 - 演示如何实现一个流水线节点
 */
@Slf4j
@Service("node_init")
public class NodeInit implements InterruptableJob {

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
   * 执行节点逻辑
   *
   * @param context Job执行上下文
   * @throws JobExecutionException 执行异常
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("开始执行初始化节点");

    try {
      // 模拟初始化逻辑
      Thread.sleep(1000);

      if (interrupted) {
        log.warn("初始化节点被中断");
        return;
      }

      execute(context.getMergedJobDataMap());

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new JobExecutionException("初始化节点执行被中断", e);
    } catch (Exception e) {
      log.error("初始化节点执行失败", e);
      throw new JobExecutionException("初始化节点执行失败", e);
    }
  }

  /**
   * 提供一个简化的execute方法供PipelineExecutor调用
   *
   * @param params 执行参数
   */
  public void execute(Map<String, Object> params) {
    log.info("执行初始化节点，参数: {}", params);

    try {
      // 模拟初始化逻辑
      Thread.sleep(1000);

      log.info("初始化节点执行完成");

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("初始化节点执行被中断", e);
    } catch (Exception e) {
      log.error("初始化节点执行失败", e);
      throw new RuntimeException("初始化节点执行失败", e);
    }
  }
}
