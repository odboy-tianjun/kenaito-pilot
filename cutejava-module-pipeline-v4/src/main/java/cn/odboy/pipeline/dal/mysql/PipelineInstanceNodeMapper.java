package cn.odboy.pipeline.dal.mysql;

import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineInstanceNodeMapper extends BaseMapper<PipelineInstanceNodeTb> {

}
