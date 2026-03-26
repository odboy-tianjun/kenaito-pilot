package cn.odboy.gitlab.dal.mysql;

import cn.odboy.gitlab.dal.dataobject.GitlabSiteConfigTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Gitlab站点配置 Mapper 接口
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Mapper
public interface GitlabSiteConfigMapper extends BaseMapper<GitlabSiteConfigTb> {

}
