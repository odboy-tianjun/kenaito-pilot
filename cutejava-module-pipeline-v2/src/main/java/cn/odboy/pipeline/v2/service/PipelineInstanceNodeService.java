package cn.odboy.pipeline.v2.service;

import cn.odboy.pipeline.v2.dal.dataobject.PipelineInstanceNodeTb;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PipelineInstanceNodeService extends IService<PipelineInstanceNodeTb> {

  void removeByInstanceId(String instanceId);
}
