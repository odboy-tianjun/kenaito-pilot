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
import cn.odboy.gitlab.dal.dataobject.GitlabSiteConfigTb;
import cn.odboy.gitlab.infra.GitlabPipelineListener;
import cn.odboy.gitlab.repository.GitlabRepository;
import cn.odboy.gitlab.service.GitlabSiteConfigService;
import cn.odboy.util.KitDateUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.Pager;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Pipeline;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GitlabTests {

  @Autowired
  private GitlabRepository gitlabRepository;
  @Autowired
  private GitlabSiteConfigService gitlabSiteConfigService;

  @Test
  public void testCreateProject() {
    gitlabRepository.createProject(null, "测试应用", "test", "测试应用描述");
  }

  @Test
  public void testCreateGroup() {
    gitlabRepository.createGroup("testgroup3", "测试组描述");
  }

  @Test
  public void testMergeBranch() {
    gitlabRepository.mergeBranch("asdasd", "asdasdasda", "main");
  }

  @Test
  public void testMergeDefaultBranch() {
    gitlabRepository.mergeDefaultBranch("asdasd", "asdasdasda");
  }

  @Test
  public void testSyncCreatePipeline() {
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

    Pipeline pipeline = gitlabRepository.startPipeline(appName, branchName, variables);
    System.err.println("============== 执行结果 ==============");
    System.err.println(JSON.toJSONString(pipeline));
  }

  @Test
  public void testASyncCreatePipeline() {
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

    // 异步启动流水线监听
    gitlabRepository.startPipelineWithListener(
        appName, branchName, variables, new GitlabPipelineListener() {

          @Override
          public void pending(Pipeline pipeline) {
            System.err.println("============== 挂起 ==============");
          }

          @Override
          public void start(Pipeline pipeline) {
            System.err.println("============== 开始执行 ==============");
          }

          @Override
          public void running(Pipeline pipeline) {
            // 忽略
          }

          @Override
          public void success(Pipeline pipeline) {
            System.err.println("============== 成功 ==============");
          }

          @Override
          public void failed(Pipeline pipeline) {
            System.err.println("============== 失败 ==============");
          }
        }
    );

    ThreadUtil.sleep(1000 * 60 * 60);
  }

  @Test
  public void testOptimizeGetGroupByName() {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();

    String groupName = "test1";
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
//      Group group = gitLabApi.getGroupApi().getGroupsStream(groupName).filter(f -> f.getName().equals(groupName)).findFirst().orElse(null);
      Pager<Group> groups = gitLabApi.getGroupApi().getGroups(groupName, 1);
      System.err.println(groups);
    } catch (Exception e) {
      throw new BadRequestException(e);
    }
  }
}
