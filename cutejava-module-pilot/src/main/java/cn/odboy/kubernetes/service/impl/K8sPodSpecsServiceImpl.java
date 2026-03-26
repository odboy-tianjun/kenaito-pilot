package cn.odboy.kubernetes.service.impl;

import cn.odboy.kubernetes.dal.dataobject.K8sPodSpecsTb;
import cn.odboy.kubernetes.dal.mysql.K8sPodSpecsMapper;
import cn.odboy.kubernetes.service.K8sPodSpecsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * K8s Pod 规格 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Service
public class K8sPodSpecsServiceImpl extends ServiceImpl<K8sPodSpecsMapper, K8sPodSpecsTb> implements K8sPodSpecsService {

}
