package cn.odboy.pipeline.core;

import cn.odboy.pipeline.dal.model.NodeDefinition;
import java.util.List;
import java.util.Map;

public interface PipelineScheduleService {

  /**
   * 启动流水线
   *
   * @param nodes       节点定义列表
   * @param inputParams 输入参数
   * @return 实例ID
   */
  String startPipeline(List<NodeDefinition> nodes, Map<String, Object> inputParams);

  /**
   * 重试失败的节点
   *
   * @param instanceId 流水线实例ID
   * @param nodeCode   要重试的节点编码
   * @return 是否触发重试
   */
  boolean retryNode(String instanceId, String nodeCode);
}
