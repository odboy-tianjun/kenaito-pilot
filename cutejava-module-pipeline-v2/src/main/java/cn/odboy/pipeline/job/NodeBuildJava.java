package cn.odboy.pipeline.job;

import cn.hutool.core.thread.ThreadUtil;
import cn.odboy.pipeline.constant.PipelineStatusEnum;
import cn.odboy.pipeline.core.TaskContext;
import cn.odboy.pipeline.core.TaskOperation;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 构建Java节点
 *
 * @author odboy
 * @date 2026-04-26
 */
@Slf4j
@Service("node_build_java")
public class NodeBuildJava implements TaskOperation {

//  @Autowired
//  private TaskEngine taskEngine;

  @Override
  public String execute(TaskContext context) {
    log.info("执行参数：{}", JSON.toJSONString(context));
//    taskEngine.stopJob(context);
//    log.info("任务停止测试，当前上下文: {}", JSON.toJSONString(context));
    return PipelineStatusEnum.RUNNING.getCode();
  }

  @Override
  public String describe(TaskContext context) {
    boolean isReady = checkServiceStatus(context);
    if (isReady) {
      return PipelineStatusEnum.SUCCESS.getCode();
    }
    return PipelineStatusEnum.RUNNING.getCode();
  }

  @Override
  public String name() {
    return "构建";
  }

  @Override
  public long pollInterval() {
    return 2000;
  }

  private boolean checkServiceStatus(TaskContext context) {
    ThreadUtil.sleep(3000);
    return true;
  }
}
