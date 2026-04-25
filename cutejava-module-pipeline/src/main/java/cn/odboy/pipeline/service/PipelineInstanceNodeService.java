package cn.odboy.pipeline.service;

import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 流水线实例节点 服务类
 * </p>
 *
 * @author codegen
 * @since 2026-04-25
 */
public interface PipelineInstanceNodeService extends IService<PipelineInstanceNodeTb> {

  void updateStatusRunning(String instanceId, String nodeCode);

  void updateStatusSuccess(String instanceId, String nodeCode);

  void updateStatusFailure(String instanceId, String nodeCode, String executeInfo);
}
