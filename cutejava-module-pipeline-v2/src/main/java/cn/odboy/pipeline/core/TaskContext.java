package cn.odboy.pipeline.core;

import cn.odboy.base.KitObject;
import cn.odboy.framework.exception.BadRequestException;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * 任务上下文 用于在任务操作之间传递数据和共享状态
 */
@Getter
@Setter
public class TaskContext extends KitObject {

  /**
   * 任务唯一标识
   */
  private String taskId;
  /**
   * 集群类型
   */
  private String clusterType;
  /**
   * 集群编码
   */
  private String clusterCode;
  /**
   * 上下文类型
   */
  private String contextType;
  /**
   * 上下文名称
   */
  private String contextName;
  /**
   * 上下文数据存储
   */
  private Map<String, Object> data = new HashMap<>();

  /**
   * 获取上下文数据
   *
   * @param key 数据键
   * @param <T> 数据类型
   * @return 存储的数据值
   */
  @SuppressWarnings("all")
  public <T> T get(String key) {
    Object o = data.get(key);
    if (o == null) {
      throw new BadRequestException("参数 " + key + " 为null");
    }
    return (T) o;
  }

  /**
   * 存储上下文数据
   *
   * @param key   数据键
   * @param value 数据值
   */
  public void put(String key, Object value) {
    if (key == null) {
      throw new BadRequestException("上下文数据键 (key) 不能为 null");
    }
    data.put(key, value);
  }
}
