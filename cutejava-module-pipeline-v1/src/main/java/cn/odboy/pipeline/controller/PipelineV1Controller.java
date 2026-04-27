package cn.odboy.pipeline.controller;

import cn.odboy.annotation.AnonymousPostMapping;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.pipeline.core.PipelineScheduleService;
import cn.odboy.pipeline.dal.dataobject.PipelineTemplateContextTb;
import cn.odboy.pipeline.dal.model.NodeDefinition;
import cn.odboy.pipeline.service.PipelineTemplateContextService;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/pipeline/v1")
public class PipelineV1Controller {

  @Autowired
  private PipelineTemplateContextService pipelineTemplateContextService;
  @Autowired
  private PipelineScheduleService pipelineScheduleService;

  @ApiOperation("测试")
  @AnonymousPostMapping(value = "/test")
  public ResponseEntity<?> doTest() throws SchedulerException {
    String clusterType = "k8s";
    String clusterCode = "local_k8s_28";
    String contextType = "deploy_app";
    String contextName = "kenaito-pilot";

    PipelineTemplateContextTb pipelineTemplateContext = pipelineTemplateContextService.getPipelineTemplateContext(clusterType, clusterCode, contextType,
        contextName
    );
    if (pipelineTemplateContext == null) {
      throw new BadRequestException("流水线模版不存在");
    }

    List<NodeDefinition> nodes = JSON.parseArray(pipelineTemplateContext.getTemplate(), NodeDefinition.class);
    Map<String, Object> inputParams = new HashMap<>();
    String instanceId = pipelineScheduleService.startPipeline(nodes, inputParams);

    return ResponseEntity.ok(instanceId);
  }

  @ApiOperation("重试节点")
  @AnonymousPostMapping(value = "/retry")
  public ResponseEntity<?> retryNode(@RequestParam String instanceId, @RequestParam String nodeCode) {
    log.info("请求重试节点，实例ID: {}, 节点: {}", instanceId, nodeCode);

    boolean success = pipelineScheduleService.retryNode(instanceId, nodeCode);

    if (success) {
      return ResponseEntity.ok("节点重试已触发");
    } else {
      return ResponseEntity.badRequest().body("节点重试失败");
    }
  }
}
