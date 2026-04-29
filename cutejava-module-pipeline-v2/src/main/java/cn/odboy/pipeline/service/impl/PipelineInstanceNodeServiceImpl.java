package cn.odboy.pipeline.service.impl;

import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import cn.odboy.pipeline.dal.mysql.PipelineInstanceNodeMapper;
import cn.odboy.pipeline.service.PipelineInstanceNodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PipelineInstanceNodeServiceImpl extends ServiceImpl<PipelineInstanceNodeMapper, PipelineInstanceNodeTb> implements PipelineInstanceNodeService {

  @Override
  public void removeByInstanceId(String instanceId) {
    lambdaUpdate().eq(PipelineInstanceNodeTb::getInstanceId, instanceId).remove();
  }
}
