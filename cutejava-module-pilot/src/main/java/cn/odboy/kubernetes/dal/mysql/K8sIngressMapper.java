package cn.odboy.kubernetes.dal.mysql;

import cn.odboy.kubernetes.dal.dataobject.K8sIngressTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * K8s Ingress 反向代理 Mapper 接口
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Mapper
public interface K8sIngressMapper extends BaseMapper<K8sIngressTb> {

}
