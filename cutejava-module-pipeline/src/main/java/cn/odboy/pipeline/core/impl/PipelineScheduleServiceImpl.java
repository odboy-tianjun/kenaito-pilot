package cn.odboy.pipeline.core.impl;

import cn.odboy.pipeline.core.PipelineScheduleService;
import cn.odboy.pipeline.dal.model.NodeDefinition;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class PipelineScheduleServiceImpl implements PipelineScheduleService {

  @Override
  public String startPipeline(List<NodeDefinition> nodes, Map<String, Object> inputParams) {
    return "";
  }
}
