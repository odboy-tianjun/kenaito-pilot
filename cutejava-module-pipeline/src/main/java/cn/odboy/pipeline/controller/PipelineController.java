package cn.odboy.pipeline.controller;

import cn.odboy.annotation.AnonymousPostMapping;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.pipeline.core.PipelineScheduleService;
import cn.odboy.pipeline.dal.dataobject.PipelineTemplateContextTb;
import cn.odboy.pipeline.dal.model.NodeDefinition;
import cn.odboy.pipeline.service.PipelineTemplateContextService;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.ApiOperation;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

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
}
