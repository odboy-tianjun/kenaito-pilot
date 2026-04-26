package cn.odboy.pipeline.service;

import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 流水线实例 服务类
 * </p>
 *
 * @author codegen
 * @since 2026-04-25
 */
public interface PipelineInstanceService extends IService<PipelineInstanceTb> {

  /**
   * 更新流水线状态为失败
   *
   * @param instanceId 实例ID
   */
  void updateStatusFailure(String instanceId);

  /**
   * 更新流水线状态为成功
   *
   * @param instanceId 实例ID
   */
  void updateStatusSuccessByInstanceId(String instanceId);

  /**
   * 更新流水线的当前节点编码和状态
   *
   * @param instanceId 实例ID
   * @param nodeCode   节点编码
   * @param nodeStatus 节点状态
   */
  void updateCurrentNode(String instanceId, String nodeCode, String nodeStatus);
}
