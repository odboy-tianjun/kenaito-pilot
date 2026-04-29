package cn.odboy.pipeline.v3.dal.mysql;

import cn.odboy.pipeline.v3.dal.dataobject.PipelineInstanceNodeTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineInstanceNodeMapper extends BaseMapper<PipelineInstanceNodeTb> {

}
