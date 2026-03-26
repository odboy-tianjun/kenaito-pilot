package cn.odboy.app.service.impl;

import cn.odboy.app.dal.dataobject.AppUserFavoriteTb;
import cn.odboy.app.dal.mysql.AppUserFavoriteMapper;
import cn.odboy.app.service.AppUserFavoriteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户收藏的应用 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Service
public class AppUserFavoriteServiceImpl extends ServiceImpl<AppUserFavoriteMapper, AppUserFavoriteTb> implements AppUserFavoriteService {

}
