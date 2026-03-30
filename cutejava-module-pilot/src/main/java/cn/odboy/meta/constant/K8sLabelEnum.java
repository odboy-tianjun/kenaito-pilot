package cn.odboy.meta.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * K8s标签枚举
 *
 * @author odboy
 */
@Getter
@AllArgsConstructor
public enum K8sLabelEnum {

  APP("pilot-app", "归属应用"),
  ENV("pilot-env", "归属环境"),
  RESOURCE_GROUP("pilot-resource-group", "归属资源组");

  private final String code;
  private final String name;
}
