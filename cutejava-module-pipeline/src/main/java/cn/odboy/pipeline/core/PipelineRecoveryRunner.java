package cn.odboy.pipeline;

import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.dal.mysql.PipelineInstanceMapper;
import cn.odboy.pipeline.core.PipelineScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PipelineRecoveryRunner implements ApplicationRunner {

  private final PipelineInstanceMapper instanceMapper;
  private final PipelineScheduleService scheduleService;

  @Override
  public void run(ApplicationArguments args) {
    log.info("【流水线】应用启动，开始自动恢复未完成任务");
    List<PipelineInstanceTb> list = instanceMapper.selectUnfinished();

    for (PipelineInstanceTb instance : list) {
      try {
        scheduleService.recoverPipeline(instance.getId());
        log.info("恢复成功：{}", instance.getId());
      } catch (Exception e) {
        log.error("恢复失败：{}", instance.getId(), e);
      }
    }
  }
}
