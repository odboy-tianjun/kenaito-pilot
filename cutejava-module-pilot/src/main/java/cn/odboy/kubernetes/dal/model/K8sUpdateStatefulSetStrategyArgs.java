package cn.odboy.kubernetes.dal.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class K8sUpdateStatefulSetStrategyArgs {

  @NotNull(message = "参数 部署批次 必填")
  private Integer batchSize = 1;
  @NotNull(message = "参数 首批部署POD数量 必填")
  private Integer firstBatchPodSize = 1;
  /**
   * NoPause：不暂停（默认策略）
   * FirstFinishPause：首批结束后暂停
   * AnyPause：每批暂停
   */
  @NotBlank(message = "参数 每批部署策略 必填")
  private String deployStrategy = "NoPause";
}
