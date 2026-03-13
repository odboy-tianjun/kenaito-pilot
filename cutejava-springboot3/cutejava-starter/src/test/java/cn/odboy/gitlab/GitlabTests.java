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
import cn.odboy.gitlab.service.GitlabService;
import org.gitlab4j.api.models.Pipeline;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    Pipeline pipeline = gitlabService.startPipeline("asdasd", "asdasdasda", null);
    while (true) {
      ThreadUtil.sleep(2000);
      pipeline = gitlabService.getPipelineById(pipeline.getProjectId(), pipeline.getId());
      System.err.println("getStatus: " + pipeline.getStatus());
      System.err.println("getDetailedStatus: " + pipeline.getDetailedStatus());
      System.err.println("getStartedAt: " + pipeline.getStartedAt());
      System.err.println("getFinishedAt: " + pipeline.getFinishedAt());
      System.err.println("getDuration: " + pipeline.getDuration());
      System.err.println("getQueuedDuration: " + pipeline.getQueuedDuration());
      System.err.println("===============================");
    }
  }
}
