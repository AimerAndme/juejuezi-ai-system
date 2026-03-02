# Graph调用日志级别方案

## 1. 现状分析

### 1.1 当前日志实现

- **日志框架**：使用 SLF4J + Logback（通过 @Slf4j 注解）
- **日志使用**：节点级别有基本的日志记录，但缺乏 Graph 级别统一管理
- **日志级别**：使用了 info、warn、error 级别，但缺乏规范
- **监控集成**：使用了 @ExecutionTimeMonitor 注解监控执行时间
- **存在问题**：
  - 缺乏统一的日志格式和标准
  - 没有 Graph 级别（流程级）的日志记录
  - 日志级别使用不够规范
  - 缺乏结构化日志，难以进行日志分析
  - 没有与监控系统深度集成

## 2. 日志级别方案设计

### 2.1 日志级别定义

| 级别 | 描述 | 使用场景 | 示例 |
|------|------|----------|------|
| **TRACE** | 最详细的日志 | 节点内部详细执行步骤、状态变化 | 节点内部循环、状态更新详情 |
| **DEBUG** | 调试信息 | 节点执行参数、中间结果 | 节点输入参数、处理过程 |
| **INFO** | 一般信息 | 节点执行开始/完成、流程状态变化 | 节点启动、流程分支选择 |
| **WARN** | 警告信息 | 非致命错误、需要关注的情况 | 重试、降级处理、性能警告 |
| **ERROR** | 错误信息 | 致命错误、需要立即处理的问题 | 节点执行失败、异常堆栈 |
| **FATAL** | 严重错误 | 系统级错误、可能导致系统崩溃 | 资源耗尽、配置错误 |

### 2.2 统一日志格式

#### 2.2.1 结构化日志格式

```json
{
  "timestamp": "2026-01-19T10:00:00.123Z",
  "level": "INFO",
  "logger": "com.yupi.yuaiagent.graph.PreProcessingGraphFactory",
  "thread": "graph-executor-1",
  "message": "Graph execution completed",
  "graph": {
    "name": "PreProcessingGraph",
    "instanceId": "graph-12345",
    "executionTime": 1500
  },
  "node": {
    "name": "意图识别",
    "executionTime": 800,
    "status": "SUCCESS"
  },
  "state": {
    "size": 5,
    "keys": ["queryInfo", "reWriteQuery", "recognizeResult"]
  },
  "error": null,
  "traceId": "trace-67890"
}
```

#### 2.2.2 日志字段说明

| 字段 | 类型 | 说明 | 必选 |
|------|------|------|------|
| timestamp | String | 日志时间戳（ISO 8601格式） | 是 |
| level | String | 日志级别 | 是 |
| logger | String | 日志记录器名称 | 是 |
| thread | String | 线程名称 | 是 |
| message | String | 日志消息 | 是 |
| graph | Object | Graph相关信息 | 否（仅Graph级别日志） |
| node | Object | 节点相关信息 | 否（仅节点级别日志） |
| state | Object | 状态相关信息 | 否 |
| error | Object | 错误信息 | 否（仅错误日志） |
| traceId | String | 分布式追踪ID | 是 |

## 3. Graph级别日志增强

### 3.1 Graph执行生命周期日志

| 阶段 | 级别 | 消息模板 | 说明 |
|------|------|----------|------|
| **启动** | INFO | "Graph [{}] starting execution. Instance ID: {}, Initial state keys: {} | 记录Graph启动信息 |
| **分支选择** | INFO | "Graph [{}] branch selected: {} based on condition: {} | 记录条件分支选择结果 |
| **完成** | INFO | "Graph [{}] completed successfully in {}ms. Final state keys: {} | 记录Graph成功完成 |
| **异常** | ERROR | "Graph [{}] failed in {}ms. Error: {} | 记录Graph执行失败 |
| **超时** | ERROR | "Graph [{}] timed out after {}ms | 记录Graph执行超时 |

### 3.2 Graph监控日志

| 类型 | 级别 | 消息模板 | 说明 |
|------|------|----------|------|
| **性能** | WARN | "Graph [{}] execution time ({})ms exceeds threshold ({}ms) | 性能警告 |
| **状态大小** | WARN | "Graph [{}] state size ({})KB exceeds threshold ({}KB) | 状态过大警告 |
| **节点错误** | ERROR | "Graph [{}] node [{}] failed: {} | 节点错误 |
| **重试** | WARN | "Graph [{}] retrying node [{}] (attempt {}/{}) | 节点重试 |

