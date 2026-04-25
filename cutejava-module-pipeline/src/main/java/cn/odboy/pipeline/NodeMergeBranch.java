package cn.odboy.pipeline;

import org.springframework.stereotype.Service;

@Service("node_merge_branch")
public class NodeMergeBranch extends BasePipelineNode {

  @Override
  public void run(PipelineContext ctx) {
    // 合并代码逻辑
  }
}
