package cn.odboy.pipeline.test;

import cn.odboy.pipeline.core.BasePipelineNode;
import cn.odboy.pipeline.core.PipelineContext;
import org.springframework.stereotype.Service;

@Service("node_init")
public class NodeInit extends BasePipelineNode {

  @Override
  public void run(PipelineContext ctx) {
    // 初始化逻辑
  }
}
