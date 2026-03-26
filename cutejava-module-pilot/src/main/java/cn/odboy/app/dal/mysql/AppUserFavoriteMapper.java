package cn.odboy.app.dal.mysql;

import cn.odboy.app.dal.dataobject.AppUserFavoriteTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户收藏的应用 Mapper 接口
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Mapper
public interface AppUserFavoriteMapper extends BaseMapper<AppUserFavoriteTb> {

}
