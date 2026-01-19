package cn.odboy.system.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SystemMenuTypeEnum {
  DIR(0, "目录"),
  MENU(1, "菜单"),
  BUTTON(3, "按钮");

  private final Integer code;
  private final String name;
}
