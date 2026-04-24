package cn.odboy.pipeline.core;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局上下文
 */
@Data
public class PipelineContext {

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
