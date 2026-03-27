package cn.odboy.meta.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 应用级别枚举
 *
 * @author odboy
 */
@Getter
@AllArgsConstructor
public enum AppLevelEnum {

  A("A", "核心应用"),
  B("B", "非核心应用"),
  C("C", "边缘应用");

  private final String code;
  private final String name;
}