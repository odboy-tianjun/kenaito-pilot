package cn.odboy.app.service.impl;

import cn.odboy.app.dal.dataobject.AppUserRoleTb;
import cn.odboy.app.dal.mysql.AppUserRoleMapper;
import cn.odboy.app.service.AppUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用角色、用户关联 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Service
public class AppUserRoleServiceImpl extends ServiceImpl<AppUserRoleMapper, AppUserRoleTb> implements AppUserRoleService {

}
