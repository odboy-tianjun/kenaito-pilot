package cn.odboy.meta.util;

public class K8sNameUtil {

  /**
   * 生成Service名称
   *
   * @param contextName 上下文名称
   * @param envCode     环境编码
   * @return /
   */
  public static String getServiceName(String contextName, String envCode) {
    return String.format("%s-%s-svc", contextName, envCode);
  }

  /**
   * 生成Ingress名称
   *
   * @param contextName 上下文名称
   * @param envCode     环境编码
   * @param versionCode 版本号
   * @return /
   */
  public static String getIngressName(String contextName, String envCode, String versionCode) {
    return String.format("%s-%s-%s-ing", contextName, envCode, versionCode);
  }

  /**
   * 生成Pod名称
   *
   * @param contextName 上下文名称
   * @param envCode     环境编码
   * @return /
   */
  public static String getPodName(String contextName, String envCode) {
    return String.format("%s-%s-pod", contextName, envCode);
  }

  /**
   * 生成业务Pod名称
   *
   * @param contextName 上下文名称
   * @param envCode     环境编码
   * @return /
   */
  public static String getPodBizName(String contextName, String envCode) {
    return String.format("%s-%s-biz", contextName, envCode);
  }
}