## 4. 节点级别日志增强

### 4.1 节点执行生命周期日志

| 阶段 | 级别 | 消息模板 | 说明 |
|------|------|----------|------|
| **执行前** | DEBUG | "Node [{}] starting execution. Input state keys: {} | 节点启动信息 |
| **执行中** | TRACE | "Node [{}] processing. Current state: {} | 节点处理详情 |
| **执行后** | INFO | "Node [{}] completed in {}ms. Output state keys: {} | 节点完成信息 |
| **错误** | ERROR | "Node [{}] failed: {}. Stack trace: {} | 节点错误信息 |
| **重试** | WARN | "Node [{}] retrying (attempt {}/{}) after error: {} | 节点重试信息 |

### 4.2 节点类型特定日志

| 节点类型 | 级别 | 消息模板 | 说明 |
|----------|------|----------|------|
| **意图识别** | INFO | "IntentRecognitionNode: recognized intent {} for query {} | 意图识别结果 |
| **NL2SQL** | INFO | "Nl2SqlNode: generated SQL: {} | SQL生成结果 |
| **DB调用** | INFO | "DBInvocationNode: executed SQL in {}ms, rows affected: {} | 数据库操作结果 |
| **RAG查询** | INFO | "RagQueryNode: retrieved {} documents in {}ms | RAG检索结果 |
| **结果转换** | INFO | "DBResult2NlNode: converted {} rows to natural language | 结果转换结果 |

## 5. 技术实现方案

### 5.1 日志增强器

#### 5.1.1 Graph日志增强器

```java
public class GraphLoggerEnhancer {
    private static final Logger logger = LoggerFactory.getLogger(GraphLoggerEnhancer.class);
    
    public static String generateInstanceId() {
        return "graph-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static void logGraphStart(String graphName, String instanceId, Set<String> initialStateKeys) {
        logger.info("Graph [{}] starting execution. Instance ID: {}, Initial state keys: {}",
                graphName, instanceId, initialStateKeys);
    }
    
    public static void logGraphBranch(String graphName, String branchName, String condition) {
        logger.info("Graph [{}] branch selected: {} based on condition: {}",
                graphName, branchName, condition);
    }
    
    public static void logGraphComplete(String graphName, String instanceId, long durationMs, Set<String> finalStateKeys) {
        logger.info("Graph [{}] completed successfully in {}ms. Final state keys: {}",
                graphName, durationMs, finalStateKeys);
    }
    
    public static void logGraphError(String graphName, String instanceId, long durationMs, Throwable error) {
        logger.error("Graph [{}] failed in {}ms. Error: {}",
                graphName, durationMs, error.getMessage(), error);
    }
    
    public static void logGraphTimeout(String graphName, String instanceId, long durationMs) {
        logger.error("Graph [{}] timed out after {}ms",
                graphName, durationMs);
    }
}
```

#### 5.1.2 节点日志增强器

```java
public class NodeLoggerEnhancer {
    private static final Logger logger = LoggerFactory.getLogger(NodeLoggerEnhancer.class);
    
    public static void logNodeStart(String nodeName, Set<String> inputStateKeys) {
        logger.debug("Node [{}] starting execution. Input state keys: {}",
                nodeName, inputStateKeys);
    }
    
    public static void logNodeProcessing(String nodeName, Map<String, Object> currentState) {
        if (logger.isTraceEnabled()) {
            // 只记录状态键，避免日志过大
            Set<String> stateKeys = currentState.keySet();
            logger.trace("Node [{}] processing. Current state keys: {}",
                    nodeName, stateKeys);
        }
    }
    
    public static void logNodeComplete(String nodeName, long durationMs, Set<String> outputStateKeys) {
        logger.info("Node [{}] completed in {}ms. Output state keys: {}",
                nodeName, durationMs, outputStateKeys);
    }
    
    public static void logNodeError(String nodeName, Throwable error) {
        logger.error("Node [{}] failed: {}. Stack trace: {}",
                nodeName, error.getMessage(), error);
    }
    
    public static void logNodeRetry(String nodeName, int attempt, int maxAttempts, Throwable error) {
        logger.warn("Node [{}] retrying (attempt {}/{}) after error: {}",
                nodeName, attempt, maxAttempts, error.getMessage());
    }
}
```

### 5.2 增强的异步节点

