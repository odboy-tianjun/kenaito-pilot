package cn.odboy.pipeline.v4.job;

import cn.hutool.core.thread.ThreadUtil;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.pipeline.v4.constant.TaskStatusEnum;
import cn.odboy.pipeline.v4.core.TaskContext;
import cn.odboy.pipeline.v4.core.TaskOperation;
import cn.odboy.pipeline.v4.core.TaskScheduler;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
//  private TaskScheduler taskScheduler;
  // 任务手动停止测试
//    taskScheduler.stopJob(context);
  //    throw new BadRequestException("模拟执行失败");
  @Override
  public String execute(TaskContext context) {
    log.info("执行参数：{}", JSON.toJSONString(context));
    return TaskStatusEnum.RUNNING.getCode();
  }

  @Override
  public String describe(TaskContext context) {
    boolean isReady = checkServiceStatus(context);
    if (isReady) {
      return TaskStatusEnum.SUCCESS.getCode();
    }
    return TaskStatusEnum.RUNNING.getCode();
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
    ThreadUtil.sleep(5000);
    return true;
  }
}
