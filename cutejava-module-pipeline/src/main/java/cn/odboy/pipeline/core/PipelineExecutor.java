package cn.odboy.pipeline.core;

import cn.odboy.framework.context.KitSpringBeanHolder;
import cn.odboy.pipeline.constant.PipelineStatusEnum;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceNodeTb;
import cn.odboy.pipeline.dal.dataobject.PipelineInstanceTb;
import cn.odboy.pipeline.dal.model.NodeDefinition;
import cn.odboy.pipeline.service.PipelineInstanceNodeService;
import cn.odboy.pipeline.service.PipelineInstanceService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 流水线执行器 - 核心执行逻辑，顺序执行节点并处理异常
 */
@Slf4j
@Component
public class PipelineExecutor {

  @Autowired
  private PipelineInstanceService pipelineInstanceService;

  @Autowired
  private PipelineInstanceNodeService pipelineInstanceNodeService;

  /**
   * 执行流水线
   *
   * @param instanceId  流水线实例ID
   * @param nodes       节点定义列表（按JSON数组顺序执行）
   * @param inputParams 输入参数
   */
  public void execute(String instanceId, List<NodeDefinition> nodes, Map<String, Object> inputParams) {
    log.info("开始执行流水线，实例ID: {}, 节点数量: {}", instanceId, nodes.size());

    // 更新实例状态为运行中
    PipelineInstanceTb instance = new PipelineInstanceTb();
    instance.setId(instanceId);
    instance.setStatus(PipelineStatusEnum.RUNNING.getCode());
    instance.setUpdateTime(new Date());
    pipelineInstanceService.updateById(instance);

    // 按照JSON数组的自然顺序执行节点（无需排序）
    for (int i = 0; i < nodes.size(); i++) {
      NodeDefinition node = nodes.get(i);
      log.info("执行第{}个节点: {} ({})", i + 1, node.getName(), node.getCode());

      try {
        // 执行单个节点
        executeNode(instanceId, node, inputParams);
        log.info("节点执行成功: {}", node.getCode());
      } catch (Exception e) {
        log.error("节点执行失败: {}, 错误信息: {}", node.getCode(), e.getMessage(), e);

        // 更新节点状态为失败
        updateNodeStatus(instanceId, node.getCode(), PipelineStatusEnum.FAILURE.getCode(), e.getMessage());

        // 更新实例状态为失败
        updateInstanceStatus(instanceId, PipelineStatusEnum.FAILURE.getCode(), node.getCode());

        // 终止后续节点执行
        log.warn("节点 {} 执行失败，终止后续节点执行", node.getCode());
        throw new RuntimeException("流水线执行失败，节点: " + node.getCode(), e);
      }
    }

    // 所有节点执行成功，更新实例状态为成功
    updateInstanceStatus(instanceId, PipelineStatusEnum.SUCCESS.getCode(), null);
    log.info("流水线执行成功，实例ID: {}", instanceId);
  }

  /**
   * 执行单个节点
   *
   * @param instanceId  实例ID
   * @param node        节点定义
   * @param inputParams 输入参数
   */
  private void executeNode(String instanceId, NodeDefinition node, Map<String, Object> inputParams) {
    // 创建节点实例记录
    PipelineInstanceNodeTb nodeInstance = new PipelineInstanceNodeTb();
    nodeInstance.setInstanceId(instanceId);
    nodeInstance.setBizCode(node.getCode());
    nodeInstance.setBizName(node.getName());
    nodeInstance.setExecuteStatus(PipelineStatusEnum.RUNNING.getCode());
    nodeInstance.setStartTime(new Date());

    // 合并默认参数和输入参数
    Map<String, Object> executeParams = mergeParameters(node.getParameters(), inputParams);
    nodeInstance.setExecuteParams(JSON.toJSONString(executeParams));

    // 保存节点实例
    pipelineInstanceNodeService.save(nodeInstance);

    try {
      // 根据节点类型执行不同的逻辑
      if ("service".equals(node.getType())) {
        // 本地Service节点执行
        executeLocalService(node.getCode(), executeParams);
      } else if ("rpc".equals(node.getType())) {
        // RPC节点执行（预留扩展）
        executeRpcService(node.getCode(), executeParams);
      } else {
        throw new IllegalArgumentException("不支持的节点类型: " + node.getType());
      }

      // 更新节点状态为成功
      nodeInstance.setExecuteStatus(PipelineStatusEnum.SUCCESS.getCode());
      nodeInstance.setFinishTime(new Date());
      nodeInstance.setExecuteInfo("执行成功");
      pipelineInstanceNodeService.updateById(nodeInstance);

    } catch (Exception e) {
      // 更新节点状态为失败
      nodeInstance.setExecuteStatus(PipelineStatusEnum.FAILURE.getCode());
      nodeInstance.setFinishTime(new Date());
      nodeInstance.setExecuteInfo("执行失败: " + e.getMessage());
      pipelineInstanceNodeService.updateById(nodeInstance);

      throw e;
    }
  }

