# juejuezi-AI-Agent

一个基于 Spring Boot 和 AI 大模型的智能体平台，提供强大的自主规划和工具调用能力。

## 项目概述

juejuezi-AI-Agent 是一个集成了多种 AI 模型和工具的智能体平台，旨在帮助用户解决复杂的任务。该平台采用 ReAct 模式实现智能体的思考和行动能力，支持多种工具调用，包括文件操作、资源下载、终端命令执行、PDF 生成等。

主要功能：

- 智能体自主规划与执行
- 多种 AI 模型支持（阿里云通义千问、Ollama 本地模型等）
- 丰富的工具集支持
- 对话记忆管理
- 向量数据库集成（RAG 知识库）
- 流式响应支持（SSE）

## 技术栈

| 分类     | 技术           | 版本        | 用途                          |
| -------- | -------------- | ----------- | ----------------------------- |
| 编程语言 | Java           | 17          | 后端开发                      |
| 框架     | Spring Boot    | 3.4.4       | 应用框架                      |
| AI 框架  | Spring AI      | 1.0.2       | AI 模型集成                   |
| AI 框架  | LangChain4j    | 1.0.0-beta2 | AI 智能体开发                 |
| 大模型   | 阿里云通义千问 | -           | 提供 AI 能力                  |
| 本地模型 | Ollama         | -           | 本地模型部署                  |
| 数据库   | PostgreSQL     | 14+         | 数据存储（PGvector 向量扩展） |
| 缓存     | Redis          | 6.x/7.x     | 会话记忆持久化                |
| 搜索引擎 | Elasticsearch  | 8.x         | 文档检索                      |
| 消息队列 | RabbitMQ       | 3.x         | 异步任务处理                  |
| 开发工具 | Maven          | 3.6+        | 项目构建                      |
| API 文档 | Knife4j        | 4.4.0       | RESTful API 文档              |
| 容器化   | Docker         | -           | 应用部署                      |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL 14+（需安装 PGvector 扩展）
- Redis 6.x/7.x（可选，用于会话记忆）
- Elasticsearch 8.x（可选，用于文档检索）
- RabbitMQ 3.x（可选，用于异步任务）

### 安装步骤

1. **克隆项目**

```bash
git clone <项目地址>
cd yu-ai-agent-master
```

2. **配置数据库**

创建 PostgreSQL 数据库并启用 PGvector 扩展：

```sql
CREATE DATABASE sdagent;
\c sdagent;
CREATE EXTENSION IF NOT EXISTS vector;
```

3. **配置应用**

修改 `src/main/resources/application.yml` 文件：

- 更新数据库连接信息
- 配置 AI 模型 API 密钥（阿里云通义千问）
- 根据需要配置 Redis、Elasticsearch、RabbitMQ 等

4. **构建项目**

```bash
mvn clean install
```

5. **运行项目**

```bash
mvn spring-boot:run
```

或使用打包后的 JAR 文件：

```bash
java -jar target/lv-ai-agent-0.0.1-SNAPSHOT.jar
```

6. **访问 API 文档**

项目启动后，访问以下地址查看 API 文档：

```
http://localhost:8123/api/doc.html
```

## 项目结构

