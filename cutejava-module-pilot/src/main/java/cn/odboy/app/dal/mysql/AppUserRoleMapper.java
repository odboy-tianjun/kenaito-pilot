package cn.odboy.app.dal.mysql;

import cn.odboy.app.dal.dataobject.AppUserRoleTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 应用角色、用户关联 Mapper 接口
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Mapper
public interface AppUserRoleMapper extends BaseMapper<AppUserRoleTb> {

}
