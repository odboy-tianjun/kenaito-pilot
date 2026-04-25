package cn.odboy.pipeline;

import cn.odboy.base.KitObject;
import lombok.Getter;
import lombok.Setter;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局上下文
 */
@Getter
@Setter
public class PipelineContext extends KitObject {

  private String taskStatus;
  private final Map<String, Object> params = new HashMap<>();

  public void setParam(String key, Object value) {
    params.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getParam(String key) {
    return (T) params.get(key);
  }
}
