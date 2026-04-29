package cn.odboy.pipeline.v2.dal.mysql;

import cn.odboy.pipeline.v2.dal.dataobject.PipelineInstanceTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineInstanceMapper extends BaseMapper<PipelineInstanceTb> {

}
