package cn.odboy.pipeline.v2.util;

import cn.odboy.pipeline.v2.core.TaskContext;

public class PipelineNameUtil {

  public static String getJobGroup() {
    return "PIPELINE_GROUP";
  }

  public static String getJobName(TaskContext context) {
    return "PIPELINE_JOB_" + context.getTaskId();
  }
}
