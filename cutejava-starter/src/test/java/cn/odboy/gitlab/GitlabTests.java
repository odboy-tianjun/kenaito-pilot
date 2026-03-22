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
import cn.odboy.util.KitDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.PipelineStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    // 容器镜像服务
    String criServiceUser = "tianjun@odboy.cn";
    String criServicePwd = "xxxxxx";

    // 项目配置
    String appName = "kenaito-pilot";
    String branchName = "main";
    String envCode = "daily";
    String versionCode = KitDateUtil.getNowDateTimeStr();

    // 变量构建
    Map<String, String> variables = new HashMap<>();
    variables.put("CONTEXT_NAME", appName);
    variables.put("VERSION_CODE", versionCode);
    variables.put("ENV_CODE", envCode);
    variables.put("CRI_SERVICE_USER", criServiceUser);
    variables.put("CRI_SERVICE_PWD", criServicePwd);

    Pipeline pipeline = gitlabService.startPipeline(appName, branchName, variables);

    // 判断流水线是否挂起
    final int maxRetryCount = 5;
    final int waitSeconds = 5;
    int retryCount = 1;
    while (true) {
      log.info("检测流水线是否挂起中，最大重试次数：{}，当前重试次数：{}", maxRetryCount, retryCount);
      ThreadUtil.sleep(waitSeconds * 1000);
      pipeline = gitlabService.getPipelineById(pipeline.getProjectId(), pipeline.getId());

      if (PipelineStatus.RUNNING.equals(pipeline.getStatus())) {
        log.info("流水线运行中");
        break;
      }
      if (PipelineStatus.FAILED.equals(pipeline.getStatus())) {
        log.info("流水线执行失败");
        throw new BadRequestException("流水线执行失败");
      }
      if (PipelineStatus.PENDING.equals(pipeline.getStatus())) {
        log.info("流水线挂起");
        retryCount++;
        if (retryCount > maxRetryCount) {
          throw new BadRequestException("流水线挂起，可能是由于.gitlab-ci.yml配置或者资源不足导致，请联系管理员处理");
        }
      }
    }

    // 运行流水线
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
