package cn.odboy.kubernetes.service.impl;

import cn.odboy.kubernetes.dal.dataobject.K8sServiceTb;
import cn.odboy.kubernetes.dal.mysql.K8sServiceMapper;
import cn.odboy.kubernetes.service.K8sServiceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * K8s Service 负载均衡与服务发现 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Service
public class K8sServiceServiceImpl extends ServiceImpl<K8sServiceMapper, K8sServiceTb> implements K8sServiceService {

}
