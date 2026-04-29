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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.gitlab.dal.dataobject.GitlabSiteConfigTb;
import cn.odboy.gitlab.dal.mysql.GitlabSiteConfigMapper;
import cn.odboy.gitlab.repository.GitlabRepository;
import cn.odboy.gitlab.service.GitlabSiteConfigService;
import cn.odboy.meta.constant.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.gitlab4j.api.models.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Lazy
  @Autowired
  private GitlabRepository gitlabRepository;

  @Override
  public GitlabSiteConfigTb getMasterSiteConfig() {
    GitlabSiteConfigTb masterConfig = lambdaQuery().eq(GitlabSiteConfigTb::getStatus, StatusEnum.ENABLED.getCode())
        .eq(GitlabSiteConfigTb::getMaster, StatusEnum.ENABLED.getCode()).one();
    if (masterConfig == null) {
      throw new BadRequestException("必须至少配置一个Gitlab主站点");
    }
    return masterConfig;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void createSiteConfig(GitlabSiteConfigTb.CreateArgs args) {
    String name = StrUtil.trim(args.getName());
    String endpoint = StrUtil.trim(args.getEndpoint());
    String token = StrUtil.trim(args.getToken());
    String defaultGroupName = StrUtil.trim(args.getDefaultGroupName());

    GitlabSiteConfigTb record = new GitlabSiteConfigTb();
    record.setName(name);
    record.setEndpoint(endpoint);
    record.setToken(token);
    record.setDefaultGroupName(name);

    Group group = gitlabRepository.getGroupByName(record, defaultGroupName);
    record.setDefaultGroupId(group.getId());
    record.setStatus(true);

    boolean hasMaster = lambdaQuery().eq(GitlabSiteConfigTb::getMaster, StatusEnum.ENABLED.getCode()).count() > 0;
    record.setMaster(!hasMaster);

    save(record);
  }

  @Override
  public void updateSiteConfig(GitlabSiteConfigTb.UpdateArgs args) {
    if (args.getMaster()) {
      LambdaQueryWrapper<GitlabSiteConfigTb> wrapper = new LambdaQueryWrapper<>();
      wrapper.ne(GitlabSiteConfigTb::getId, args.getId());
      wrapper.eq(GitlabSiteConfigTb::getMaster, StatusEnum.ENABLED.getCode());
      if (count(wrapper) > 0) {
        throw new BadRequestException("已存在master节点，无法将当前节点配置为master");
      }
    }

    GitlabSiteConfigTb record = BeanUtil.copyProperties(args, GitlabSiteConfigTb.class);
    updateById(record);
  }
}