```java
public class EnhancedAsyncNodeAction implements NodeAction {
    private final String nodeName;
    private final NodeAction delegate;
    private final int maxRetries;
    private final long retryDelayMs;
    
    public EnhancedAsyncNodeAction(String nodeName, NodeAction delegate) {
        this.nodeName = nodeName;
        this.delegate = delegate;
        this.maxRetries = 3;
        this.retryDelayMs = 200;
    }
    
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        long startTime = System.currentTimeMillis();
        Set<String> inputStateKeys = getStateKeys(state);
        
        NodeLoggerEnhancer.logNodeStart(nodeName, inputStateKeys);
        
        int attempt = 0;
        while (attempt <= maxRetries) {
            try {
                if (attempt > 0) {
                    NodeLoggerEnhancer.logNodeRetry(nodeName, attempt, maxRetries, null);
                }
                
                Map<String, Object> result = delegate.apply(state);
                
                long duration = System.currentTimeMillis() - startTime;
                Set<String> outputStateKeys = result != null ? result.keySet() : Collections.emptySet();
                NodeLoggerEnhancer.logNodeComplete(nodeName, duration, outputStateKeys);
                
                return result;
            } catch (Exception e) {
                attempt++;
                if (attempt > maxRetries) {
                    NodeLoggerEnhancer.logNodeError(nodeName, e);
                    throw e;
                }
                
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    NodeLoggerEnhancer.logNodeError(nodeName, ie);
                    throw new RuntimeException(ie);
                }
            }
        }
        
        throw new RuntimeException("Node execution failed after max retries");
    }
    
    private Set<String> getStateKeys(OverAllState state) {
        // 提取状态键的逻辑
        return Collections.emptySet();
    }
}
```

### 5.3 增强的Graph工厂

```java
@Component
public class EnhancedPreProcessingGraphFactory {
    private final ChatClient chatClient;
    private final NodeAction normalChatNode;
    private final NodeAction ragQueryNode;
    private final NodeAction nl2SqlNode;
    private final NodeAction dBInvocationNode;
    private final NodeAction dBResult2NlNode;
    
    // 构造函数省略
    
    public CompiledGraph getDBInvocationChatInstance() throws GraphStateException {
        // 现有代码
        StateGraph stateGraph = new StateGraph("PreProcessingGraph", keyStrategyFactory);
        
        // 使用增强的节点
        stateGraph.addNode("查询重写", AsyncNodeAction.node_async(
                new EnhancedAsyncNodeAction("查询重写", new QueryRewritingNode(chatClient))));
        
        stateGraph.addNode("意图识别", AsyncNodeAction.node_async(
                new EnhancedAsyncNodeAction("意图识别", new IntentRecognitionNode(chatClient))));
        
        // 其他节点类似处理
        
        return stateGraph.compile();
    }
}
```

## 6. 监控系统集成

### 6.1 与Micrometer集成

```java
public class GraphMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    public GraphMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordGraphExecution(String graphName, long durationMs, boolean success) {
        meterRegistry.timer("graph.execution.time", 
                "graph_name", graphName, 
                "status", success ? "success" : "error")
            .record(durationMs, TimeUnit.MILLISECONDS);
        
        meterRegistry.counter("graph.execution.count", 
                "graph_name", graphName, 
                "status", success ? "success" : "error")
            .increment();
    }
    
    public void recordNodeExecution(String nodeName, long durationMs, boolean success) {
        meterRegistry.timer("graph.node.execution.time", 
                "node_name", nodeName, 
                "status", success ? "success" : "error")
            .record(durationMs, TimeUnit.MILLISECONDS);
        
        meterRegistry.counter("graph.node.execution.count", 
                "node_name", nodeName, 
                "status", success ? "success" : "error")
            .increment();
    }
}
```

### 6.2 与OpenTelemetry集成

```java
public class GraphTracingEnhancer {
    private final Tracer tracer;
    
    public GraphTracingEnhancer(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public Span startGraphSpan(String graphName, String instanceId) {
        return tracer.spanBuilder("graph.execution")
            .setAttribute("graph.name", graphName)
            .setAttribute("graph.instance_id", instanceId)
            .startSpan();
    }
    
    public Span startNodeSpan(Span parentSpan, String nodeName) {
        return tracer.spanBuilder("graph.node.execution")
            .setParent(Context.current().with(parentSpan))
            .setAttribute("node.name", nodeName)
            .startSpan();
    }
    
    public void endSpan(Span span, boolean success, Throwable error) {
        if (!success && error != null) {
            span.recordException(error);
            span.setAttribute("execution.status", "error");
        } else {
            span.setAttribute("execution.status", "success");
        }
        span.end();
    }
}
```

