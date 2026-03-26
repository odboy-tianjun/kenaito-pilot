package cn.odboy.host.service.impl;

import cn.odboy.host.dal.dataobject.HostAppTb;
import cn.odboy.host.dal.mysql.HostAppMapper;
import cn.odboy.host.service.HostAppService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 主机、应用关联关系 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Service
public class HostAppServiceImpl extends ServiceImpl<HostAppMapper, HostAppTb> implements HostAppService {

}
