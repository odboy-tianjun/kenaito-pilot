package cn.odboy.pipeline.v4.util;

import cn.odboy.pipeline.v4.core.TaskContext;

public class PipelineNameUtil {

  public static String getJobGroup() {
    return "PIPELINE_GROUP";
  }

  public static String getJobName(TaskContext context) {
    return "PIPELINE_JOB_" + context.getTaskId();
  }
}
