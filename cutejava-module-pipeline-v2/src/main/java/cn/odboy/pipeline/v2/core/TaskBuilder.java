package cn.odboy.pipeline.v2.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.odboy.framework.context.KitSpringBeanHolder;
import cn.odboy.framework.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务构建器 用于链式构建和执行任务(注意：本类不是线程安全的，不要在多线程间复用)
 */
public class TaskBuilder {

  private final List<NodeDefinition> nodes = new ArrayList<>();
  private final TaskContext context = new TaskContext();

  /**
   * 设置任务节点
   *
   * @param nodeDefinitions 节点定义
   * @return 当前构建器实例
   */
  public TaskBuilder nodes(List<NodeDefinition> nodeDefinitions) {
    if (CollUtil.isEmpty(nodeDefinitions)) {
      throw new BadRequestException("参数 节点定义(nodeDefinitions) 必填");
    }
    nodes.addAll(nodeDefinitions);
    return this;
  }

  /**
   * 设置上下文数据
   *
   * @param key   数据键
   * @param value 数据值
   * @return 当前构建器实例
   */
  public TaskBuilder withData(String key, Object value) {
    context.put(key, value);
    return this;
  }

  /**
   * 执行任务
   *
   * @return 任务执行结果
   */
  public TaskContext execute() {
    if (context.getTaskId() == null) {
      // 自动配置任务 id
      context.setTaskId(IdUtil.fastSimpleUUID());
    }
    TaskEngine engine = KitSpringBeanHolder.getBean(TaskEngine.class);
    engine.execute(nodes, context);
    return context;
  }

  /**
   * 设置任务 ID
   *
   * @param taskId 任务唯一标识
   * @return 当前构建器实例
   */
  public TaskBuilder taskId(String taskId) {
    if (StrUtil.isBlank(taskId)) {
      throw new BadRequestException("参数 任务ID(taskId) 必填");
    }
    context.setTaskId(taskId);
    return this;
  }

  public TaskBuilder clusterType(String clusterType) {
    if (StrUtil.isBlank(clusterType)) {
      throw new BadRequestException("参数 集群类型(clusterType) 必填");
    }
    context.setClusterType(clusterType);
    return this;
  }

  public TaskBuilder clusterCode(String clusterCode) {
    if (StrUtil.isBlank(clusterCode)) {
      throw new BadRequestException("参数 集群编码(clusterCode) 必填");
    }
    context.setClusterCode(clusterCode);
    return this;
  }

  public TaskBuilder contextType(String contextType) {
    if (StrUtil.isBlank(contextType)) {
      throw new BadRequestException("参数 上下文类型(contextType) 必填");
    }
    context.setContextType(contextType);
    return this;
  }

  public TaskBuilder contextName(String contextName) {
    if (StrUtil.isBlank(contextName)) {
      throw new BadRequestException("参数 上下文名称(contextName) 必填");
    }
    context.setContextName(contextName);
    return this;
  }
}
