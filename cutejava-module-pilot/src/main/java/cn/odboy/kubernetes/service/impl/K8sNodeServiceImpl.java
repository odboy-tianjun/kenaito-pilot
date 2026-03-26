package cn.odboy.kubernetes.service.impl;

import cn.odboy.kubernetes.dal.dataobject.K8sNodeTb;
import cn.odboy.kubernetes.dal.mysql.K8sNodeMapper;
import cn.odboy.kubernetes.service.K8sNodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * K8s 节点 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Service
public class K8sNodeServiceImpl extends ServiceImpl<K8sNodeMapper, K8sNodeTb> implements K8sNodeService {

}
