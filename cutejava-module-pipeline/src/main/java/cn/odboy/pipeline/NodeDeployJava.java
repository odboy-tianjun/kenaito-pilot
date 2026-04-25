package cn.odboy.pipeline;

import org.springframework.stereotype.Service;

@Service("node_deploy_java")
public class NodeDeployJava extends BasePipelineNode {

  @Override
  public void run(PipelineContext ctx) {
    // 部署逻辑
  }
}
