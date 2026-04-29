package cn.odboy.pipeline.service;

import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PipelineInstanceNodeService extends IService<PipelineInstanceNodeTb> {

  void removeByInstanceId(String instanceId);
}
