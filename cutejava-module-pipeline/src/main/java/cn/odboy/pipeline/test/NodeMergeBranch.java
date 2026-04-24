package cn.odboy.pipeline.test;

import cn.odboy.pipeline.core.BasePipelineNode;
import cn.odboy.pipeline.core.PipelineContext;
import org.springframework.stereotype.Service;

@Service("node_merge_branch")
public class NodeMergeBranch extends BasePipelineNode {

  @Override
  public void run(PipelineContext ctx) {
    // 合并代码逻辑
  }
}