## 7. 配置与部署

### 7.1 日志配置

**logback-spring.xml 配置示例：**

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/graph-execution.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/graph-execution.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Graph相关日志 -->
    <logger name="com.yupi.yuaiagent.graph" level="info" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </logger>
    
    <!-- 节点相关日志 -->
    <logger name="com.yupi.yuaiagent.node" level="info" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </logger>
    
    <!-- 生产环境可调整为warn -->
    <root level="warn">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### 7.2 环境变量配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| GRAPH_LOG_LEVEL | info | Graph相关日志级别 |
| NODE_LOG_LEVEL | info | 节点相关日志级别 |
| GRAPH_EXECUTION_TIMEOUT | 30000 | Graph执行超时时间（ms） |
| NODE_EXECUTION_THRESHOLD | 5000 | 节点执行警告阈值（ms） |
| ENABLE_TRACING | true | 是否启用分布式追踪 |
| ENABLE_METRICS | true | 是否启用指标收集 |

## 8. 性能考虑

### 8.1 日志性能优化

1. **异步日志**：使用异步Appender减少日志对主流程的影响
2. **日志级别控制**：生产环境使用较高日志级别（info/warn）
3. **日志过滤**：避免记录敏感信息和过大的状态数据
4. **批量处理**：使用批量Appender减少I/O操作
5. **缓存**：缓存频繁使用的日志模板

### 8.2 监控性能优化

1. **采样率**：生产环境使用适当的采样率（如10%）
2. **指标聚合**：使用指标聚合减少数据传输
3. **本地缓存**：缓存指标数据，减少网络传输
4. **异步采集**：使用异步方式采集和发送指标

## 9. 实施计划

### 9.1 阶段一：基础增强（1周）

1. **日志格式统一**：
   - 定义统一的日志格式和模板
   - 更新现有节点的日志实现

2. **Graph级别日志**：
   - 实现GraphLoggerEnhancer
   - 在Graph工厂中集成日志增强

### 9.2 阶段二：节点增强（1周）

1. **节点日志增强**：
   - 实现NodeLoggerEnhancer
   - 开发EnhancedAsyncNodeAction
   - 更新所有节点使用增强的实现

2. **错误处理优化**：
   - 实现统一的错误处理和重试机制
   - 完善错误日志记录

### 9.3 阶段三：监控集成（1周）

1. **指标集成**：
   - 集成Micrometer
   - 实现GraphMetricsCollector
   - 配置指标采集和暴露

2. **追踪集成**：
   - 集成OpenTelemetry
   - 实现GraphTracingEnhancer
   - 配置与Jaeger/Zipkin的集成

### 9.4 阶段四：部署与调优（1周）

1. **配置优化**：
   - 根据环境调整日志级别和配置
   - 优化监控采样率和指标收集

2. **性能测试**：
   - 进行负载测试，验证日志和监控对性能的影响
   - 调整配置参数，达到最佳平衡

3. **文档完善**：
   - 编写日志使用指南
   - 更新监控配置文档

## 10. 预期效果

### 10.1 可观察性提升

1. **流程可视化**：通过Graph级别日志，清晰了解流程执行路径
2. **问题定位**：快速定位节点执行失败的原因
3. **性能分析**：通过日志和指标，识别性能瓶颈
4. **趋势分析**：通过结构化日志，进行长期趋势分析

### 10.2 运维效率提升

1. **告警准确**：基于日志和指标的准确告警
2. **故障快速响应**：快速定位和解决问题
3. **容量规划**：基于历史数据进行容量规划
4. **系统优化**：基于日志分析结果优化系统

### 10.3 开发效率提升

1. **调试便捷**：详细的日志便于开发调试
2. **问题重现**：结构化日志便于问题重现
3. **代码质量**：统一的日志规范提升代码质量
4. **知识传递**：清晰的日志便于团队知识传递

## 11. 总结

本方案通过统一的日志级别定义、标准化的日志格式、Graph和节点级别的日志增强，以及与监控系统的深度集成，构建了一个完整的Graph调用日志级别方案。该方案不仅满足了当前系统的需求，也为未来的系统扩展和性能优化提供了基础。

实施本方案后，系统将具备更好的可观察性、更高的运维效率和开发效率，同时保持良好的性能表现。