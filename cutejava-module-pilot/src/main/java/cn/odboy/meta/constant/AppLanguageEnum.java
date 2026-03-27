package cn.odboy.meta.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 应用语言枚举
 *
 * @author odboy
 */
@Getter
@AllArgsConstructor
public enum AppLanguageEnum {

  C_PLUS("cplus", "C++"),
  DOTNET("dotnet", ".Net"),
  GO("go", "Go"),
  JAVA("java", "Java"),
  NODEJS("nodejs", "Node.js"),
  PYTHON("python", "Python"),
  ANDROID("android", "Android"),
  IOS("ios", "iOS");

  private final String code;
  private final String name;
}