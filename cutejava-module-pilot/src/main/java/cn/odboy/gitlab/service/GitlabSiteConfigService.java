package cn.odboy.gitlab.service;

import cn.odboy.gitlab.dal.dataobject.GitlabSiteConfigTb;

/**
 * <p>
 * Gitlab站点配置 服务类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
public interface GitlabSiteConfigService {

  /**
   * 查询主配置
   *
   * @return /
   */
  GitlabSiteConfigTb getMasterSiteConfig();
}
