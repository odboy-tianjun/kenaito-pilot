package cn.odboy.app.service.impl;

import cn.odboy.app.dal.dataobject.AppInfoTb;
import cn.odboy.app.dal.mysql.AppInfoMapper;
import cn.odboy.app.service.AppInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用信息 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Service
public class AppInfoServiceImpl extends ServiceImpl<AppInfoMapper, AppInfoTb> implements AppInfoService {

}
