package cn.odboy.pipeline;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * <p>
 * 流水线实例 服务实现类
 * </p>
 *
 * @author codegen
 * @since 2026-04-25
 */
@Service
public class PipelineInstanceServiceImpl extends ServiceImpl<PipelineInstanceMapper, PipelineInstanceTb> implements PipelineInstanceService {

  /**
   * 更新流水线状态为失败
   *
   * @param instanceId 实例ID
   */
  @Override
  public void updateStatusFailure(String instanceId) {
    LambdaUpdateWrapper<PipelineInstanceTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceTb::getId, instanceId);
    wrapper.set(PipelineInstanceTb::getStatus, PipelineStatusEnum.FAILURE.getCode());
    update(wrapper);
  }

  /**
   * 更新流水线状态为成功
   *
   * @param instanceId 实例ID
   */
  @Override
  public void updateStatusSuccessByInstanceId(String instanceId) {
    LambdaUpdateWrapper<PipelineInstanceTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceTb::getId, instanceId);
    wrapper.set(PipelineInstanceTb::getStatus, PipelineStatusEnum.SUCCESS.getCode());
    update(wrapper);
  }

  /**
   * 更新流水线的当前节点编码和状态
   *
   * @param instanceId 实例ID
   * @param nodeCode 节点编码
   * @param nodeStatus 节点状态
   */
  @Override
  public void updateCurrentNode(String instanceId, String nodeCode, String nodeStatus) {
    LambdaUpdateWrapper<PipelineInstanceTb> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PipelineInstanceTb::getId, instanceId);
    wrapper.set(PipelineInstanceTb::getCurrentNodeCode, nodeCode);
    wrapper.set(PipelineInstanceTb::getCurrentNodeStatus, nodeStatus);
    wrapper.set(PipelineInstanceTb::getUpdateTime, new Date());
    update(wrapper);
  }
}
