package cn.odboy.meta.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 应用部署类型枚举
 *
 * @author odboy
 */
@Getter
@AllArgsConstructor
public enum AppDeployTypeEnum {

  STATIC("static", "静态产物"),
  DOTNET("backend", "后端服务");

  private final String code;
  private final String name;
}