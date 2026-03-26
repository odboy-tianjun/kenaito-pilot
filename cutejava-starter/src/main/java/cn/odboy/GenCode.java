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

package cn.odboy;

import cn.odboy.framework.mybatisplus.core.KitMpCmdGenUtil;

import java.util.List;

/**
 * 代码生成入口
 */
public class GenCode {

  private static final String ADDR = "127.0.0.1";
  private static final Integer PORT = 3306;
  private static final String DATABASE_NAME = "kenaito_pilot";
  private static final String DATABASE_USER = "root";
  private static final String DATABASE_PWD = "kd123456789";
  private static final String URL = String.format("jdbc:mysql://%s:%s/%s", ADDR, PORT, DATABASE_NAME);

  public static void main(String[] args) {
    KitMpCmdGenUtil generator = new KitMpCmdGenUtil();
    generator.setDatabaseUrl(URL);
    generator.setDriverClassName("com.mysql.cj.jdbc.Driver");
    generator.setDatabaseUsername(DATABASE_USER);
    generator.setDatabasePassword(DATABASE_PWD);
    genK8sCode(generator);
    genHostCode(generator);
    genAppCode(generator);
    genGitlabCode(generator);
  }

  private static void genK8sCode(KitMpCmdGenUtil generator) {
    generator.gen("kubernetes", "", List.of("k8s_node", "k8s_pod_specs", "k8s_service", "k8s_ingress"));
  }

  private static void genHostCode(KitMpCmdGenUtil generator) {
    generator.gen("host", "", List.of("host_info", "host_app"));
  }

  private static void genAppCode(KitMpCmdGenUtil generator) {
    generator.gen("app", "", List.of("app_info", "app_user_favorite", "app_role", "app_user_role"));
  }

  private static void genGitlabCode(KitMpCmdGenUtil generator) {
    generator.gen("gitlab", "", List.of("gitlab_site_config"));
  }
}
