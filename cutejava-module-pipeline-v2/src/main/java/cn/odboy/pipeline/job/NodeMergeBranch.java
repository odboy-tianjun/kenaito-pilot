package cn.odboy.pipeline.job;

import cn.odboy.pipeline.constant.PipelineStatusEnum;
import cn.odboy.pipeline.core.TaskContext;
import cn.odboy.pipeline.core.TaskOperation;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 合并代码节点
 */
@Slf4j
@Service("node_merge_branch")
public class NodeMergeBranch implements TaskOperation {

  @Override
  public String execute(TaskContext context) {
    log.info("执行参数：{}", JSON.toJSONString(context));
//    throw new BadRequestException("调用Gitlab失败");
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
    return "合并代码";
  }

  @Override
  public long pollInterval() {
    return 2000;
  }

  private boolean checkServiceStatus(TaskContext context) {
    return true;
  }
}
