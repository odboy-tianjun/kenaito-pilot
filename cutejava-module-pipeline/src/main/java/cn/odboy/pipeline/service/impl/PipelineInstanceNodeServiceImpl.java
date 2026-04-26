package cn.odboy.pipeline.service.impl;

import cn.odboy.pipeline.constant.PipelineStatusEnum;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import cn.odboy.pipeline.dal.mysql.PipelineInstanceNodeMapper;
import cn.odboy.pipeline.service.PipelineInstanceNodeService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class PipelineInstanceNodeServiceImpl extends ServiceImpl<PipelineInstanceNodeMapper, PipelineInstanceNodeTb> implements PipelineInstanceNodeService {

  @Override
  public void updateStatusRunning(String instanceId, String nodeCode) {
    LambdaUpdateWrapper<PipelineInstanceNodeTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceNodeTb::getInstanceId, instanceId);
    wrapper.eq(PipelineInstanceNodeTb::getBizCode, nodeCode);
    wrapper.set(PipelineInstanceNodeTb::getExecuteStatus, PipelineStatusEnum.RUNNING.getCode());
    wrapper.set(PipelineInstanceNodeTb::getStartTime, new Date());
    wrapper.set(PipelineInstanceNodeTb::getFinishTime, null);
    update(wrapper);
  }

  @Override
  public void updateStatusSuccess(String instanceId, String nodeCode) {
    LambdaUpdateWrapper<PipelineInstanceNodeTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceNodeTb::getInstanceId, instanceId);
    wrapper.eq(PipelineInstanceNodeTb::getBizCode, nodeCode);
    wrapper.set(PipelineInstanceNodeTb::getExecuteStatus, PipelineStatusEnum.SUCCESS.getCode());
    wrapper.set(PipelineInstanceNodeTb::getFinishTime, new Date());
    update(wrapper);
  }

  @Override
  public void updateStatusFailure(String instanceId, String nodeCode, String executeInfo) {
    LambdaUpdateWrapper<PipelineInstanceNodeTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceNodeTb::getInstanceId, instanceId);
    wrapper.eq(PipelineInstanceNodeTb::getBizCode, nodeCode);
    wrapper.set(PipelineInstanceNodeTb::getExecuteStatus, PipelineStatusEnum.FAILURE.getCode());
    wrapper.set(PipelineInstanceNodeTb::getExecuteInfo, executeInfo);
    wrapper.set(PipelineInstanceNodeTb::getFinishTime, new Date());
    update(wrapper);
  }

  @Override
  public List<PipelineInstanceNodeTb> listByInstanceId(String instanceId) {
    return lambdaQuery().eq(PipelineInstanceNodeTb::getInstanceId, instanceId).list();
  }

  // ======================
  // 【重试专用】重置为 PENDING
  // ======================
  @Override
  public void updateStatusPending(String instanceId, String nodeCode) {
    LambdaUpdateWrapper<PipelineInstanceNodeTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceNodeTb::getInstanceId, instanceId);
    wrapper.eq(PipelineInstanceNodeTb::getBizCode, nodeCode);
    wrapper.set(PipelineInstanceNodeTb::getExecuteStatus, PipelineStatusEnum.PENDING.getCode());
    wrapper.set(PipelineInstanceNodeTb::getStartTime, null);
    wrapper.set(PipelineInstanceNodeTb::getFinishTime, null);
    wrapper.set(PipelineInstanceNodeTb::getExecuteInfo, "");
    update(wrapper);
  }

  @Override
  public PipelineInstanceNodeTb getByInstanceIdAndNodeCode(String instanceId, String nextNodeCode) {
    return lambdaQuery().eq(PipelineInstanceNodeTb::getInstanceId, instanceId)
        .eq(PipelineInstanceNodeTb::getBizCode, nextNodeCode)
        .one();
  }
}
