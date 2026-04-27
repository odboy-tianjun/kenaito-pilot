package cn.odboy.pipeline.core;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.stereotype.Component;

/**
 * 流水线Job工厂 - 解决Job中注入Spring Bean为null的问题
 */
@Slf4j
@Component("pipelineJobFactory")
public class PipelineJobFactory extends AdaptableJobFactory {

  private final AutowireCapableBeanFactory autowireCapableBeanFactory;

  @Autowired
  public PipelineJobFactory(AutowireCapableBeanFactory autowireCapableBeanFactory) {
    this.autowireCapableBeanFactory = autowireCapableBeanFactory;
  }

  @Nonnull
  @Override
  protected Object createJobInstance(@Nonnull TriggerFiredBundle triggerFiredBundle) throws Exception {
    try {
      // 调用父类的方法，创建Job实例
      Object jobInstance = super.createJobInstance(triggerFiredBundle);
      // 将Job实例注入到Spring容器中，使其能够使用@Autowired等注解
      autowireCapableBeanFactory.autowireBean(jobInstance);
      log.debug("Pipeline Job实例创建成功: {}", jobInstance.getClass().getSimpleName());
      return jobInstance;
    } catch (Exception e) {
      log.error("Pipeline Job实例创建失败", e);
      throw e;
    }
  }
}
