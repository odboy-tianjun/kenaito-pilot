package cn.odboy.meta.util;

public class PilotNameUtil {

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
   * 生成Pod名称
   *
   * @param contextName 上下文名称
   * @param envCode     环境编码
   * @return /
   */
  public static String getPodName(String contextName, String envCode) {
    return String.format("%s-%s-pod", contextName, envCode);
  }
}
