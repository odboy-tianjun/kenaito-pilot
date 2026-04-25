/*
 * Copyright 2021-2026 Odboy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.odboy.pipeline;

import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.pipeline.core.PipelineScheduleService;
import cn.odboy.pipeline.dal.dataobject.PipelineTemplateContextTb;
import cn.odboy.pipeline.dal.model.NodeDefinition;
import cn.odboy.pipeline.service.PipelineTemplateContextService;
import com.alibaba.fastjson2.JSON;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PipelineTests {

  @Autowired
  private PipelineTemplateContextService pipelineTemplateContextService;
  @Autowired
  private PipelineScheduleService pipelineScheduleService;

  @Test
  @SneakyThrows
  public void test() {
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
    pipelineScheduleService.startPipeline(nodes, inputParams);
  }
}
