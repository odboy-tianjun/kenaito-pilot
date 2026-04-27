package cn.odboy.pipeline.job;

import cn.odboy.pipeline.constant.PipelineStatusEnum;
import cn.odboy.pipeline.core.TaskContext;
import cn.odboy.pipeline.core.TaskOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 初始化节点示例
 */
@Slf4j
@Service("node_init")
public class NodeInit implements TaskOperation {

  @Override
  public String execute(TaskContext context) {
    return PipelineStatusEnum.SUCCESS.getCode();
  }

  @Override
  public String name() {
    return "初始化";
  }
}
