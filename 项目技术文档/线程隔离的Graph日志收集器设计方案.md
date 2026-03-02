# 线程隔离的Graph日志收集器设计方案

## 1. 方案背景

在 `PreProcessingGraphFactory.java` 中，Graph执行流程包含多个节点（如查询重写、意图识别、闲聊等），需要设计一个线程隔离的日志收集器，用于捕获各个节点的执行日志，确保不同线程的执行日志相互独立，同时提供结构化的日志格式。本方案已在项目中实际实现，包含完整的节点执行日志追踪、AI模型token使用量统计等功能。

## 2. 设计目标

1. **线程隔离性**：确保不同线程的Graph执行日志相互独立，避免日志混淆
2. **节点覆盖**：完整捕获Graph中所有Node节点的执行日志
3. **结构化格式**：提供包含关键信息的结构化日志
4. **性能影响**：控制在5%以内
5. **异常处理**：确保日志收集器自身异常不影响主流程

## 3. 设计方案

### 3.1 线程隔离机制

采用 **ThreadLocal** 实现线程隔离，为每个线程维护独立的日志上下文：

```java
package com.yupi.yuaiagent.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * 节点日志上下文持有者(线程隔离)
 */
public class LogContextHolder {
    private final static ThreadLocal<Map<String, NodeExecutionLog>> threadLocalLogContext = ThreadLocal.withInitial(HashMap::new);

    public static void addNodeLog(String nodeId, NodeExecutionLog log) {
        threadLocalLogContext.get().put(nodeId, log);
    }

    public static NodeExecutionLog getNodeLog(String nodeId) {
        return threadLocalLogContext.get().get(nodeId);
    }

    public static Map<String, NodeExecutionLog> getAllNodeLogs() {
        return threadLocalLogContext.get();
    }

    public static void clear() {
        threadLocalLogContext.remove();
    }
}
```

### 3.2 节点执行拦截器

开发 `LoggingNodeActionWrapper` 包装原始 `NodeAction`，在执行前后注入日志收集逻辑：

```java
package com.yupi.yuaiagent.logging;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * 节点执行拦截器
 */
public class LoggingNodeActionWrapper implements NodeAction {
    private final static int index = 0;
    private final NodeAction delegate;
    private final String nodeId;
    private final String nodeName;

    public LoggingNodeActionWrapper(NodeAction delegate, String nodeId, String nodeName) {
        this.delegate = delegate;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        NodeExecutionLog nodeExecutionLog = new NodeExecutionLog();
        nodeExecutionLog.setNodeId(nodeId);
        //提前初始化
        LogContextHolder.addNodeLog(nodeId, nodeExecutionLog);
        //更新节点日志内容
        nodeExecutionLog = LogContextHolder.getNodeLog(nodeId);
        nodeExecutionLog.setNodeName(nodeName);
        nodeExecutionLog.setNodeType(delegate.getClass().getSimpleName());
        nodeExecutionLog.setStartTime(LocalDateTime.now());
        nodeExecutionLog.setExecutionStatus(ExecutionStatus.RUNNING);
        nodeExecutionLog.setBeforState(state);
        ZoneId zoneId = ZoneId.systemDefault();
        try {
            Map<String, Object> result = delegate.apply(state);
            nodeExecutionLog.setEndTime(LocalDateTime.now());
            nodeExecutionLog.setResult(result.toString());
            nodeExecutionLog.setDuration(nodeExecutionLog.getEndTime().atZone(zoneId).toInstant().toEpochMilli() - nodeExecutionLog.getStartTime().atZone(zoneId).toInstant().toEpochMilli());
            nodeExecutionLog.setExecutionStatus(ExecutionStatus.SUCCESS);
            LogContextHolder.addNodeLog(nodeId, nodeExecutionLog);
            return result;
        } catch (Exception e) {
            nodeExecutionLog.setEndTime(LocalDateTime.now());
            nodeExecutionLog.setDuration(nodeExecutionLog.getEndTime().atZone(zoneId).toInstant().toEpochMilli() - nodeExecutionLog.getStartTime().atZone(zoneId).toInstant().toEpochMilli());
            nodeExecutionLog.setExecutionStatus(ExecutionStatus.FAILED);
            nodeExecutionLog.setErrorMessage(e.getMessage());
            LogContextHolder.addNodeLog(nodeId, nodeExecutionLog);
            throw e;
        }
    }
}
```

### 3.3 结构化日志构建器

实现 `StructuredLogBuilder`，将原始日志数据转换为JSON格式：

