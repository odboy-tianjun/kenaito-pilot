# 简化版任务编排系统 - 编码指南

## 🚀 核心概念

这是一个**轻量级同步任务编排器**，在内存中顺序执行操作链，支持同步和简单异步场景。

### 设计目标

- ✅ **零依赖**: 除Lombok和Slf4j外无其他框架依赖
- ✅ **易用性**: 3个核心接口即可上手
- ✅ **灵活性**: 可嵌入任何Java应用
- ✅ **简洁性**: 约300行核心代码

---

## 📐 架构组件

```
┌─────────────────────────────────────┐
│      TaskContext (任务上下文)        │
│  - taskId: String                   │
│  - data: Map<String, Object>        │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│     TaskEngine (执行引擎)            │
│  - 顺序执行Operation列表             │
│  - 自动重试                          │
│  - 异步轮询                          │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  TaskOperation Chain (操作链)        │
│  ┌────┐  ┌────┐  ┌────┐            │
│  │Op1 │→ │Op2 │→ │OpN │            │
│  └────┘  └────┘  └────┘            │
└─────────────────────────────────────┘
```

---

## 🔌 核心接口定义

### 1. TaskOperation 接口

```java
package com.devops.task.core;

/**
 * 简化版任务操作接口
 * 
 * 执行模式：
 * 1. 同步：execute() 返回 "SUCCESS" → 下一个操作
 * 2. 异步：execute() 返回 "RUNNING" → 循环调用describe()直到"SUCCESS"
 */
public interface TaskOperation {
    
    /**
     * 执行操作
     * @param context 任务上下文，通过data传递参数
     * @return 状态字符串
     *   - "SUCCESS": 操作完成
     *   - "RUNNING": 异步操作中，需要轮询
     *   - "FAILED": 操作失败
     */
    String execute(TaskContext context);
    
    /**
     * 查询异步操作状态（可选实现）
     * @param context 任务上下文
     * @return 状态字符串
     *   - "SUCCESS": 异步操作完成
     *   - "RUNNING": 继续等待
     *   - "FAILED": 操作失败
     */
    default String describe(TaskContext context) {
        return "SUCCESS"; // 默认直接成功（同步操作）
    }
    
    /**
     * 操作名称，用于日志
     */
    String name();
    
    /**
     * 重试次数，默认3次
     */
    default int retryTimes() {
        return 3;
    }
    
    /**
     * 轮询间隔（毫秒），默认1秒
     */
    default long pollInterval() {
        return 1000;
    }
}
```

### 2. TaskContext 类

```java
package com.devops.task.core;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化的单层上下文
 */
@Data
public class TaskContext {
    private String taskId;
    private Map<String, Object> data = new HashMap<>();
    
    /**
     * 获取数据（泛型方法）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }
    
    /**
     * 设置数据
     */
    public void put(String key, Object value) {
        data.put(key, value);
    }
}
```

### 3. TaskResult 类

```java
package com.devops.task.core;

import lombok.Data;

/**
 * 任务执行结果
 */
@Data
public class TaskResult {
    private boolean success;
    private String message;
    private String failedOperation;
    
    /**
     * 创建成功结果
     */
    public static TaskResult success(String message) {
        TaskResult result = new TaskResult();
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static TaskResult fail(String operation, String message) {
        TaskResult result = new TaskResult();
        result.setSuccess(false);
        result.setFailedOperation(operation);
        result.setMessage(message);
        return result;
    }
}
```

### 4. TaskStatus 常量接口

```java
package com.devops.task.core;

/**
 * 任务状态常量
 */
public interface TaskStatus {
    String SUCCESS = "SUCCESS";
    String RUNNING = "RUNNING";
    String FAILED = "FAILED";
}
```

---

## ⚙️ 执行引擎实现

