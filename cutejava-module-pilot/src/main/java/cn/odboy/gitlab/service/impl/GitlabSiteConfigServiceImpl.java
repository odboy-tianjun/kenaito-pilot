package cn.odboy.gitlab.service.impl;

import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.gitlab.dal.dataobject.GitlabSiteConfigTb;
import cn.odboy.gitlab.dal.mysql.GitlabSiteConfigMapper;
import cn.odboy.gitlab.service.GitlabSiteConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Gitlab站点配置 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Service
public class GitlabSiteConfigServiceImpl implements GitlabSiteConfigService {

  @Autowired
  private GitlabSiteConfigMapper gitlabSiteConfigMapper;

  @Override
  public GitlabSiteConfigTb getMasterSiteConfig() {
    LambdaQueryWrapper<GitlabSiteConfigTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(GitlabSiteConfigTb::getStatus, 1);
    wrapper.eq(GitlabSiteConfigTb::getMaster, 1);
    GitlabSiteConfigTb masterConfig = gitlabSiteConfigMapper.selectOne(wrapper);
    if (masterConfig == null) {
      throw new BadRequestException("必须至少配置一个Gitlab主站点");
    }
    return masterConfig;
  }
}
