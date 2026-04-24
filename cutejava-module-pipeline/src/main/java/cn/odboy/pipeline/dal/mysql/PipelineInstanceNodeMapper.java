package cn.odboy.pipeline.dal.mysql;

import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PipelineInstanceNodeMapper extends BaseMapper<PipelineInstanceNodeTb> {

  default List<PipelineInstanceNodeTb> selectByInstanceId(String instanceId) {
    LambdaQueryWrapper<PipelineInstanceNodeTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(PipelineInstanceNodeTb::getInstanceId, instanceId);
    wrapper.orderByAsc(PipelineInstanceNodeTb::getCreateTime);
    return selectList(wrapper);
  }
}