```java
package com.devops.task.core;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * 任务执行引擎
 * 
 * 职责：
 * 1. 顺序执行Operation链
 * 2. 处理重试逻辑
 * 3. 处理异步轮询
 */
@Slf4j
public class TaskEngine {
    
    /**
     * 执行任务链
     * @param operations 操作列表
     * @param context 任务上下文
     * @return 执行结果
     */
    public TaskResult execute(List<TaskOperation> operations, TaskContext context) {
        for (TaskOperation operation : operations) {
            log.info("开始执行操作: {}", operation.name());
            
            String status = executeWithRetry(operation, context);
            
            if (TaskStatus.FAILED.equals(status)) {
                log.error("操作执行失败: {}", operation.name());
                return TaskResult.fail(operation.name(), "操作执行失败");
            }
        }
        
        log.info("所有操作执行完成");
        return TaskResult.success("任务执行成功");
    }
    
    /**
     * 带重试的执行
     */
    private String executeWithRetry(TaskOperation operation, TaskContext context) {
        int maxRetries = operation.retryTimes();
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                String status = operation.execute(context);
                
                if (TaskStatus.SUCCESS.equals(status)) {
                    return TaskStatus.SUCCESS;
                }
                
                if (TaskStatus.RUNNING.equals(status)) {
                    return waitForCompletion(operation, context);
                }
                
            } catch (Exception e) {
                log.warn("操作执行异常, 重试次数: {}/{}", i + 1, maxRetries, e);
                if (i == maxRetries - 1) {
                    return TaskStatus.FAILED;
                }
            }
        }
        
        return TaskStatus.FAILED;
    }
    
    /**
     * 等待异步操作完成
     */
    private String waitForCompletion(TaskOperation operation, TaskContext context) {
        while (true) {
            try {
                String status = operation.describe(context);
                
                if (TaskStatus.SUCCESS.equals(status)) {
                    return TaskStatus.SUCCESS;
                }
                
                if (TaskStatus.FAILED.equals(status)) {
                    return TaskStatus.FAILED;
                }
                
                // 等待指定间隔后再次检查
                Thread.sleep(operation.pollInterval());
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return TaskStatus.FAILED;
            } catch (Exception e) {
                log.warn("查询状态异常", e);
                return TaskStatus.FAILED;
            }
        }
    }
}
```

---

## 📝 使用示例

### 示例1：同步操作（发送通知）

```java
package com.devops.task.operations;

import com.devops.task.core.TaskContext;
import com.devops.task.core.TaskOperation;
import com.devops.task.core.TaskStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendNotificationOperation implements TaskOperation {
    
    @Override
    public String execute(TaskContext context) {
        String message = context.get("message");
        String recipient = context.get("recipient");
        
        log.info("发送通知给 {}: {}", recipient, message);
        
        // 模拟发送通知
        sendNotification(recipient, message);
        
        return TaskStatus.SUCCESS;
    }
    
    @Override
    public String name() {
        return "发送通知";
    }
    
    private void sendNotification(String recipient, String message) {
        // 实际的通知发送逻辑
    }
}
```

### 示例2：异步操作（等待服务就绪）

```java
package com.devops.task.operations;

import com.devops.task.core.TaskContext;
import com.devops.task.core.TaskOperation;
import com.devops.task.core.TaskStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaitForServiceReadyOperation implements TaskOperation {
    
    @Override
    public String execute(TaskContext context) {
        String serviceName = context.get("serviceName");
        String namespace = context.get("namespace");
        
        log.info("开始创建服务: {}", serviceName);
        
        // 触发服务创建
        createService(serviceName, namespace);
        
        return TaskStatus.RUNNING;
    }
    
    @Override
    public String describe(TaskContext context) {
        String serviceName = context.get("serviceName");
        String namespace = context.get("namespace");
        
        log.info("检查服务状态: {}", serviceName);
        
        boolean isReady = checkServiceStatus(serviceName, namespace);
        
        if (isReady) {
            log.info("服务已就绪: {}", serviceName);
            return TaskStatus.SUCCESS;
        }
        
        return TaskStatus.RUNNING;
    }
    
    @Override
    public String name() {
        return "等待服务就绪";
    }
    
    @Override
    public long pollInterval() {
        return 2000; // 2秒轮询一次
    }
    
    private void createService(String serviceName, String namespace) {
        // 创建服务的逻辑
    }
    
    private boolean checkServiceStatus(String serviceName, String namespace) {
        // 检查服务状态的逻辑
        return true;
    }
}
```

### 示例3：数据库操作

```java
package com.devops.task.operations;

import com.devops.task.core.TaskContext;
import com.devops.task.core.TaskOperation;
import com.devops.task.core.TaskStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateDatabaseOperation implements TaskOperation {
    
    @Override
    public String execute(TaskContext context) {
        String sql = context.get("sql");
        
        log.info("执行SQL: {}", sql);
        
        try {
            executeSql(sql);
            return TaskStatus.SUCCESS;
        } catch (Exception e) {
            log.error("SQL执行失败", e);
            return TaskStatus.FAILED;
        }
    }
    
    @Override
    public String name() {
        return "更新数据库";
    }
    
    @Override
    public int retryTimes() {
        return 5; // 重试5次
    }
    
    private void executeSql(String sql) {
        // 执行SQL的逻辑
    }
}
```

