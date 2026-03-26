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
package cn.odboy.pilot.gitlab.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gitlab")
public class GitlabProperties {

  /**
   * gitlab地址
   */
  private String endpoint;
  /**
   * 令牌。懂得都懂
   */
  private String token;
  /**
   * root用户的id
   */
  private Long rootId;
  /**
   * 流水线相关
   */
  private Pipeline pipeline;

  @Getter
  @Setter
  public static class Pipeline {

    /**
     * 每次调用接口等待时长
     */
    private long waitSeconds = 5;
    /**
     * 流水线运行前置最大重试次数(maxRetryCount * waitSeconds)
     */
    private long maxRetryCount = 30;
    /**
     * 流水线运行中最大检测次数(maxCheckCount * waitSeconds)
     */
    private long maxCheckCount = 1440;
  }
}
