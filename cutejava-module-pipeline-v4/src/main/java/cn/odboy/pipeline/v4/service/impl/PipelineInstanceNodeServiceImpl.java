package cn.odboy.pipeline.v4.service.impl;

import cn.odboy.pipeline.v4.dal.dataobject.PipelineInstanceNodeTb;
import cn.odboy.pipeline.v4.dal.mysql.PipelineInstanceNodeMapper;
import cn.odboy.pipeline.v4.service.PipelineInstanceNodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PipelineInstanceNodeServiceImpl extends ServiceImpl<PipelineInstanceNodeMapper, PipelineInstanceNodeTb> implements PipelineInstanceNodeService {

}