---

## 🎯 完整使用示例

### 基础用法

```java
package com.devops.task.example;

import com.devops.task.core.TaskContext;
import com.devops.task.core.TaskEngine;
import com.devops.task.core.TaskResult;
import com.devops.task.operations.SendNotificationOperation;
import com.devops.task.operations.WaitForServiceReadyOperation;
import com.devops.task.operations.UpdateDatabaseOperation;

import java.util.Arrays;

public class TaskExample {
    
    public static void main(String[] args) {
        // 1. 创建引擎
        TaskEngine engine = new TaskEngine();
        
        // 2. 准备上下文
        TaskContext context = new TaskContext();
        context.setTaskId("task-001");
        context.put("serviceName", "my-app");
        context.put("namespace", "default");
        context.put("message", "部署完成");
        context.put("recipient", "admin@example.com");
        context.put("sql", "UPDATE apps SET status='deployed' WHERE name='my-app'");
        
        // 3. 定义操作链
        var operations = Arrays.asList(
            new WaitForServiceReadyOperation(),
            new UpdateDatabaseOperation(),
            new SendNotificationOperation()
        );
        
        // 4. 执行任务
        TaskResult result = engine.execute(operations, context);
        
        // 5. 处理结果
        if (result.isSuccess()) {
            System.out.println("✅ 任务成功: " + result.getMessage());
        } else {
            System.out.println("❌ 任务失败: " + result.getMessage() + 
                             ", 失败操作: " + result.getFailedOperation());
        }
    }
}
```

### Builder模式用法（推荐）

```java
package com.devops.task.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务构建器，提供流畅的API
 */
public class TaskBuilder {
    
    private List<TaskOperation> operations = new ArrayList<>();
    private TaskContext context = new TaskContext();
    
    public TaskBuilder taskId(String taskId) {
        context.setTaskId(taskId);
        return this;
    }
    
    public TaskBuilder addOperation(TaskOperation operation) {
        operations.add(operation);
        return this;
    }
    
    public TaskBuilder withData(String key, Object value) {
        context.put(key, value);
        return this;
    }
    
    public TaskResult execute() {
        TaskEngine engine = new TaskEngine();
        return engine.execute(operations, context);
    }
}
```

使用Builder：

```java
TaskResult result = new TaskBuilder()
    .taskId("task-001")
    .withData("serviceName", "my-app")
    .withData("namespace", "default")
    .addOperation(new WaitForServiceReadyOperation())
    .addOperation(new UpdateDatabaseOperation())
    .addOperation(new SendNotificationOperation())
    .execute();

if (result.isSuccess()) {
    System.out.println("任务成功!");
}
```

---

## 🔄 执行流程图

```
START
  │
  ▼
┌─────────────────────────┐
│ for each Operation:     │
└─────────┬───────────────┘
          │
          ▼
┌─────────────────────────┐
│ execute(op) with retry  │
│ (最多retryTimes次)      │
└─────────┬───────────────┘
          │
    ┌─────┴─────┐
    │ 返回值？   │
    └─┬─────┬───┘
      │     │
  SUCCESS RUNNING
      │     │
      │     ▼
      │  ┌──────────────────┐
      │  │ while(true):     │
      │  │  describe(op)    │
      │  └────┬─────────────┘
      │       │
      │  ┌────┴────┐
      │  │ 返回值？ │
      │  └─┬──┬───┘
      │    │  │
      │ SUCCESS│
      │    │  │
      │    │  └──→ 继续循环
      │    │
      ▼    ▼
┌─────────────────────────┐
│ 下一个Operation          │
│ 或返回TaskResult         │
└─────────────────────────┘
```

---

## ✅ 编码要求

1. **纯Java实现**，无框架依赖（除Lombok和Slf4j）
2. **同步操作**: 只需实现execute()返回"SUCCESS"
3. **异步操作**: execute()返回"RUNNING"，describe()轮询检查
4. **异常处理**: execute/describe抛出的异常会被捕获并计入重试
5. **线程安全**: TaskEngine是单例，但每次execute创建新Context
6. **无状态**: Operation应该是无状态的，所有状态保存在Context中
7. **幂等性**: describe可能被多次调用，确保幂等

---

## 🧪 测试要点

