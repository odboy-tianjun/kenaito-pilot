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
package cn.odboy.gitlab.initializer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.odboy.constant.SystemConst;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.gitlab.dal.dataobject.GitlabSiteConfigTb;
import cn.odboy.gitlab.infra.GitlabPipelineListener;
import cn.odboy.gitlab.infra.GitlabProperties;
import cn.odboy.gitlab.repository.GitlabRepository;
import cn.odboy.gitlab.service.GitlabSiteConfigService;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.Pager;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestFilter;
import org.gitlab4j.api.models.MergeRequestParams;
import org.gitlab4j.api.models.Namespace;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.PipelineFilter;
import org.gitlab4j.api.models.PipelineStatus;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.Visibility;
import org.gitlab4j.models.Constants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GitlabRepositoryInitializer implements InitializingBean, GitlabRepository {

  @Autowired
  private GitlabProperties properties;
  @Autowired
  private GitlabSiteConfigService gitlabSiteConfigService;
  @Resource(name = "taskAsync")
  private ThreadPoolTaskExecutor taskExecutor;

  @Override
  public void afterPropertiesSet() {
    List<GitlabSiteConfigTb> configTbs = gitlabSiteConfigService.list();
    for (GitlabSiteConfigTb configTb : configTbs) {
      try {
        this.getOneGroup(configTb);
        configTb.setStatus(true);
        configTb.setErrorMessage("");
      } catch (Exception e) {
        configTb.setStatus(false);
        configTb.setErrorMessage(e.getMessage());
      }
    }
    gitlabSiteConfigService.updateBatchById(configTbs);
  }

  /**
   * 创建项目
   *
   * @param groupName   所属组名称
   * @param chineseName 中文名称
   * @param englishName 英文名称
   * @param description 描述
   * @return /
   */
  @Override
  public Project createProject(String groupName, String chineseName, @NonNull String englishName, @NonNull String description) {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();

    Namespace namespace;
    if (StrUtil.isBlank(groupName)) {
      namespace = this.getRootNamespace();
    } else {
      Group group = this.getGroupByName(masterSiteConfig, groupName);
      if (group == null) {
        group = this.createGroup(groupName, SystemConst.CURRENT_APP_TITLE + " 自动创建");
      }
      namespace = this.getNamespaceById(group.getId());
    }
    String name = StrUtil.isNotBlank(chineseName) ? chineseName : englishName;
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
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

  /**
   * 创建组
   *
   * @param groupName   组名称
   * @param description 组描述
   * @return /
   */
  @Override
  public Group createGroup(@NonNull String groupName, @NonNull String description) {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
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

  /**
   * 分支合并
   *
   * @param projectEnglishName 项目英文名称
   * @param sourceBranch       源分支名称
   * @param targetBranch       目标分支名称
   * @return /
   */
  @Override
  public MergeRequest mergeBranch(@NonNull String projectEnglishName, @NonNull String sourceBranch, @NonNull String targetBranch) {
    Project project = this.getProjectByName(projectEnglishName);
    return this.mergeBranch(project.getId(), sourceBranch, targetBranch);
  }

  /**
   * 分支合并到默认分支
   *
   * @param projectEnglishName 项目英文名称
   * @param sourceBranch       源分支名称
   * @return /
   */
  @Override
  public MergeRequest mergeDefaultBranch(@NonNull String projectEnglishName, @NonNull String sourceBranch) {
    Project project = this.getProjectByName(projectEnglishName);
    String defaultBranch = project.getDefaultBranch();
    return this.mergeBranch(project.getId(), sourceBranch, defaultBranch);
  }

  /**
   * 启动流水线
   *
   * @param projectEnglishName 项目英文名称
   * @param branchName         分支名称
   * @param variables          运行变量
   * @return /
   */
  @Override
  public Pipeline startPipeline(@NonNull String projectEnglishName, @NonNull String branchName, Map<String, String> variables) {
    Pipeline pipeline = this.createPipelineByProjectName(projectEnglishName, branchName, variables);
    return this.waitForPipelineCompletion(pipeline, null);
  }

  /**
   * 启动流水线
   *
   * @param projectEnglishName 项目英文名称
   * @param branchName         分支名称
   * @param variables          运行变量
   * @param listener           运行监听
   */
  @Override
  public void startPipelineWithListener(@NonNull String projectEnglishName, @NonNull String branchName, Map<String, String> variables, GitlabPipelineListener listener) {
    Pipeline pipeline = this.createPipelineByProjectName(projectEnglishName, branchName, variables);
    taskExecutor.execute(() -> GitlabRepositoryInitializer.this.waitForPipelineCompletion(pipeline, listener));
  }

  /**
   * 列出所有的分组
   *
   * @return 分组列表
   */
  public List<Group> listGroups() {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
      return gitLabApi.getGroupApi().getGroups();
    } catch (GitLabApiException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * 查询一个分组信息
   */
  private void getOneGroup(GitlabSiteConfigTb siteConfig) {
    try (GitLabApi gitLabApi = new GitLabApi(siteConfig.getEndpoint(), siteConfig.getToken())) {
      gitLabApi.getGroupApi().getGroups(1);
    } catch (GitLabApiException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * 同步等待流水线执行完成
   * <p>
   * 该方法会阻塞当前线程，直到流水线执行完成（成功或失败）或超时。 执行过程分为两个阶段： 1. 等待阶段：检测流水线是否从 PENDING 状态变为 RUNNING 状态，若超过最大重试次数则抛出异常 2. 监控阶段：持续监控流水线执行状态，直到成功或失败，若超过最大检查次数则抛出超时异常
   *
   * @param pipeline 流水线对象
   * @param listener 流水线状态监听器，用于回调各状态事件（可为null）
   * @return 执行完成的流水线对象
   * @throws BadRequestException 当流水线执行失败、挂起超时或运行超时时抛出
   */
  private Pipeline waitForPipelineCompletion(Pipeline pipeline, GitlabPipelineListener listener) {
    // 第一阶段：等待流水线从挂起状态变为运行状态
    GitlabProperties.Pipeline pipelineConfig = properties.getPipeline();
    long waitSeconds = pipelineConfig.getWaitSeconds();
    long maxRetryCount = pipelineConfig.getMaxRetryCount();
    long maxCheckCount = pipelineConfig.getMaxCheckCount();

    int retryCount = 1;
    while (true) {
      log.debug("检测流水线是否挂起中，最大重试次数：{}，当前重试次数：{}", maxRetryCount, retryCount);
      ThreadUtil.sleep(waitSeconds * 1000);
      pipeline = this.getPipelineById(pipeline.getProjectId(), pipeline.getId());

      // 流水线已开始运行，进入第二阶段监控
      if (PipelineStatus.RUNNING.equals(pipeline.getStatus())) {
        log.debug("流水线运行中");
        break;
      }
      // 流水线执行失败，直接抛出异常
      if (PipelineStatus.FAILED.equals(pipeline.getStatus())) {
        log.debug("流水线执行失败");
        if (listener != null) {
          listener.failed(pipeline);
        }
        throw new BadRequestException("流水线执行失败");
      }
      // 流水线处于挂起状态，继续等待或超时抛出异常
      if (PipelineStatus.PENDING.equals(pipeline.getStatus())) {
        log.debug("流水线挂起");
        retryCount++;
        if (retryCount > maxRetryCount) {
          if (listener != null) {
            listener.pending(pipeline);
          }
          throw new BadRequestException("流水线挂起，可能是由于.gitlab-ci.yml配置或者资源不足导致，请联系管理员处理");
        }
      }
    }

    // 第二阶段：监控流水线执行直到完成
    int checkCount = 1;
    Date startedAt = null;
    while (true) {
      ThreadUtil.sleep(waitSeconds * 1000);
      pipeline = this.getPipelineById(pipeline.getProjectId(), pipeline.getId());

      if (startedAt == null) {
        startedAt = pipeline.getStartedAt();
        if (startedAt != null && listener != null) {
          listener.start(pipeline);
        }
      }

      PipelineStatus status = pipeline.getStatus();
      String ref = pipeline.getRef();
      String source = pipeline.getSource();
      String webUrl = pipeline.getWebUrl();

      log.debug("getStatus -> 状态: {}", status);
      log.debug("getWebUrl -> 明细URL: {}", webUrl);
      log.debug("getRef -> 关联分支: {}", ref);
      log.debug("getSource -> 来源: {}", source);

      // 执行中
      if (PipelineStatus.RUNNING.equals(status)) {
        if (listener != null) {
          listener.running(pipeline);
        }
        continue;
      }

      // 执行失败
      if (PipelineStatus.FAILED.equals(status)) {
        log.debug("getFinishedAt -> 完成时间: {}", pipeline.getFinishedAt());
        log.debug("getDuration -> 持续时长(秒): {}", pipeline.getDuration());
        if (listener != null) {
          listener.failed(pipeline);
        }
        throw new BadRequestException("流水线执行失败");
      }

      // 执行成功
      if (PipelineStatus.SUCCESS.equals(status)) {
        log.debug("getFinishedAt -> 完成时间: {}", pipeline.getFinishedAt());
        log.debug("getDuration -> 持续时长(秒): {}", pipeline.getDuration());
        if (listener != null) {
          listener.success(pipeline);
        }
        break;
      }

      checkCount++;
      if (checkCount > maxCheckCount) {
        if (listener != null) {
          listener.pending(pipeline);
        }
        throw new BadRequestException("流水线运行超时，请联系管理员处理");
      }
    }

    return pipeline;
  }

  /**
   * 获取文件内容
   *
   * @param projectId      项目id
   * @param filePath       文件所在的路径
   * @param branchName     分支名称
   * @param includeContent 是否返回文件内容
   */
  private RepositoryFile getRepositoryFile(@NonNull Long projectId, @NonNull String filePath, @NonNull String branchName, @NonNull Boolean includeContent) {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
      return gitLabApi.getRepositoryFileApi().getFile(projectId, filePath, branchName, includeContent);
    } catch (GitLabApiException e) {
      if ("Not Found".equals(e.getReason())) {
        return null;
      }
      throw new BadRequestException("获取仓库文件失败");
    }
  }

  /**
   * 创建流水线
   *
   * @param projectEnglishName 项目英文名称
   * @param branchName         分支名称
   * @param variables          运行变量
   * @return /
   */
  private Pipeline createPipelineByProjectName(@NonNull String projectEnglishName, @NonNull String branchName, Map<String, String> variables) {
    Project project = this.getProjectByName(projectEnglishName);
    List<Pipeline> pendingPipelineList = this.listPipelinesByStatus(project.getId(), branchName, PipelineStatus.PENDING);
    if (CollUtil.isNotEmpty(pendingPipelineList)) {
      log.debug("存在挂起的流水线，停止并释放资源");
      this.cancelAndDeletePipeline(project, true, pendingPipelineList);
    }
    List<Pipeline> canceledPipelineList = this.listPipelinesByStatus(project.getId(), branchName, PipelineStatus.CANCELED);
    if (CollUtil.isNotEmpty(canceledPipelineList)) {
      log.debug("存在取消的任务，释放资源");
      this.cancelAndDeletePipeline(project, false, canceledPipelineList);
    }
    // 理论上只能有一个运行中的
    List<Pipeline> runningPipelineList = this.listPipelinesByStatus(project.getId(), branchName, PipelineStatus.RUNNING);
    if (CollUtil.isNotEmpty(runningPipelineList)) {
      log.debug("存在相同的运行中的流水线，停止并释放资源");
      this.cancelAndDeletePipeline(project, true, runningPipelineList);
    }
    // 创建新流水线
    return this.createPipelineByProjectId(project.getId(), branchName, variables);
  }

  /**
   * 停止并删除流水线
   *
   * @param project      /
   * @param needCancel   是否需要停止
   * @param pipelineList /
   */
  private void cancelAndDeletePipeline(Project project, boolean needCancel, List<Pipeline> pipelineList) {
    for (Pipeline pipeline : pipelineList) {
      if (needCancel) {
        // 取消任务
        this.cancelPipelineJobs(project.getId(), pipeline.getId());
      }
      // 删除流水线
      this.deletePipeline(project.getId(), pipeline.getId());
    }
  }

  /**
   * 停止指定项目的流水线
   *
   * @param projectId  项目id
   * @param pipelineId 流水线id
   */
  private void cancelPipelineJobs(@NonNull Long projectId, @NonNull Long pipelineId) {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
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
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
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
  private Pipeline createPipelineByProjectId(@NonNull Long projectId, @NonNull String branchName, Map<String, String> variables) {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
      Pipeline pipeline = gitLabApi.getPipelineApi().createPipeline(projectId, branchName, variables);
      log.debug("创建流水线成功, {}", JSON.toJSONString(pipeline));
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

  /**
   * 根据状态查询流水线列表
   *
   * @param projectId      项目id
   * @param branchName     分支名称
   * @param pipelineStatus 流水线状态
   * @return 流水线列表
   */
  private List<Pipeline> listPipelinesByStatus(@NonNull Long projectId, @NonNull String branchName, @NonNull PipelineStatus pipelineStatus) {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
      PipelineFilter pipelineFilter = new PipelineFilter();
      pipelineFilter.withRef(branchName);
      pipelineFilter.withStatus(pipelineStatus);
      return gitLabApi.getPipelineApi().getPipelines(projectId, pipelineFilter);
    } catch (GitLabApiException e) {
      throw new BadRequestException("根据状态查询流水线列表失败");
    }
  }

  /**
   * 根据流水线id查询
   *
   * @param projectId  项目id
   * @param pipelineId 流水线id
   * @return /
   */
  public Pipeline getPipelineById(@NonNull Long projectId, @NonNull Long pipelineId) {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
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
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
      gitLabApi.getPipelineApi().retryPipelineJob(projectId, pipelineId);
    } catch (GitLabApiException e) {
      throw new BadRequestException("流水线重试失败");
    }
  }

  /**
   * 创建文件
   *
   * @param projectId  项目id
   * @param branchName 分支名称
   * @param filePath   文件路径
   * @param content    文件内容
   */
  private void createRepositoryFile(Long projectId, String branchName, String filePath, String content) {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
      RepositoryFile repositoryFile = new RepositoryFile();
      repositoryFile.setEncoding(Constants.Encoding.TEXT);
      repositoryFile.setFilePath(filePath);
      repositoryFile.setContent(content);
      gitLabApi.getRepositoryFileApi().createFile(projectId, repositoryFile, branchName, SystemConst.CURRENT_APP_TITLE + "自动补充");
    } catch (GitLabApiException e) {
      throw new BadRequestException("创建仓库文件 " + filePath + " 失败");
    }
  }

  /**
   * 分支合并
   *
   * @param projectId    项目id
   * @param sourceBranch 源分支名称
   * @param targetBranch 目标分支名称
   * @return /
   */
  private MergeRequest mergeBranch(@NonNull Long projectId, @NonNull String sourceBranch, @NonNull String targetBranch) {
    List<MergeRequest> mergeRequests = this.listOpenedMergeRequestsByBranchName(projectId, sourceBranch, targetBranch);
    if (CollUtil.isNotEmpty(mergeRequests)) {
      for (MergeRequest request : mergeRequests) {
        this.deleteMergeRequestByIid(request.getProjectId(), request.getIid());
      }
    }

    MergeRequest mergeRequest = this.createMergeRequest(projectId, sourceBranch, targetBranch);

    GitlabProperties.Pipeline pipeline = properties.getPipeline();
    long waitSeconds = pipeline.getWaitSeconds();
    long maxRetryCount = pipeline.getMaxRetryCount();

    int retryCount = 1;
    while (true) {
      ThreadUtil.sleep(waitSeconds * 1000);
      mergeRequest = this.getMergeRequestByIid(projectId, mergeRequest.getIid());
      log.debug("getState: {}", mergeRequest.getState());
      log.debug("getMergeError: {}", mergeRequest.getMergeError());
      log.debug("getHasConflicts: {}", mergeRequest.getHasConflicts());
      log.debug("getDetailedMergeStatus: {}", mergeRequest.getDetailedMergeStatus());

      // 无变更，不需要合并，删除合并请求
      int changesCount = mergeRequest.getChangesCount() == null ? 0 : Integer.parseInt(mergeRequest.getChangesCount());
      if (changesCount <= 0) {
        this.deleteMergeRequestByIid(projectId, mergeRequest.getIid());
        return mergeRequest;
      }
      // 分支已合并
      if (Constants.MergeRequestState.MERGED.toValue().equals(mergeRequest.getState())) {
        return mergeRequest;
      }
      // 分支存在冲突
      if (Constants.MergeRequestState.OPENED.toValue().equals(mergeRequest.getState()) && mergeRequest.getHasConflicts()) {
        // 在线解决冲突地址 http://219.151.187.115:5000/test1/asdasd/-/merge_requests/7/conflicts
        throw new BadRequestException(String.format("%s 与 %s 存在冲突", sourceBranch, targetBranch));
      }
      // 允许合并
      if (Constants.MergeRequestState.OPENED.toValue().equals(mergeRequest.getState())) {
        return this.acceptMergeRequest(projectId, mergeRequest.getIid());
      }

      retryCount++;
      if (retryCount > maxRetryCount) {
        throw new BadRequestException("超过最大重试次数");
      }
    }
  }

  /**
   * 查询root用户的namespace
   *
   * @return /
   */
  private Namespace getRootNamespace() {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    return this.getNamespaceById(masterSiteConfig.getDefaultGroupId());
  }

  /**
   * 根据分组名称查询分组
   *
   * @param groupName 分组名称
   * @return /
   */
  @Override
  public Group getGroupByName(GitlabSiteConfigTb siteConfig, String groupName) {
    try (GitLabApi gitLabApi = new GitLabApi(siteConfig.getEndpoint(), siteConfig.getToken())) {
//      return gitLabApi.getGroupApi().getGroupsStream(groupName).filter(f -> f.getName().equals(groupName)).findFirst().orElse(null);
      Pager<Group> groups = gitLabApi.getGroupApi().getGroups(groupName, 1);
      return groups.current().stream().filter(f -> f.getName().equals(groupName)).findFirst().orElse(null);
    } catch (Exception e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * 根据id查询namespace
   *
   * @param id userId/GroupId
   * @return /
   */
  private Namespace getNamespaceById(@NonNull Long id) {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
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
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
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
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
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
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
//      return gitLabApi.getMergeRequestApi().getMergeRequestsStream(projectId)
//          .filter(f -> f.getSourceBranch().equals(sourceBranch))
//          .filter(f -> f.getTargetBranch().equals(targetBranch))
//          .filter(f -> "opened".equals(f.getState())).findFirst().orElse(null);
      MergeRequestFilter filter = new MergeRequestFilter();
      filter.setProjectId(projectId);
      filter.setSourceBranch(sourceBranch);
      filter.setTargetBranch(targetBranch);
      filter.setState(Constants.MergeRequestState.OPENED);
      filter.setOrderBy(Constants.MergeRequestOrderBy.CREATED_AT);
      return gitLabApi.getMergeRequestApi().getMergeRequests(filter).stream().findFirst().orElse(null);
    } catch (GitLabApiException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * 查询已打开的合并请求列表
   *
   * @param projectId    项目id
   * @param sourceBranch 源分支
   * @param targetBranch 目标分支
   * @return 合并请求列表
   */
  private List<MergeRequest> listOpenedMergeRequestsByBranchName(@NonNull Long projectId, @NonNull String sourceBranch, @NonNull String targetBranch) {
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
//      return gitLabApi.getMergeRequestApi().getMergeRequestsStream(projectId)
//          .filter(f -> f.getSourceBranch().equals(sourceBranch))
//          .filter(f -> f.getTargetBranch().equals(targetBranch))
//          .filter(f -> "opened".equals(f.getState())).toList();
      MergeRequestFilter filter = new MergeRequestFilter();
      filter.setProjectId(projectId);
      filter.setSourceBranch(sourceBranch);
      filter.setTargetBranch(targetBranch);
      filter.setState(Constants.MergeRequestState.OPENED);
      filter.setOrderBy(Constants.MergeRequestOrderBy.CREATED_AT);
      return gitLabApi.getMergeRequestApi().getMergeRequests(filter);
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
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
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
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
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
    GitlabSiteConfigTb masterSiteConfig = gitlabSiteConfigService.getMasterSiteConfig();
    try (GitLabApi gitLabApi = new GitLabApi(masterSiteConfig.getEndpoint(), masterSiteConfig.getToken())) {
//      return gitLabApi.getProjectApi().getProjectsStream().filter(f -> f.getPath().equals(projectEnglishName)).findFirst().orElse(null);
      Pager<Project> projects = gitLabApi.getProjectApi().getProjects(projectEnglishName, 1);
      return projects.current().stream().filter(f -> f.getPath().equals(projectEnglishName)).findFirst().orElse(null);
    } catch (GitLabApiException e) {
      throw new BadRequestException(e);
    }
  }
}
