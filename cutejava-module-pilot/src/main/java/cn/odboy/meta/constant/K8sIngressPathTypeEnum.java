package cn.odboy.meta.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * K8s Ingress 路由规则路径类型 枚举
 *
 * @author odboy
 */
@Getter
@AllArgsConstructor
public enum K8sIngressPathTypeEnum {

  EXACT("Exact", "精确匹配路径（如 /foo, 只匹配请求路径与之完全相同的 /foo）"),
  PREFIX("Prefix", "前缀匹配路径（如 /foo, 匹配请求路径以 /foo 开始的所有路径, /foo、/foo/bar 等）"),
  IMPLEMENTATION_SPECIFIC("ImplementationSpecific", "特定IngressController匹配（和具体的Ingress控制器有关）");

  private final String code;
  private final String name;
}