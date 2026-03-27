/*
 * Copyright 2021-2026 Odboy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.odboy.meta.controller;

import cn.odboy.base.KitSelectOptionVo;
import cn.odboy.framework.exception.BadRequestException;
import cn.odboy.meta.constant.AppDeployTypeEnum;
import cn.odboy.meta.constant.AppLanguageEnum;
import cn.odboy.meta.constant.AppLevelEnum;
import cn.odboy.meta.constant.EnvironmentEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pilot枚举控制器
 *
 * @author odboy
 */
@RestController
@Api(tags = "系统：枚举管理")
@RequestMapping("/api/enum")
public class PilotEnumController {

  /**
   * 枚举类型映射
   */
  private enum EnumType {
    APP_LANGUAGE("AppLanguage", AppLanguageEnum.class),
    APP_LEVEL("AppLevel", AppLevelEnum.class),
    APP_DEPLOY_TYPE("AppDeployType", AppDeployTypeEnum.class),
    ENVIRONMENT("Environment", EnvironmentEnum.class);

    private final String code;
    private final Class<? extends Enum<?>> enumClass;

    EnumType(String code, Class<? extends Enum<?>> enumClass) {
      this.code = code;
      this.enumClass = enumClass;
    }

    public static Class<? extends Enum<?>> getEnumClass(String code) {
      for (EnumType type : values()) {
        if (type.code.equals(code)) {
          return type.enumClass;
        }
      }
      return null;
    }
  }

  /**
   * 获取指定枚举的选项列表 返回格式: [{ label: name, value: code }]
   *
   * @param enumCode 枚举代码，如: appLanguage, environment, fileType, dataScope
   * @return 枚举选项列表
   */
  @ApiOperation("获取枚举选项列表")
  @PostMapping("/{enumCode}")
  public ResponseEntity<List<KitSelectOptionVo>> getEnumOptions(@PathVariable String enumCode) {
    Class<? extends Enum<?>> enumClass = EnumType.getEnumClass(enumCode);
    if (enumClass == null) {
      throw new BadRequestException("不支持的枚举类型: " + enumCode);
    }
    return ResponseEntity.ok(this.convertToOptions(enumClass));
  }

  /**
   * 将枚举转换为选项列表 适配规则：优先使用 getCode() 和 getName()，如果不存在则使用 name()
   *
   * @param enumClass 枚举类
   * @return 选项列表
   */
  private List<KitSelectOptionVo> convertToOptions(Class<? extends Enum<?>> enumClass) {
    Enum<?>[] enumConstants = enumClass.getEnumConstants();
    if (enumConstants == null || enumConstants.length == 0) {
      return List.of();
    }

    // 检查是否存在 getCode() 和 getName() 方法
    boolean hasCodeMethod = this.hasMethod(enumClass, "getCode");
    boolean hasNameMethod = this.hasMethod(enumClass, "getName");

    return Arrays.stream(enumConstants)
        .map(enumConstant -> {
          String code = this.extractCode(enumConstant, hasCodeMethod);
          String name = this.extractName(enumConstant, hasNameMethod);
          return KitSelectOptionVo.builder()
              .label(name)
              .value(code)
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * 提取 code 值
   */
  private String extractCode(Enum<?> enumConstant, boolean hasCodeMethod) {
    try {
      if (hasCodeMethod) {
        Method method = enumConstant.getClass().getMethod("getCode");
        return (String) method.invoke(enumConstant);
      }
    } catch (Exception e) {
      // 忽略反射异常，使用默认值
    }
    return enumConstant.name();
  }

  /**
   * 提取 name 值
   */
  private String extractName(Enum<?> enumConstant, boolean hasNameMethod) {
    try {
      if (hasNameMethod) {
        Method method = enumConstant.getClass().getMethod("getName");
        return (String) method.invoke(enumConstant);
      }
    } catch (Exception e) {
      // 忽略反射异常，使用默认值
    }
    return enumConstant.name();
  }

  /**
   * 检查类是否存在指定方法
   */
  private boolean hasMethod(Class<?> clazz, String methodName) {
    try {
      clazz.getMethod(methodName);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }
}
