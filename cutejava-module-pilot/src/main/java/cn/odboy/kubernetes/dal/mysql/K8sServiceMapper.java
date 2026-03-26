package cn.odboy.kubernetes.dal.mysql;

import cn.odboy.kubernetes.dal.dataobject.K8sServiceTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * K8s Service 负载均衡与服务发现 Mapper 接口
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Mapper
public interface K8sServiceMapper extends BaseMapper<K8sServiceTb> {

}
