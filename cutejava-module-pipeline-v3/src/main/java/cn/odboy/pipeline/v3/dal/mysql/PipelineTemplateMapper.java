package cn.odboy.pipeline.v3.dal.mysql;

import cn.odboy.pipeline.v3.dal.dataobject.PipelineTemplateTb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 流水线模板 Mapper 接口
 * </p>
 *
 * @author odboy
 * @since 2026-04-25
 */
@Mapper
public interface PipelineTemplateMapper extends BaseMapper<PipelineTemplateTb> {

}
