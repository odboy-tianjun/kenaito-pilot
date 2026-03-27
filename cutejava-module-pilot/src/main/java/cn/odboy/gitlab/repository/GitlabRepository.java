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
package cn.odboy.gitlab.repository;

import cn.odboy.gitlab.dal.dataobject.GitlabSiteConfigTb;
import cn.odboy.gitlab.infra.GitlabPipelineListener;
import lombok.NonNull;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.Project;

import java.util.Map;

public interface GitlabRepository {

  Project createProject(String groupName, String chineseName, @NonNull String englishName, @NonNull String description);

  Group createGroup(@NonNull String groupName, @NonNull String description);

  MergeRequest mergeBranch(@NonNull String projectEnglishName, @NonNull String sourceBranch, @NonNull String targetBranch);

  MergeRequest mergeDefaultBranch(@NonNull String projectEnglishName, @NonNull String sourceBranch);

  Pipeline startPipeline(@NonNull String projectEnglishName, @NonNull String branchName, Map<String, String> variables);

  void startPipelineWithListener(@NonNull String projectEnglishName, @NonNull String branchName, Map<String, String> variables, GitlabPipelineListener listener);

  /**
   * 根据分组名称查询
   *
   * @param config    /
   * @param groupName /
   * @return /
   */
  Group getGroupByName(GitlabSiteConfigTb config, String groupName);
}
