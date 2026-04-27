package cn.odboy.pipeline.core;

import cn.odboy.pipeline.dal.model.NodeDefinition;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 流水线执行Job - Quartz Job类，负责执行整个流水线
 */
@Slf4j
@Component
public class PipelineExecutionJob implements Job {

  public static final String JOB_KEY_INSTANCE_ID = "INSTANCE_ID";
  public static final String JOB_KEY_NODES = "NODES";
  public static final String JOB_KEY_PARAMS = "PARAMS";

  @Autowired
  private PipelineExecutor pipelineExecutor;
  
  /**
   * 执行Job
   *
   * @param context Job执行上下文
   * @throws JobExecutionException 执行异常
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDataMap dataMap = context.getMergedJobDataMap();
  
    // 从 JobDataMap中获取流水线信息
    String instanceId = dataMap.getString(JOB_KEY_INSTANCE_ID);
    String nodesJson = dataMap.getString(JOB_KEY_NODES);
    String paramsJson = dataMap.getString(JOB_KEY_PARAMS);
    String retryNodeCode = dataMap.getString("RETRY_NODE_CODE"); // 重试节点编码（可选）
  
    log.info("开始执行Pipeline Job，实例ID: {}, 重试节点: {}", instanceId, retryNodeCode);
  
    try {
      // 反序列化节点定义
      List<NodeDefinition> nodes = JSON.parseArray(nodesJson, NodeDefinition.class);
  
      // 反序列化参数
      Map<String, Object> params = null;
      if (paramsJson != null && !paramsJson.isEmpty()) {
        params = JSON.parseObject(paramsJson, Map.class);
      }
  
      // 判断是正常执行还是重试
      if (retryNodeCode != null && !retryNodeCode.isEmpty()) {
        // 重试模式：从指定节点开始执行
        log.info("执行重试模式，从节点 {} 开始", retryNodeCode);
        pipelineExecutor.retryFromNode(instanceId, nodes, retryNodeCode, params);
      } else {
        // 正常模式：从头开始执行
        log.info("执行正常模式");
        pipelineExecutor.execute(instanceId, nodes, params);
      }
  
      log.info("Pipeline Job执行完成，实例ID: {}", instanceId);
  
    } catch (Exception e) {
      log.error("Pipeline Job执行失败，实例ID: {}", instanceId, e);
      // 抛出异常，让Quartz知道执行失败
      throw new JobExecutionException("流水线执行失败: " + e.getMessage(), e, false);
    }
  }
}