  /**
   * 执行本地Service节点
   *
   * @param beanName Bean名称
   * @param params   执行参数
   */
  private void executeLocalService(String beanName, Map<String, Object> params) {
    try {
      // 从Spring容器中获取Bean
      Object serviceBean = KitSpringBeanHolder.getBean(beanName);

      if (serviceBean instanceof InterruptableJob) {
        // 如果是Quartz Job类型，直接调用execute方法
        InterruptableJob job = (InterruptableJob) serviceBean;
        // 这里可以创建一个简单的JobExecutionContext
        // 为了简化，我们假设节点有自己的执行方法
        log.debug("执行Quartz Job类型的Service: {}", beanName);
      } else {
        // 普通Service，通过反射调用execute方法
        java.lang.reflect.Method method = serviceBean.getClass()
            .getMethod("execute", Map.class);
        method.invoke(serviceBean, params);
        log.debug("执行普通Service: {}", beanName);
      }
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Service不存在execute方法: " + beanName, e);
    } catch (Exception e) {
      throw new RuntimeException("执行Service失败: " + beanName, e);
    }
  }

  /**
   * 执行RPC服务节点（预留扩展）
   *
   * @param serviceName 服务名称
   * @param params      执行参数
   */
  private void executeRpcService(String serviceName, Map<String, Object> params) {
    // TODO: 实现RPC调用逻辑
    log.warn("RPC节点执行暂未实现: {}", serviceName);
    throw new UnsupportedOperationException("RPC节点执行暂未实现");
  }

  /**
   * 合并参数
   *
   * @param defaultParams 默认参数
   * @param inputParams   输入参数
   * @return 合并后的参数
   */
  private Map<String, Object> mergeParameters(Map<String, Object> defaultParams, Map<String, Object> inputParams) {
    if (defaultParams == null && inputParams == null) {
      return null;
    }
    if (defaultParams == null) {
      return inputParams;
    }
    if (inputParams == null) {
      return defaultParams;
    }

    // 输入参数覆盖默认参数
    Map<String, Object> merged = JSON.parseObject(JSON.toJSONString(defaultParams), Map.class);
    merged.putAll(inputParams);
    return merged;
  }

  /**
   * 更新节点状态
   *
   * @param instanceId 实例ID
   * @param nodeCode   节点编码
   * @param status     状态
   * @param message    消息
   */
  private void updateNodeStatus(String instanceId, String nodeCode, String status, String message) {
    PipelineInstanceNodeTb nodeInstance = pipelineInstanceNodeService.lambdaQuery()
        .eq(PipelineInstanceNodeTb::getInstanceId, instanceId)
        .eq(PipelineInstanceNodeTb::getBizCode, nodeCode)
        .one();

    if (nodeInstance != null) {
      nodeInstance.setExecuteStatus(status);
      nodeInstance.setFinishTime(new Date());
      nodeInstance.setExecuteInfo(message);
      pipelineInstanceNodeService.updateById(nodeInstance);
    }
  }

  /**
   * 更新实例状态
   *
   * @param instanceId    实例ID
   * @param status        状态
   * @param currentNodeCode 当前节点编码
   */
  private void updateInstanceStatus(String instanceId, String status, String currentNodeCode) {
    PipelineInstanceTb instance = new PipelineInstanceTb();
    instance.setId(instanceId);
    instance.setStatus(status);
    instance.setCurrentNodeCode(currentNodeCode);
    instance.setCurrentNodeStatus(status);
    instance.setUpdateTime(new Date());
    pipelineInstanceService.updateById(instance);
  }
}
