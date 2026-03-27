package cn.odboy.meta.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 环境枚举
 *
 * @author odboy
 */
@Getter
@AllArgsConstructor
public enum EnvironmentEnum {

  DAILY("daily", "日常环境"),
  STAGE("stage", "预发环境"),
  PRODUCTION("production", "生产环境");

  private final String code;
  private final String name;
}