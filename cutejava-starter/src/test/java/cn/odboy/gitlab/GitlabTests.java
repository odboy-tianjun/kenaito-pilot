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
package cn.odboy.gitlab;

import cn.hutool.core.thread.ThreadUtil;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.gitlab.service.GitlabService;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.PipelineStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GitlabTests {

  @Autowired
  private GitlabService gitlabService;

  @Test
  public void testCreateProject() {
    gitlabService.createProject(null, "测试应用", "test", "测试应用描述");
  }

  @Test
  public void testCreateGroup() {
    gitlabService.createGroup("testgroup3", "测试组描述");
  }

  @Test
  public void testMergeBranch() {
    gitlabService.mergeBranch("asdasd", "asdasdasda", "main");
  }

  @Test
  public void testCreatePipeline() {
    Pipeline pipeline = gitlabService.startPipeline("kenaito-pilot", "main", null);

    // 判断流水线是否挂起
    int retryCnt = 5;
    int waitSeconds = 10;
    while (true) {
      if (retryCnt < 0) {
        throw new BadRequestException("Runner资源不足，请联系管理员处理");
      }
      ThreadUtil.sleep(waitSeconds * 1000);
      pipeline = gitlabService.getPipelineById(pipeline.getProjectId(), pipeline.getId());
      if (!PipelineStatus.RUNNING.equals(pipeline.getStatus())) {
        retryCnt--;
        continue;
      }
      break;
    }

    // 流水线启动时间
    waitSeconds = 2;
    Date startedAt = null;
    while (true) {
      ThreadUtil.sleep(waitSeconds * 1000);
      pipeline = gitlabService.getPipelineById(pipeline.getProjectId(), pipeline.getId());

      if (startedAt == null) {
        startedAt = pipeline.getStartedAt();
      }

      PipelineStatus status = pipeline.getStatus();
      Date finishedAt = pipeline.getFinishedAt();
      Integer duration = pipeline.getDuration();
      String ref = pipeline.getRef();
      String source = pipeline.getSource();
      String webUrl = pipeline.getWebUrl();

      log.info("==============================================================");
      log.info("getStatus -> 状态: {}", status);
      log.info("getWebUrl -> 明细URL: {}", webUrl);
      log.info("getRef -> 关联分支: {}", ref);
      log.info("getSource -> 来源: {}", source);

      // 执行中
      if (PipelineStatus.RUNNING.equals(status)) {
        continue;
      }

      // 执行失败
      if (PipelineStatus.FAILED.equals(status)) {
        log.info("getFinishedAt -> 完成时间: {}", finishedAt);
        log.info("getDuration -> 持续时长(秒): {}", duration);
        throw new BadRequestException("流水线执行失败");
      }

      // 执行成功
      if (PipelineStatus.SUCCESS.equals(status)) {
        log.info("getFinishedAt -> 完成时间: {}", finishedAt);
        log.info("getDuration -> 持续时长(秒): {}", duration);
        break;
      }
    }
  }
}
