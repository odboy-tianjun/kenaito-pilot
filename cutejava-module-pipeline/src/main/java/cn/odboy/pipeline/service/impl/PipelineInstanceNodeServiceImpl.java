package cn.odboy.pipeline.service.impl;

import cn.odboy.pipeline.constant.PipelineStatusEnum;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import cn.odboy.pipeline.dal.mysql.PipelineInstanceNodeMapper;
import cn.odboy.pipeline.service.PipelineInstanceNodeService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * <p>
 * 流水线实例节点 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-04-25
 */
@Service
public class PipelineInstanceNodeServiceImpl extends ServiceImpl<PipelineInstanceNodeMapper, PipelineInstanceNodeTb> implements PipelineInstanceNodeService {

  @Override
  public void updateStatusRunning(String instanceId, String nodeCode) {
    LambdaUpdateWrapper<PipelineInstanceNodeTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceNodeTb::getInstanceId, instanceId);
    wrapper.eq(PipelineInstanceNodeTb::getBizCode, nodeCode);
    wrapper.set(PipelineInstanceNodeTb::getExecuteStatus, PipelineStatusEnum.RUNNING.getCode());
    wrapper.set(PipelineInstanceNodeTb::getExecuteInfo, "");
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
    wrapper.set(PipelineInstanceNodeTb::getExecuteInfo, "");
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
}