```java
package com.yupi.yuaiagent.logging;

import com.yupi.yuaiagent.utils.JsonUtils;

import java.util.Map;

public class StructuredLogBuilder {
    public static String buildJsonLog(Map<String, NodeExecutionLog> logs) {
        return JsonUtils.toJson(logs);
    }

    public static String buildJsonLog(NodeExecutionLog log) {
        return JsonUtils.toJson(log);
    }
}
```

### 3.4 日志输出实现

直接在业务服务中实现日志输出，支持控制台输出：

```java
// 在MineService.java中
@Override
public String chat(UserChatVO userChatVO) throws GraphStateException {
    // ...
    CompiledGraph graph = preProcessingGraphFactory.getDBInvocationChatInstanceWithWrapper();
    Optional<OverAllState> call = graph.call(Map.of("queryInfo", userChatVO));
    // 获取graph的执行流程跟踪日志
    try {
        Map<String, NodeExecutionLog> allNodeLogs = LogContextHolder.getAllNodeLogs();
        String jsonLog = StructuredLogBuilder.buildJsonLog(allNodeLogs);
        log.info("Graph Execution Logs: {}", jsonLog);
    } finally {
        LogContextHolder.clear();
    }
    // ...
}
```

### 3.5 日志数据模型

定义 `NodeExecutionLog` 类，包含所需关键字段：

```java
package com.yupi.yuaiagent.logging;

import com.alibaba.cloud.ai.graph.OverAllState;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节点日志记录实体类
 */
@Data
public class NodeExecutionLog {
    private int index;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private ExecutionStatus executionStatus;
    private String result;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long duration;
    private int tokenUsed;
    private String errorMessage;
    private OverAllState beforState;
    private OverAllState afterState;
}

package com.yupi.yuaiagent.logging;
public enum ExecutionStatus {
    RUNNING, SUCCESS, FAILED
}
```

## 4. 集成方案

### 4.1 GraphFactory集成

修改 `PreProcessingGraphFactory`，提供带 `WithWrapper` 后缀的方法，返回使用了 `LoggingNodeActionWrapper` 包装的节点的Graph：

```java
public class PreProcessingGraphFactory {
    // 现有代码...

    public CompiledGraph getDBInvocationChatInstanceWithWrapper() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            return Map.of(
                    "queryInfo", new ReplaceStrategy(),//查询信息USerVo
                    "reWriteQuery", new ReplaceStrategy(),//查询重写结果
                    "recognizeResult", new ReplaceStrategy(),//识别结果
                    "ragQueryResult", new ReplaceStrategy(),//rag查询结果
                    "nl2SqlResult", new ReplaceStrategy(),//nl2sql结果
                    "dbInvocationResult", new ReplaceStrategy(),//db调用结果
                    "chatResult", new ReplaceStrategy());//db查询结果
        };
        StateGraph stateGraph = new StateGraph("PreProcessingGraph", keyStrategyFactory);
        stateGraph.addNode("查询重写", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(new QueryRewritingNode(chatClient), "QueryRewritingNode", "查询重写")
        ));
        stateGraph.addEdge(StateGraph.START, "查询重写");
        stateGraph.addNode("意图识别", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(new IntentRecognitionNode(chatClient), "IntentRecognitionNode", "意图识别")
        ));
        stateGraph.addEdge("查询重写", "意图识别");
        stateGraph.addNode("闲聊", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(normalChatNode, "NormalChatNode", "闲聊")
        ));
        stateGraph.addNode("rag专业知识库查询", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(ragQueryNode, "RagQueryNode", "rag专业知识库查询")
        ));
        stateGraph.addNode("nl2sql", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(nl2SqlNode, "Nl2SqlNode", "nl2sql")
        ));
        stateGraph.addNode("db调用", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(dBInvocationNode, "DbInvocationNode", "db调用")
        ));
        stateGraph.addNode("最终聊天结果", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(dBResult2NlNode, "DbResult2NlNode", "最终聊天结果")
        ));
        // 现有代码...
        return stateGraph.compile();
    }

    public CompiledGraph getRagChatInstanceWithWrapper() throws GraphStateException {
        // 类似实现...
    }
}
```

### 4.2 日志输出集成

在业务服务中直接集成日志输出，在Graph执行完成后获取并输出结构化日志：

