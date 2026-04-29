package cn.odboy.pipeline.v3.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.odboy.annotation.AnonymousPostMapping;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.pipeline.v3.core.NodeDefinition;
import cn.odboy.pipeline.v3.core.PipelineOrchestrator;
import cn.odboy.pipeline.v3.core.TaskContext;
import cn.odboy.pipeline.v3.core.TaskResult;
import cn.odboy.pipeline.v3.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.v3.dal.dataobject.PipelineTemplateContextTb;
import cn.odboy.pipeline.v3.service.PipelineTemplateContextService;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流水线 V2 控制器 职责： 1. 接收 API 请求 2. 参数校验 3. 委托给编排器执行
 */
@Slf4j
@RestController
@RequestMapping("/api/pipeline/v2")
public class PipelineV3Controller {

  @Autowired
  private PipelineTemplateContextService templateContextService;
  @Autowired
  private PipelineOrchestrator orchestrator;

  @ApiOperation("启动流水线（测试）")
  @AnonymousPostMapping(value = "/testStart")
  public ResponseEntity<?> testStart() {
    // 1. 获取模板配置
    String clusterType = "k8s";
    String clusterCode = "local_k8s_28";
    String contextType = "deploy_app";
    String contextName = "kenaito-pilot";

    PipelineTemplateContextTb templateContext = templateContextService.getPipelineTemplateContext(
        clusterType, clusterCode, contextType, contextName);

    if (templateContext == null) {
      throw new BadRequestException("流水线模版查询失败");
    }

    // 2. 解析节点定义
    List<NodeDefinition> nodeDefinitions = JSON.parseArray(
        templateContext.getTemplate(), NodeDefinition.class);

    if (CollUtil.isEmpty(nodeDefinitions)) {
      throw new BadRequestException("流水线节点模版解析失败");
    }

    // 3. 构建任务上下文
    String taskId = IdUtil.fastSimpleUUID();
    TaskContext context = new TaskContext();
    context.setTaskId(taskId);
    context.setClusterType(clusterType);
    context.setClusterCode(clusterCode);
    context.setContextType(contextType);
    context.setContextName(contextName);

    // 4. 合并参数到上下文
    Map<String, Object> mergedParameters = nodeDefinitions.stream()
        .map(NodeDefinition::getParameters)
        .filter(params -> params != null && !params.isEmpty())
        .flatMap(params -> params.entrySet().stream())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (existing, replacement) -> replacement
        ));

    mergedParameters.forEach(context::put);

    // 5. 通过编排器触发执行
    try {
      orchestrator.executePipeline(nodeDefinitions, context);
      log.info("流水线触发成功，实例ID: {}", taskId);
      return ResponseEntity.ok(taskId);
    } catch (Exception e) {
      log.error("流水线触发失败，实例ID: {}", taskId, e);
      throw new RuntimeException("流水线启动失败", e);
    }
  }

  @ApiOperation("流水线节点重试")
  @AnonymousPostMapping(value = "/testRetry")
  public ResponseEntity<?> testRetry(@RequestBody PipelineInstanceTb args) {
    try {
      orchestrator.retryPipeline(args.getId(), args.getCurrentNodeCode());
      log.info("流水线重试触发成功，实例ID: {}", args.getId());
      return ResponseEntity.ok(TaskResult.success("重试已启动"));
    } catch (Exception e) {
      log.error("流水线重试触发失败，实例ID: {}", args.getId(), e);
      throw new RuntimeException("重试启动失败", e);
    }
  }
}
