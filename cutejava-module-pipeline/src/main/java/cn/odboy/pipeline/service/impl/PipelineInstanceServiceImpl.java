package cn.odboy.pipeline.service.impl;

import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.dal.mysql.PipelineInstanceMapper;
import cn.odboy.pipeline.service.PipelineInstanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
