package cn.odboy.pipeline.v2.service.impl;

import cn.odboy.pipeline.v2.dal.dataobject.PipelineInstanceNodeTb;
import cn.odboy.pipeline.v2.dal.mysql.PipelineInstanceNodeMapper;
import cn.odboy.pipeline.v2.service.PipelineInstanceNodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PipelineInstanceNodeServiceImpl extends ServiceImpl<PipelineInstanceNodeMapper, PipelineInstanceNodeTb> implements PipelineInstanceNodeService {

  @Override
  public void removeByInstanceId(String instanceId) {
    lambdaUpdate().eq(PipelineInstanceNodeTb::getInstanceId, instanceId).remove();
  }
}