### 单元测试示例

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskEngineTest {
    
    @Test
    void testSyncOperations() {
        TaskEngine engine = new TaskEngine();
        TaskContext context = new TaskContext();
        
        var operations = Arrays.asList(
            new SyncOperation1(),
            new SyncOperation2()
        );
        
        TaskResult result = engine.execute(operations, context);
        
        assertTrue(result.isSuccess());
    }
    
    @Test
    void testAsyncOperations() {
        TaskEngine engine = new TaskEngine();
        TaskContext context = new TaskContext();
        context.put("serviceName", "test-service");
        
        var operations = Arrays.asList(
            new AsyncOperation()
        );
        
        TaskResult result = engine.execute(operations, context);
        
        assertTrue(result.isSuccess());
    }
    
    @Test
    void testRetryMechanism() {
        TaskEngine engine = new TaskEngine();
        TaskContext context = new TaskContext();
        
        var operations = Arrays.asList(
            new RetryOperation() // 前2次失败，第3次成功
        );
        
        TaskResult result = engine.execute(operations, context);
        
        assertTrue(result.isSuccess());
    }
}
```

---

## 📊 与原版对比

| 维度 | 原版 | 简化版 |
|------|------|--------|
| **持久化** | 每次操作后保存到MySQL | 无持久化，纯内存 |
| **调度** | Quartz定时任务，支持cron | 同步执行，立即完成 |
| **上下文** | 双层（change + operation） | 单层（task data） |
| **状态管理** | 数据库驱动，支持断点续传 | 内存驱动，重启丢失 |
| **依赖** | Spring + MyBatis + Quartz | 仅Lombok + Slf4j |
| **适用场景** | 长时间运行的变更流程（小时级） | 快速任务编排（秒级） |
| **复杂度** | 高（10+个核心类） | 低（3个核心接口） |
| **代码量** | 数千行 | 约300行核心代码 |

---

## 💡 最佳实践

### 1. 操作粒度

- ✅ **好**: 每个Operation只做一件事
- ❌ **坏**: 一个Operation包含多个业务步骤

### 2. 错误处理

```java
@Override
public String execute(TaskContext context) {
    try {
        // 业务逻辑
        doSomething();
        return TaskStatus.SUCCESS;
    } catch (BusinessException e) {
        log.error("业务异常", e);
        return TaskStatus.FAILED; // 明确返回失败
    } catch (Exception e) {
        log.error("系统异常", e);
        throw e; // 抛出异常触发重试
    }
}
```

### 3. 日志记录

```java
@Override
public String execute(TaskContext context) {
    String param = context.get("param");
    log.info("开始执行操作, param={}", param);
    
    // 业务逻辑
    
    log.info("操作执行完成");
    return TaskStatus.SUCCESS;
}
```

### 4. 超时控制

对于可能长时间运行的异步操作，建议在describe中添加超时检查：

```java
private static final long TIMEOUT = 5 * 60 * 1000; // 5分钟

@Override
public String describe(TaskContext context) {
    Long startTime = context.get("startTime");
    if (startTime != null && System.currentTimeMillis() - startTime > TIMEOUT) {
        log.error("操作超时");
        return TaskStatus.FAILED;
    }
    
    // 正常检查逻辑
    return checkStatus();
}
```

---

## 🛠️ 技术栈

- **语言**: Java 8+
- **工具库**: Lombok, Slf4j
- **测试**: JUnit 5
- **构建**: Maven / Gradle

### Maven依赖

```xml
<dependencies>
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Slf4j -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.9</version>
    </dependency>
    
    <!-- 测试 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 📦 项目结构建议

```
task-system/
├── src/main/java/com/devops/task/
│   ├── core/
│   │   ├── TaskOperation.java
│   │   ├── TaskContext.java
│   │   ├── TaskEngine.java
│   │   ├── TaskResult.java
│   │   ├── TaskStatus.java
│   │   └── TaskBuilder.java
│   ├── operations/
│   │   ├── SendNotificationOperation.java
│   │   ├── WaitForServiceReadyOperation.java
│   │   └── UpdateDatabaseOperation.java
│   └── example/
│       └── TaskExample.java
├── src/test/java/com/devops/task/
│   └── TaskEngineTest.java
├── pom.xml
└── README.md
```

---

## 🎓 总结

这个简化版任务编排系统保留了原版的**核心特性**：
- ✅ 操作链编排
- ✅ 同步/异步支持
- ✅ 重试机制
- ✅ 轮询检查

同时去除了**复杂依赖**：
- ❌ 数据库持久化
- ❌ Quartz调度
- ❌ Spring框架
- ❌ 复杂的上下文管理

非常适合：
- 微服务内部的任务编排
- 快速原型开发
- 学习和理解任务编排原理
- 不需要持久化的短时任务
