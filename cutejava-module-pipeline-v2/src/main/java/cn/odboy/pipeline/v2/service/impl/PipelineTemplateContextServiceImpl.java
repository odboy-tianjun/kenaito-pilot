package cn.odboy.pipeline.v2.service.impl;

import cn.odboy.pipeline.v2.dal.dataobject.PipelineTemplateContextTb;
import cn.odboy.pipeline.v2.dal.mysql.PipelineTemplateContextMapper;
import cn.odboy.pipeline.v2.service.PipelineTemplateContextService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * pipeline上下文模版 服务实现类
 * </p>
 *
 * @author odboy
 * @since 2026-04-25
 */
@Service
public class PipelineTemplateContextServiceImpl extends ServiceImpl<PipelineTemplateContextMapper, PipelineTemplateContextTb> implements PipelineTemplateContextService {

  @Override
  public PipelineTemplateContextTb getPipelineTemplateContext(String clusterType, String clusterCode, String contextType, String contextName) {
    return lambdaQuery()
        .eq(PipelineTemplateContextTb::getClusterType, clusterType)
        .eq(PipelineTemplateContextTb::getClusterCode, clusterCode)
        .eq(PipelineTemplateContextTb::getContextType, contextType)
        .eq(PipelineTemplateContextTb::getContextName, contextName)
        .one();
  }
}
