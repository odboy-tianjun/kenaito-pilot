package cn.odboy.pipeline;

import org.springframework.stereotype.Service;

@Service("node_init")
public class NodeInit extends BasePipelineNode {

  @Override
  public void run(PipelineContext ctx) {
    // 初始化逻辑
  }
}
