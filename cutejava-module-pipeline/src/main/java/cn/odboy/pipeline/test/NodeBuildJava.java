package cn.odboy.pipeline.test;

import cn.odboy.pipeline.core.BasePipelineNode;
import cn.odboy.pipeline.core.PipelineContext;
import org.springframework.stereotype.Service;

@Service("node_build_java")
public class NodeBuildJava extends BasePipelineNode {

  @Override
  public void run(PipelineContext ctx) {
    String tags = ctx.getParam("tags");
    String job = ctx.getParam("job");
    String version = ctx.getParam("version");
    // 构建逻辑
  }
}
