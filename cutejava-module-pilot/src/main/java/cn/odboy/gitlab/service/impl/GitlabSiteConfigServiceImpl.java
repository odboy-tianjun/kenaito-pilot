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

import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.gitlab.dal.dataobject.GitlabSiteConfigTb;
import cn.odboy.gitlab.dal.mysql.GitlabSiteConfigMapper;
import cn.odboy.gitlab.service.GitlabSiteConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Gitlab站点配置 服务实现类
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@Service
public class GitlabSiteConfigServiceImpl extends ServiceImpl<GitlabSiteConfigMapper, GitlabSiteConfigTb> implements GitlabSiteConfigService {

  @Override
  public GitlabSiteConfigTb getMasterSiteConfig() {
    GitlabSiteConfigTb masterConfig = lambdaQuery().eq(GitlabSiteConfigTb::getStatus, 1).eq(GitlabSiteConfigTb::getMaster, 1).one();
    if (masterConfig == null) {
      throw new BadRequestException("必须至少配置一个Gitlab主站点");
    }
    return masterConfig;
  }
}
