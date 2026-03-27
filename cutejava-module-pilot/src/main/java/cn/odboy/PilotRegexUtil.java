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
package cn.odboy;

import cn.hutool.core.util.ReUtil;
import cn.odboy.framework.exception.BadRequestException;

/**
 * 正则表达式工具
 */
public class PilotRegexUtil {

  /**
   * 校验应用名称
   *
   * @param appName 应用名称
   */
  public static void validAppName(String appName) {
    boolean result = ReUtil.isMatch("^[a-zA-Z0-9_][a-zA-Z0-9_\\-.]{0,99}$", appName);
    if (!result) {
      throw new BadRequestException("应用名称：只能包含字母和数字、 '_'、 '.'和'-'，且只能以字母、数字或'_'开头");
    }
  }
}
