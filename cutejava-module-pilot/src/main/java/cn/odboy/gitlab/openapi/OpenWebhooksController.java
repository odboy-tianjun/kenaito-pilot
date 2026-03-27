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
package cn.odboy.gitlab.openapi;

import cn.odboy.annotation.AnonymousGetMapping;
import cn.odboy.gitlab.dal.model.GitlabModel;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhooks/gitlab")
@Api(tags = "Gitlab：免签验证")
public class OpenWebhooksController {

  /**
   * 此处的@ModelAttribute注解可以省略，但是为了方便学习，这里不做处理
   */
  @ApiOperation("构建上报入口")
  @AnonymousGetMapping(value = "/build")
  public ResponseEntity<?> report(@ModelAttribute GitlabModel.RunnerBuildReportArgs request) {
    // 构建状态回传：{"commitId":"fdaa4e409037586b6c9dce9fcc9e355e4a84f897","contextName":"kenaito-pilot","envCode":"daily","imageName":"registry.cn-shanghai.aliyuncs.com/odboy/devops:kenaito-pilot_daily_20260325190837","jobId":"50","pipelineId":"43","projectId":"6","status":"success","versionCode":"20260325190837"}
    log.info("构建状态回传：{}", JSON.toJSONString(request));
    return ResponseEntity.ok(null);
  }
}