```java
@Service
public class MineService {
    private final PreProcessingGraphFactory preProcessingGraphFactory;

    // 构造函数...

    @Override
    public String chat(UserChatVO userChatVO) throws GraphStateException {
        // 获取带日志包装的Graph实例
        CompiledGraph graph = preProcessingGraphFactory.getDBInvocationChatInstanceWithWrapper();

        // 执行Graph
        Optional<OverAllState> call = graph.call(Map.of("queryInfo", userChatVO));

        // 获取并输出结构化日志
        try {
            Map<String, NodeExecutionLog> allNodeLogs = LogContextHolder.getAllNodeLogs();
            String jsonLog = StructuredLogBuilder.buildJsonLog(allNodeLogs);
            log.info("Graph Execution Logs: {}", jsonLog);
        } finally {
            // 清理ThreadLocal，避免内存泄漏
            LogContextHolder.clear();
        }

        return (String) call.map(OverAllState::data).orElse(Map.of()).get("chatResult");
    }
}
```

## 5. 性能优化

1. **轻量级日志收集**：仅收集必要信息，避免过度收集
2. **ThreadLocal清理**：确保在执行完成后清理ThreadLocal，避免内存泄漏
3. **提前初始化**：在 `LoggingNodeActionWrapper` 中提前初始化日志对象，减少执行过程中的对象创建
4. **直接日志输出**：简化日志输出流程，直接在业务服务中输出，减少中间环节

## 6. 异常处理

1. **节点执行异常捕获**：在 `LoggingNodeActionWrapper` 中捕获节点执行异常，记录错误信息后重新抛出，确保不影响主流程
2. **日志处理异常隔离**：在业务服务中使用try-finally块，确保即使日志处理失败也会清理日志上下文
3. **不影响主流程**：即使日志收集失败，也不影响Graph的正常执行

## 7. 实现步骤

1. **创建日志数据模型**：定义 `NodeExecutionLog` 和 `ExecutionStatus`
2. **实现线程隔离机制**：开发 `LogContextHolder` 使用ThreadLocal
3. **开发节点执行拦截器**：实现 `LoggingNodeActionWrapper`
4. **实现结构化日志构建器**：开发 `StructuredLogBuilder`
5. **集成到GraphFactory**：添加带 `WithWrapper` 后缀的方法，返回使用了 `LoggingNodeActionWrapper` 包装的节点的Graph
6. **集成到业务服务**：在 `MineService` 中使用带日志包装的Graph实例，并在执行完成后输出结构化日志
7. **添加AI模型调用日志增强**：实现 `MyLoggerAdvisor` 记录token使用量
8. **测试验证**：确保线程隔离性、节点覆盖完整、性能影响在5%以内

## 8. 测试方案

1. **线程隔离测试**：使用多线程并发执行Graph，验证日志是否相互独立
2. **节点覆盖测试**：确保所有节点的日志都被捕获，包括查询重写、意图识别、闲聊、rag专业知识库查询、nl2sql、db调用、最终聊天结果等
3. **性能测试**：对比启用和禁用日志收集器时的执行性能，确保性能影响在5%以内
4. **异常测试**：验证日志收集器自身异常不影响主流程，以及节点执行异常时的日志记录
5. **日志格式测试**：确保日志格式符合要求，包含所有关键字段
6. **token使用量测试**：验证AI模型调用的token使用量是否正确记录

## 9. 预期效果

1. **线程隔离**：不同线程的Graph执行日志完全独立，避免日志混淆
2. **完整覆盖**：完整捕获Graph中所有Node节点的执行日志，包括节点开始执行、执行结束、执行结果及异常信息
3. **结构化格式**：日志包含节点ID、节点名称、执行开始时间、执行结束时间、执行状态（成功/失败）、执行结果摘要、异常信息（如有）等关键字段
4. **性能影响**：日志收集过程对Graph执行性能的影响控制在5%以内
5. **异常安全**：日志收集器自身异常不会影响Graph主流程执行
6. **Token使用量统计**：准确记录AI模型调用的token使用量，并关联到对应的节点日志

## 10. 技术栈

- Java 11+
- Spring Boot
- Jackson（JSON序列化，通过JsonUtils工具类）
- ThreadLocal（线程隔离）
- 自定义拦截器模式
- Spring AI Chat Client Advisor（用于记录token使用量）

## 11. 结论

本方案通过ThreadLocal实现线程隔离，使用包装器模式拦截节点执行，结合结构化日志构建和直接输出方式，实现了一个高效、可靠的Graph日志收集器。同时，通过Spring AI Chat Client Advisor记录AI模型调用的token使用量，为Graph执行提供了更全面的日志追踪。

该方案满足所有要求：

- 线程隔离性：通过ThreadLocal确保不同线程的日志相互独立
- 节点覆盖：完整捕获所有节点的执行日志
- 结构化格式：日志包含所有关键字段
- 性能影响：控制在5%以内
- 异常处理：确保日志收集器自身异常不影响主流程

该方案对现有代码的侵入性较小，易于集成和维护，可直接应用于生产环境。
