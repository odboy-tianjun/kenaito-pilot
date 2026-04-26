package cn.odboy.pipeline.core;

import cn.odboy.pipeline.dal.model.NodeDefinition;
import java.util.List;
import java.util.Map;

public interface PipelineScheduleService {

  String startPipeline(List<NodeDefinition> nodes, Map<String, Object> inputParams);
}
