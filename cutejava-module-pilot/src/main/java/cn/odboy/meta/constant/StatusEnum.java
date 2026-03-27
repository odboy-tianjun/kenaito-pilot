package cn.odboy.meta.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态枚举
 *
 * @author odboy
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

  DISABLED(0, "禁用"),
  ENABLED(1, "启用");

  private final int code;
  private final String name;
}