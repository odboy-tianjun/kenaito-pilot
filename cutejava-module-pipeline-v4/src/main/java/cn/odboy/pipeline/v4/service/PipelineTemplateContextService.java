package cn.odboy.pipeline.v4.service;

import cn.odboy.pipeline.v4.dal.dataobject.PipelineTemplateContextTb;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * pipeline上下文模版 服务类
 * </p>
 *
 * @author odboy
 * @since 2026-04-25
 */
public interface PipelineTemplateContextService extends IService<PipelineTemplateContextTb> {

  PipelineTemplateContextTb getPipelineTemplateContext(String clusterType, String clusterCode, String contextType, String contextName);
}
