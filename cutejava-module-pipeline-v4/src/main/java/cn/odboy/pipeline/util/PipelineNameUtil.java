package cn.odboy.pipeline.util;

import cn.odboy.pipeline.core.TaskContext;

public class PipelineNameUtil {

  public static String getJobGroup() {
    return "PIPELINE_GROUP";
  }

  public static String getJobName(TaskContext context) {
    return "PIPELINE_JOB_" + context.getTaskId();
  }
}
