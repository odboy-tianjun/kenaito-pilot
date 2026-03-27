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
package cn.odboy.gitlab.controller;

import cn.odboy.gitlab.dal.dataobject.GitlabSiteConfigTb;
import cn.odboy.gitlab.service.GitlabSiteConfigService;
import cn.odboy.system.framework.operalog.OperationLog;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Gitlab站点配置 前端控制器
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@RestController
@RequestMapping("/api/gitlabSiteConfig")
public class GitlabSiteConfigController {

  @Autowired
  private GitlabSiteConfigService gitlabSiteConfigService;

  @OperationLog
  @ApiOperation("新增Gitlab站点配置")
  @PostMapping(value = "/createSiteConfig")
  public ResponseEntity<Void> createSiteConfig(@Validated @RequestBody GitlabSiteConfigTb.CreateArgs args) {
    gitlabSiteConfigService.createSiteConfig(args);
    return ResponseEntity.ok(null);
  }

  @OperationLog
  @ApiOperation("修改Gitlab站点配置")
  @PostMapping(value = "/updateSiteConfig")
  public ResponseEntity<Void> updateSiteConfig(@Validated @RequestBody GitlabSiteConfigTb.UpdateArgs args) {
    gitlabSiteConfigService.updateSiteConfig(args);
    return ResponseEntity.ok(null);
  }

}
