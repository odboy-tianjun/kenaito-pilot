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
package cn.odboy.pilot.gitlab.service;

import cn.odboy.pilot.gitlab.core.GitlabPipelineListener;
import lombok.NonNull;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.Project;

import java.util.Map;

public interface GitlabService {

  /**
   * 创建项目
   *
   * @param groupName   所属组名称
   * @param chineseName 中文名称
   * @param englishName 英文名称
   * @param description 描述
   * @return /
   */
  Project createProject(String groupName, String chineseName, @NonNull String englishName, @NonNull String description);

  /**
   * 创建组
   *
   * @param groupName   组名称
   * @param description 组描述
   * @return /
   */
  Group createGroup(@NonNull String groupName, @NonNull String description);

  /**
   * 分支合并
   *
   * @param projectEnglishName 项目英文名称
   * @param sourceBranch       源分支名称
   * @param targetBranch       目标分支名称
   * @return /
   */
  MergeRequest mergeBranch(@NonNull String projectEnglishName, @NonNull String sourceBranch, @NonNull String targetBranch);

  /**
   * 启动流水线
   *
   * @param projectEnglishName 项目英文名称
   * @param branchName         分支名称
   * @param variables          运行变量
   * @return /
   */
  Pipeline startPipeline(@NonNull String projectEnglishName, @NonNull String branchName, Map<String, String> variables);

  /**
   * 启动流水线
   *
   * @param projectEnglishName 项目英文名称
   * @param branchName         分支名称
   * @param variables          运行变量
   * @param listener           运行监听
   */
  void startPipelineWithListener(@NonNull String projectEnglishName, @NonNull String branchName, Map<String, String> variables, GitlabPipelineListener listener);
}
