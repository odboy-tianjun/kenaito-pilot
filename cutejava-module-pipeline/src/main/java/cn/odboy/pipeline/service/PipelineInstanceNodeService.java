package cn.odboy.pipeline.service;

import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface PipelineInstanceNodeService extends IService<PipelineInstanceNodeTb> {
  void updateStatusRunning(String instanceId, String nodeCode);
  void updateStatusSuccess(String instanceId, String nodeCode);
  void updateStatusFailure(String instanceId, String nodeCode, String executeInfo);
  List<PipelineInstanceNodeTb> listByInstanceId(String instanceId);

  // 【新增】重试专用：重置为待执行
  void updateStatusPending(String instanceId, String nodeCode);

  PipelineInstanceNodeTb getByInstanceIdAndNodeCode(String instanceId, String nextNodeCode);
}
