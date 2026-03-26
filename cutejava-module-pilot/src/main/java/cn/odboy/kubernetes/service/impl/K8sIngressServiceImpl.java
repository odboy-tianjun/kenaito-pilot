package cn.odboy.kubernetes.service.impl;

import cn.odboy.kubernetes.dal.dataobject.K8sIngressTb;
import cn.odboy.kubernetes.dal.mysql.K8sIngressMapper;
import cn.odboy.kubernetes.service.K8sIngressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * K8s Ingress 反向代理 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Service
public class K8sIngressServiceImpl extends ServiceImpl<K8sIngressMapper, K8sIngressTb> implements K8sIngressService {

}
