package cn.odboy.host.dal.mysql;

import cn.odboy.host.dal.dataobject.HostAppTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 主机、应用关联关系 Mapper 接口
 * </p>
 *
 * @author codegen
 * @since 2026-03-27
 */
@Mapper
public interface HostAppMapper extends BaseMapper<HostAppTb> {

}
