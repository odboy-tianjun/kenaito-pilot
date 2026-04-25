package cn.odboy.pipeline.dal.mysql;

import cn.odboy.pipeline.constant.PipelineStatusEnum;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PipelineInstanceMapper extends BaseMapper<PipelineInstanceTb> {
  default List<PipelineInstanceTb> selectUnfinished() {
    LambdaQueryWrapper<PipelineInstanceTb> wrapper = new LambdaQueryWrapper<>();
    wrapper.in(PipelineInstanceTb::getStatus,
        PipelineStatusEnum.PENDING.getCode(),
        PipelineStatusEnum.RUNNING.getCode());
    return selectList(wrapper);
  }
}
