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
package cn.odboy.pilot.gitlab.dal.model;

import cn.odboy.base.KitObject;
import lombok.Getter;
import lombok.Setter;

/**
 * Gitlab Runner 流水线构建上报
 */
@Getter
@Setter
public class GitlabRunnerBuildReportRequest extends KitObject {

  /**
   * 项目id
   */
  private String projectId;
  /**
   * 项目提交的完整SHA哈希值
   */
  private String commitId;
  /**
   * 流水线id
   */
  private String pipelineId;
  /**
   * 任务id
   */
  private String jobId;
  /**
   * 上下文名称
   */
  private String contextName;
  /**
   * 版本号
   */
  private String versionCode;
  /**
   * 环境编码
   */
  private String envCode;
  /**
   * 镜像名称（仅当status返回success是，这个字段是有效的）
   */
  private String imageName;
  /**
   * 状态： success：构建成功 failed：构建失败 canceled：任务被取消 skipped：任务被跳过 manual：手动执行
   */
  private String status;
}