```
src/main/java/com/yupi/yuaiagent/
├── advisor/          # AI对话顾问
│   ├── MessageMemoryAdvisor.java      # 消息记忆顾问
│   ├── MyLoggerAdvisor.java           # 日志顾问
│   ├── ReReadingAdvisor.java          # 重读顾问
│   └── RetrievalRerankAdvisor.java    # 检索重排顾问
├── agent/            # 智能体实现
│   ├── model/                         # 智能体模型
│   ├── BaseAgent.java                 # 基础智能体
│   ├── LongruanManus.java             # 龙软智能体
│   ├── ReActAgent.java                # ReAct智能体
│   ├── ToolCallAgent.java             # 工具调用智能体
│   └── YuManus.java                   # 鱼皮超级智能体
├── app/              # 应用实现
│   ├── LoveApp.java                   # 爱情应用
│   ├── MineChatApp.java               # 煤矿聊天应用
│   └── RedisChatApp.java              # Redis聊天应用
├── chatmemory/       # 对话记忆
│   ├── FileBasedChatMemory.java       # 文件对话记忆
│   ├── RedisChatMemory.java           # Redis对话记忆
│   └── RedissonChatMemory.java        # Redisson对话记忆
├── client/           # 客户端
│   └── EmbeddingClient.java           # 嵌入客户端
├── config/           # 配置类
│   ├── ChatClientConfig.java          # 聊天客户端配置
│   ├── CorsConfig.java                # 跨域配置
│   ├── EsConfig.java                  # Elasticsearch配置
│   ├── EsIndexInitializer.java        # Elasticsearch索引初始化
│   ├── KryoSerializer.java            # Kryo序列化器
│   ├── MyBatisConfig.java             # MyBatis配置
│   ├── RabbitMQConfig.java            # RabbitMQ配置
│   ├── RedisConfig.java               # Redis配置
│   ├── RedissonChatConfig.java        # Redisson聊天配置
│   └── RedissonConfig.java            # Redisson配置
├── controller/       # 控制器
│   ├── AiController.java              # AI控制器
│   ├── AuthController.java            # 认证控制器
│   ├── ConversationController.java    # 对话控制器
│   ├── DocumentVectorController.java  # 文档向量控制器
│   ├── FileManageController.java      # 文件管理控制器
│   ├── FileParseController.java       # 文件解析控制器
│   ├── FileUploadController.java      # 文件上传控制器
│   └── HealthController.java          # 健康检查控制器
├── demo/             # 演示代码
│   ├── invoke/                        # 调用示例
│   └── rag/                           # RAG示例
├── domin/            # 领域模型
│   ├── constant/                      # 常量
│   ├── context/                       # 上下文
│   ├── dto/                           # 数据传输对象
│   ├── entity/                        # 实体类
│   ├── enums/                         # 枚举类
│   └── vo/                            # 视图对象
├── exception/        # 异常处理
│   ├── BusinessException.java         # 业务异常
│   ├── ErrorCode.java                 # 错误码
│   └── GlobalExceptionHandler.java    # 全局异常处理器
├── graph/            # 图结构
│   └── PreProcessingGraphFactory.java # 预处理图工厂
├── handler/          # 处理器
│   ├── JsonbTypeHandler.java          # JSONB类型处理器
│   ├── PgEnumTypeHandler.java         # PG枚举类型处理器
│   └── RagRequestContextFilter.java   # RAG请求上下文过滤器
├── mapper/           # MyBatis映射器
├── node/             # 节点
│   ├── DBInvocationNode.java          # 数据库调用节点
│   ├── DBResult2NlNode.java           # 数据库结果转自然语言节点
│   ├── IntentRecognitionNode.java     # 意图识别节点
│   ├── Nl2SqlNode.java                # 自然语言转SQL节点
│   ├── NormalChatNode.java            # 普通聊天节点
│   ├── QueryRewritingNode.java        # 查询重写节点
│   └── RagQueryNode.java              # RAG查询节点
├── rag/              # RAG相关
│   ├── LoveAppContextualQueryAugmenterFactory.java # 爱情应用上下文查询增强器工厂
│   ├── LoveAppDocumentLoader.java     # 爱情应用文档加载器
│   ├── LoveAppRagCloudAdvisorConfig.java # 爱情应用RAG云顾问配置
│   ├── LoveAppRagCustomAdvisorFactory.java # 爱情应用RAG自定义顾问工厂
│   ├── LoveAppVectorStoreConfig.java  # 爱情应用向量存储配置
│   ├── MyKeywordEnricher.java         # 自定义关键词增强器
│   ├── MyTokenTextSplitter.java       # 自定义令牌文本分割器
│   └── PgVectorVectorStoreConfig.java # PG向量存储配置
├── service/          # 服务层
├── tools/            # 工具实现
│   ├── FileOperationTool.java         # 文件操作工具
│   ├── PDFGenerationTool.java         # PDF生成工具
│   ├── ResourceDownloadTool.java      # 资源下载工具
│   ├── TerminalOperationTool.java     # 终端操作工具
│   ├── TerminateTool.java             # 终止工具
│   └── ToolRegistration.java          # 工具注册
└── YuAiAgentApplication.java          # 应用入口
```

## 核心功能

### 智能体系统

Yu-AI-Agent 提供了多种智能体实现：

