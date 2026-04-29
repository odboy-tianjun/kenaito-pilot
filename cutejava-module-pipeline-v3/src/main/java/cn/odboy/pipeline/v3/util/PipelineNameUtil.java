package cn.odboy.pipeline.v3.util;

import cn.odboy.pipeline.v3.core.TaskContext;

public class PipelineNameUtil {

  public static String getJobGroup() {
    return "PIPELINE_GROUP";
  }

  public static String getJobName(TaskContext context) {
    return "PIPELINE_JOB_" + context.getTaskId();
  }
}
