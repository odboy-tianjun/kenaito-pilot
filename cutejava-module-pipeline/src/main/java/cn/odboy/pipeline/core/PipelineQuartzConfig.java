package cn.odboy.pipeline.core;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * 流水线Quartz配置 - 配置Scheduler使用自定义的JobFactory
 */
@Slf4j
@Configuration
public class PipelineQuartzConfig {

  @Autowired
  private Scheduler scheduler;

  @Autowired
  private PipelineJobFactory pipelineJobFactory;

  /**
   * 初始化时设置JobFactory
   */
  @PostConstruct
  public void init() {
    try {
      // 设置自定义的JobFactory，使Job能够注入Spring Bean
      scheduler.setJobFactory(pipelineJobFactory);
      log.info("Pipeline Quartz Scheduler配置完成，已设置自定义JobFactory");
    } catch (SchedulerException e) {
      log.error("设置Pipeline JobFactory失败", e);
      throw new RuntimeException("设置Pipeline JobFactory失败", e);
    }
  }
}