1. **YuManus**：鱼皮超级智能体，拥有自主规划能力，可以直接使用
2. **ReActAgent**：基于 ReAct 模式的智能体，实现了思考-行动循环
3. **ToolCallAgent**：处理工具调用的基础代理类

### 工具集

平台支持多种工具调用：

- **文件操作工具**：读取、写入、删除文件
- **资源下载工具**：下载网络资源
- **终端操作工具**：执行终端命令
- **PDF 生成工具**：生成 PDF 文档
- **终止工具**：结束智能体执行

### 记忆管理

支持多种对话记忆实现：

- 文件存储记忆
- Redis 记忆
- Redisson 记忆

### RAG 知识库

集成向量数据库，支持文档检索增强生成（RAG）：

- PostgreSQL PGvector 扩展
- Elasticsearch

## API 文档

项目使用 Knife4j 生成 API 文档，访问地址：

```
http://localhost:8123/api/doc.html
```

## 配置说明

主要配置项说明：

| 配置项                                 | 说明                    | 默认值                                   |
| -------------------------------------- | ----------------------- | ---------------------------------------- |
| server.port                            | 服务器端口              | 8123                                     |
| server.servlet.context-path            | 应用上下文路径          | /api                                     |
| spring.datasource.url                  | 数据库连接 URL          | jdbc:postgresql://localhost:5432/sdagent |
| spring.datasource.username             | 数据库用户名            | postgres                                 |
| spring.datasource.password             | 数据库密码              | postgres                                 |
| spring.ai.dashscope.api-key            | 阿里云通义千问 API 密钥 | -                                        |
| spring.ai.dashscope.chat.options.model | 聊天模型                | qwen-plus                                |
| spring.ai.ollama.base-url              | Ollama 服务地址         | http://localhost:11434                   |
| spring.ai.ollama.chat.model            | Ollama 模型             | deepseek-r1:1.5b                         |
| file.upload.temp-dir                   | 文件上传临时目录        | E:/编程学习/项目/yv-ai/upload/temp       |
| file.upload.final-dir                  | 文件上传最终目录        | E:/编程学习/项目/yv-ai/upload/files      |

## 部署

### 本地部署

直接运行 Spring Boot 应用：

```bash
mvn spring-boot:run
```

### Docker 部署

使用 Docker Compose 部署（示例）：

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - '8123:8123'
    depends_on:
      - postgres
      - redis
      - elasticsearch
      - rabbitmq
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/sdagent
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      - SPRING_REDIS_HOST=redis
      - SPRING_RABBITMQ_HOST=rabbitmq

  postgres:
    image: postgres:14
    environment:
      - POSTGRES_DB=sdagent
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7

  elasticsearch:
    image: elasticsearch:8
    environment:
      - discovery.type=single-node
      - ES_JAVA_OPTS=-Xms1g -Xmx1g

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - '15672:15672'

volumes:
  postgres_data:
```

## 开发指南

### 智能体开发

创建新的智能体需要继承 `BaseAgent` 或其实现类：

```java
@Component
public class MyAgent extends ToolCallAgent {
    public MyAgent(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("myAgent");
        String SYSTEM_PROMPT = "You are MyAgent, an AI assistant...";
        this.setSystemPrompt(SYSTEM_PROMPT);
        this.setNextStepPrompt("Based on user needs...");
        this.setMaxSteps(10);
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
```

### 工具开发

创建新工具需要实现 `ToolCallback` 接口或使用 `@Tool` 注解：

```java
@Component
public class MyTool {
    @Tool(description = "My custom tool")
    public String myTool(@ToolParam(description = "Parameter") String param) {
        // 工具实现逻辑
        return "Result: " + param;
    }
}
```

然后在 `ToolRegistration` 类中注册新工具：

```java
@Bean
public ToolCallback[] allTools() {
    // 现有工具
    MyTool myTool = new MyTool();
    return ToolCallbacks.from(
            // 现有工具
            myTool
    );
}
```

## 测试

运行单元测试：

```bash
mvn test
```

## 许可证

[MIT License](LICENSE)

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题或建议，请通过以下方式联系我们：

- 邮箱：[your-email@example.com]
- GitHub：[https://github.com/your-username/yu-ai-agent]
