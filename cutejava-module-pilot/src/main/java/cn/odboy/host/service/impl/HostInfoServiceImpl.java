package cn.odboy.host.service.impl;

import cn.odboy.host.dal.dataobject.HostInfoTb;
import cn.odboy.host.dal.mysql.HostInfoMapper;
import cn.odboy.host.service.HostInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 主机信息 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Service
public class HostInfoServiceImpl extends ServiceImpl<HostInfoMapper, HostInfoTb> implements HostInfoService {

}
