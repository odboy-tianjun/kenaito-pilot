package cn.odboy.pipeline.dal.mysql;

import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineInstanceMapper extends BaseMapper<PipelineInstanceTb> {

}
