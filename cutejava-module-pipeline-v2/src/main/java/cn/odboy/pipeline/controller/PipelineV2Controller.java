package cn.odboy.pipeline.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.odboy.annotation.AnonymousPostMapping;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.pipeline.core.TaskBuilder;
import cn.odboy.pipeline.core.TaskEngine;
import cn.odboy.pipeline.core.TaskResult;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.dal.dataobject.PipelineTemplateContextTb;
import cn.odboy.pipeline.core.NodeDefinition;
import cn.odboy.pipeline.service.PipelineTemplateContextService;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/pipeline/v2")
public class PipelineV2Controller {

  @Autowired
  private PipelineTemplateContextService pipelineTemplateContextService;
  @Autowired
  private TaskEngine taskEngine;

  @ApiOperation("启动流水线（测试）")
  @AnonymousPostMapping(value = "/testStart")
  public ResponseEntity<?> testStart() {
    String clusterType = "k8s";
    String clusterCode = "local_k8s_28";
    String contextType = "deploy_app";
    String contextName = "kenaito-pilot";
    PipelineTemplateContextTb pipelineTemplateContext = pipelineTemplateContextService.getPipelineTemplateContext(clusterType, clusterCode, contextType,
        contextName);
    if (pipelineTemplateContext == null) {
      throw new BadRequestException("流水线模版查询失败");
    }

    String template = pipelineTemplateContext.getTemplate();
    List<NodeDefinition> nodeDefinitions = JSON.parseArray(template, NodeDefinition.class);
    if (CollUtil.isEmpty(nodeDefinitions)) {
      throw new BadRequestException("流水线节点模版解析失败");
    }

    String taskId = IdUtil.fastSimpleUUID();
    TaskBuilder taskBuilder = new TaskBuilder()
        .taskId(taskId)
        .clusterType(clusterType)
        .clusterCode(clusterCode)
        .contextType(contextType)
        .contextName(contextName);

    Map<String, Object> mergedParameters = nodeDefinitions.stream()
        .map(NodeDefinition::getParameters)
        .filter(parameters -> parameters != null && !parameters.isEmpty())
        .flatMap(parameters -> parameters.entrySet().stream())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (existing, replacement) -> replacement
        ));

    // 设置参数
    for (Entry<String, Object> entry : mergedParameters.entrySet()) {
      taskBuilder = taskBuilder.withData(entry.getKey(), entry.getValue());
    }

    // 设置节点
    taskBuilder = taskBuilder.nodes(nodeDefinitions);
    return ResponseEntity.ok(taskBuilder.execute());
  }


  @ApiOperation("流水线节点重试")
  @AnonymousPostMapping(value = "/testRetry")
  public ResponseEntity<?> testRetry(@RequestBody PipelineInstanceTb args) {
    TaskResult result = taskEngine.retry(args.getId(), args.getCurrentNodeCode());
    return ResponseEntity.ok(result);
  }
}
