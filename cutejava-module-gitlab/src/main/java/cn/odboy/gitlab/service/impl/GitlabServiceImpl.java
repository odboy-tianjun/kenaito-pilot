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
package cn.odboy.gitlab.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.odboy.constant.SystemConst;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.gitlab.config.GitlabProperties;
import cn.odboy.gitlab.constant.GitlabConst;
import cn.odboy.gitlab.service.GitlabService;
import com.alibaba.fastjson2.JSON;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestParams;
import org.gitlab4j.api.models.Namespace;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.PipelineFilter;
import org.gitlab4j.api.models.PipelineStatus;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.Visibility;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GitlabServiceImpl implements InitializingBean, GitlabService {

  @Autowired
  private GitlabProperties properties;

  @Override
  public void afterPropertiesSet() {
    this.showVersion();
  }

  private void showVersion() {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      String apiNamespace = gitLabApi.getApiVersion().getApiNamespace();
      log.info("Gitlab版本：{}", apiNamespace);
    } catch (Exception e) {
      log.error("Gitlab异常", e);
      throw new BadRequestException("Gitlab异常");
    }
  }

  /**
   * 查询root用户的namespace
   *
   * @return /
   */
  private Namespace getRootNamespace() {
    return this.getNamespaceById(GitlabConst.ROOT_ID);
  }

  private Group getGroupByName(@NonNull String groupName) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      return gitLabApi.getGroupApi().getGroupsStream(groupName)
          .filter(f -> f.getName().equals(groupName))
          .findFirst()
          .orElse(null);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 根据id查询namespace
   *
   * @param id userId/GroupId
   * @return /
   */
  private Namespace getNamespaceById(@NonNull Long id) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      return gitLabApi.getNamespaceApi().getNamespace(id);
    } catch (GitLabApiException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * 创建合并请求
   *
   * @param projectId    项目id
   * @param sourceBranch 源分支
   * @param targetBranch 目标分支
   * @return /
   */
  private MergeRequest createMergeRequest(@NonNull Long projectId, @NonNull String sourceBranch, @NonNull String targetBranch) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      MergeRequestParams mergeRequestParams = new MergeRequestParams();
      mergeRequestParams.withSourceBranch(sourceBranch);
      mergeRequestParams.withTargetBranch(targetBranch);
      mergeRequestParams.withTitle(String.format("%s合并到%s", sourceBranch, targetBranch));
      mergeRequestParams.withRemoveSourceBranch(false);
      return gitLabApi.getMergeRequestApi().createMergeRequest(projectId, mergeRequestParams);
    } catch (GitLabApiException e) {
      if (e.getMessage().contains("404 Project Not Found")) {
        throw new BadRequestException("项目不存在");
      }
      throw new BadRequestException("创建合并请求失败");
    }
  }

  /**
   * 根据iid查询
   *
   * @param projectId 项目id
   * @param iid       合并请求iid
   * @return /
   */
  private MergeRequest getMergeRequestByIid(@NonNull Long projectId, @NonNull Long iid) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      return gitLabApi.getMergeRequestApi().getMergeRequest(projectId, iid);
    } catch (GitLabApiException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * 查询已打开的合并请求
   *
   * @param projectId    项目id
   * @param sourceBranch 源分支
   * @param targetBranch 目标分支
   */
  private MergeRequest getOpenedMergeRequestByBranchName(@NonNull Long projectId, @NonNull String sourceBranch, @NonNull String targetBranch) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      return gitLabApi.getMergeRequestApi().getMergeRequestsStream(projectId)
          .filter(f -> f.getSourceBranch().equals(sourceBranch))
          .filter(f -> f.getTargetBranch().equals(targetBranch))
          .filter(f -> "opened".equals(f.getState()))
          .findFirst().orElse(null);
    } catch (GitLabApiException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * 允许合并
   *
   * @param projectId 项目id
   * @param iid       合并请求iid
   * @return /
   */
  private MergeRequest acceptMergeRequest(@NonNull Long projectId, @NonNull Long iid) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      return gitLabApi.getMergeRequestApi().acceptMergeRequest(projectId, iid);
    } catch (GitLabApiException e) {
      throw new BadRequestException("允许合并请求失败");
    }
  }

  /**
   * 删除合并请求
   *
   * @param projectId 项目id
   * @param iid       合并请求iid
   */
  private void deleteMergeRequestByIid(@NonNull Long projectId, @NonNull Long iid) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      gitLabApi.getMergeRequestApi().deleteMergeRequest(projectId, iid);
    } catch (GitLabApiException e) {
      throw new BadRequestException("删除合并请求失败");
    }
  }

  /**
   * 根据项目名查询
   *
   * @param projectEnglishName 项目英文名称
   * @return /
   */
  private Project getProjectByName(@NonNull String projectEnglishName) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      return gitLabApi.getProjectApi().getProjectsStream().filter(f -> f.getPath().equals(projectEnglishName)).findFirst().orElse(null);
    } catch (GitLabApiException e) {
      throw new BadRequestException(e);
    }
  }

  @Override
  public Project createProject(String groupName, String chineseName, @NonNull String englishName, @NonNull String description) {
    Namespace namespace;
    if (StrUtil.isBlank(groupName)) {
      namespace = this.getRootNamespace();
    } else {
      Group group = this.getGroupByName(groupName);
      if (group == null) {
        group = this.createGroup(groupName, SystemConst.CURRENT_APP_TITLE + " 自动创建");
      }
      namespace = this.getNamespaceById(group.getId());
    }
    String name = StrUtil.isNotBlank(chineseName) ? chineseName : englishName;
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      Project project = new Project();
      project.setName(name);
      project.setPath(englishName);
      project.setDescription(description);
      project.setNamespace(namespace);
      project.setDefaultBranch("master");
      project.setVisibility(Visibility.PRIVATE);
      return gitLabApi.getProjectApi().createProject(project);
    } catch (GitLabApiException e) {
      if (e.getMessage().contains("has already been taken")) {
        throw new BadRequestException("项目名称已存在");
      }
      throw new BadRequestException("创建项目失败");
    }
  }

  @Override
  public Group createGroup(@NonNull String groupName, @NonNull String description) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      GroupParams groupParams = new GroupParams();
      groupParams.withName(groupName);
      groupParams.withPath(groupName);
      groupParams.withVisibility(Visibility.PRIVATE.toValue());
      groupParams.withDescription(description);
      return gitLabApi.getGroupApi().createGroup(groupParams);
    } catch (GitLabApiException e) {
      if (e.getMessage().contains("has already been taken")) {
        throw new BadRequestException("组名称已存在");
      }
      throw new BadRequestException("创建组失败");
    }
  }

  @Override
  public MergeRequest mergeBranch(@NonNull String projectEnglishName, @NonNull String sourceBranch, @NonNull String targetBranch) {
    Project project = this.getProjectByName(projectEnglishName);

    MergeRequest mergeRequest = this.getOpenedMergeRequestByBranchName(project.getId(), sourceBranch, targetBranch);
    if (mergeRequest == null) {
      mergeRequest = this.createMergeRequest(project.getId(), sourceBranch, targetBranch);
    }

    int maxRetry = 10;
    while (maxRetry > 0) {
      maxRetry--;

      ThreadUtil.sleep(5000);
      mergeRequest = this.getMergeRequestByIid(project.getId(), mergeRequest.getIid());
//      System.err.println("getState: " + mergeRequest.getState());
//      System.err.println("getMergeError: " + mergeRequest.getMergeError());
//      System.err.println("getHasConflicts: " + mergeRequest.getHasConflicts());
//      System.err.println("getDetailedMergeStatus: " + mergeRequest.getDetailedMergeStatus());
//      System.err.println("============================");

      // 无变更，不需要合并，删除合并请求
      int changesCount = Integer.parseInt(mergeRequest.getChangesCount());
      if (changesCount <= 0) {
        this.deleteMergeRequestByIid(project.getId(), mergeRequest.getIid());
        return mergeRequest;
      }
      // 分支已合并
      if ("merged".equals(mergeRequest.getState())) {
        return mergeRequest;
      }
      // 分支存在冲突
      if ("opened".equals(mergeRequest.getState()) && mergeRequest.getHasConflicts()) {
        // 在线解决冲突地址 http://219.151.187.115:5000/test1/asdasd/-/merge_requests/7/conflicts
        throw new BadRequestException(String.format("%s 与 %s 存在冲突", sourceBranch, targetBranch));
      }
      // 允许合并
      if ("opened".equals(mergeRequest.getState())) {
        return this.acceptMergeRequest(project.getId(), mergeRequest.getIid());
      }
    }
    throw new BadRequestException("超过最大重试次数");
  }

  private RepositoryFile getRepositoryFile(@NonNull Long id, @NonNull String filePath, @NonNull String branchName, @NonNull Boolean includeContent) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      return gitLabApi.getRepositoryFileApi().getFile(id, filePath, branchName, includeContent);
    } catch (GitLabApiException e) {
      if ("Not Found".equals(e.getReason())) {
        return null;
      }
      throw new BadRequestException("获取仓库文件失败");
    }
  }

  @Override
  public Pipeline startPipeline(@NonNull String projectEnglishName, @NonNull String branchName, Map<String, String> variables) {
    Project project = this.getProjectByName(projectEnglishName);
    List<Pipeline> pendingPipelineList = this.getPipelineByStatus(project.getId(), branchName, PipelineStatus.PENDING);
    if (CollUtil.isNotEmpty(pendingPipelineList)) {
      log.info("存在挂起的流水线，停止并释放资源");
      for (Pipeline pipeline : pendingPipelineList) {
        // 取消任务
        this.cancelPipelineJobs(project.getId(), pipeline.getId());
        // 删除流水线
        this.deletePipeline(project.getId(), pipeline.getId());
      }
    }
    List<Pipeline> canceledPipelineList = this.getPipelineByStatus(project.getId(), branchName, PipelineStatus.CANCELED);
    if (CollUtil.isNotEmpty(canceledPipelineList)) {
      log.info("存在取消的任务，释放资源");
      for (Pipeline pipeline : canceledPipelineList) {
        this.deletePipeline(project.getId(), pipeline.getId());
      }
    }
    // 理论上只能有一个运行中的
    List<Pipeline> runningPipelineList = this.getPipelineByStatus(project.getId(), branchName, PipelineStatus.RUNNING);
    if (CollUtil.isNotEmpty(runningPipelineList)) {
      log.info("存在相同的运行中的流水线，停止并释放资源");
      for (Pipeline pipeline : runningPipelineList) {
        // 取消任务
        this.cancelPipelineJobs(project.getId(), pipeline.getId());
        // 删除流水线
        this.deletePipeline(project.getId(), pipeline.getId());
      }
    }
    // 创建新流水线
    return this.createPipeline(project.getId(), branchName, variables);
  }

  /**
   * 停止指定项目的流水线
   *
   * @param projectId  项目id
   * @param pipelineId 流水线id
   */
  private void cancelPipelineJobs(@NonNull Long projectId, @NonNull Long pipelineId) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      gitLabApi.getPipelineApi().cancelPipelineJobs(projectId, pipelineId);
    } catch (GitLabApiException e) {
      throw new BadRequestException("停止流水线失败");
    }
  }

  /**
   * 删除指定项目的流水线
   *
   * @param projectId  项目id
   * @param pipelineId 流水线id
   */
  private void deletePipeline(@NonNull Long projectId, @NonNull Long pipelineId) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      gitLabApi.getPipelineApi().deletePipeline(projectId, pipelineId);
    } catch (GitLabApiException e) {
      throw new BadRequestException("删除流水线失败");
    }
  }

  /**
   * 创建流水线
   *
   * @param projectId  项目id
   * @param branchName 分支名称
   * @param variables  运行变量
   * @return /
   */
  private Pipeline createPipeline(@NonNull Long projectId, @NonNull String branchName, Map<String, String> variables) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      Pipeline pipeline = gitLabApi.getPipelineApi().createPipeline(projectId, branchName, variables);
      log.info("创建流水线成功, {}", JSON.toJSONString(pipeline));
      return pipeline;
    } catch (GitLabApiException e) {
      if (e.getMessage().contains("Pipeline filtered out by workflow rules")) {
        RepositoryFile repositoryFile = this.getRepositoryFile(projectId, ".gitlab-ci.yml", branchName, false);
        if (repositoryFile == null) {
          throw new BadRequestException("分支 " + branchName + " 中缺少 .gitlab-ci.yml，请补充后再试");
        }
      }
      throw new BadRequestException("创建流水线失败");
    }
  }

  private List<Pipeline> getPipelineByStatus(@NonNull Long projectId, @NonNull String branchName, @NonNull PipelineStatus pipelineStatus) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      PipelineFilter pipelineFilter = new PipelineFilter();
      pipelineFilter.withRef(branchName);
      pipelineFilter.withStatus(pipelineStatus);
      return gitLabApi.getPipelineApi().getPipelines(projectId, pipelineFilter);
    } catch (GitLabApiException e) {
      throw new BadRequestException("根据状态查询流水线列表");
    }
  }

  @Override
  public Pipeline getPipelineById(@NonNull Long projectId, @NonNull Long pipelineId) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      return gitLabApi.getPipelineApi().getPipeline(projectId, pipelineId);
    } catch (GitLabApiException e) {
      throw new BadRequestException("查询流水线明细失败");
    }
  }

  /**
   * 流水线重试
   *
   * @param projectId  项目id
   * @param pipelineId 流水线id
   */
  private void retryPipelineJob(@NonNull Long projectId, @NonNull Long pipelineId) {
    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
      gitLabApi.getPipelineApi().retryPipelineJob(projectId, pipelineId);
    } catch (GitLabApiException e) {
      throw new BadRequestException("流水线重试失败");
    }
  }

//  private void createRepositoryFile(Long id, String filePath, String branchName, String content) {
//    try (GitLabApi gitLabApi = new GitLabApi(properties.getEndpoint(), properties.getToken())) {
//      RepositoryFile repositoryFile = new RepositoryFile();
//      repositoryFile.setEncoding(Constants.Encoding.TEXT);
//      repositoryFile.setFilePath(filePath);
//      repositoryFile.setContent(content);
//      gitLabApi.getRepositoryFileApi().createFile(id, repositoryFile, branchName, SystemConst.CURRENT_APP_TITLE + "自动补充");
//    } catch (GitLabApiException e) {
//      throw new BadRequestException("创建仓库文件 " + filePath + " 失败");
//    }
//  }
}
