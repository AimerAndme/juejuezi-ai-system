package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import com.yupi.yuaiagent.utils.ExecutionTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RagQueryNode implements NodeAction {
    private final ChatClient ragChatClient;

    public RagQueryNode(ChatClient ragChatClient) {
        this.ragChatClient = ragChatClient;

    }

    @Override
    @ExecutionTimeMonitor
    @Retryable(
            value = {
                    // 服务端错误，如500、502、503等，这些可能是临时性错误
                    org.springframework.web.client.HttpServerErrorException.class,
                    // 连接超时等网络异常
                    java.net.SocketTimeoutException.class,
                    // 其他运行时异常，但要小心，避免重试不应该重试的错误
                    RuntimeException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info(" ragChatClient 开始执行");
        List<String> hypeQueryAnswer = (List<String>) state.value("hypeQueryAnswer").get();
        UserChatVO queryInfo = (UserChatVO) state.value("queryInfo").get();
        List<String> hypeAnswerList = new ArrayList<>();
            PromptTemplate promptTemplate = new PromptTemplate("""
                    "你是一位严谨的研究分析师，你的任务是基于外部知识库回答用户问题。请严格遵循以下流程：
                    
                     1. **意图解析**：精准拆解用户问题的核心诉求和关键实体。
                     2. **检索模拟**：基于解析出的关键点，设想你需要从大型文档库中检索哪些具体的段落或数据。请列出你认为必须查找的 3-5 个核心信息点（关键词/短语）。
                     3. **信息验证**：在获取到相关段落后，首先核对信息的一致性；若信息缺失或矛盾，请明确指出，不要猜测。
                     4. **综合回答**：仅使用检索到的内容进行逻辑整合，生成详尽、准确且流畅的回答。
                    
                     **重要约束**：
                     - 回答必须完全基于检索内容，严禁引入外部知识或主观臆断。
                     - 如果检索内容不足以回答问题，请如实告知“当前资料不足以回答该问题”。
                    
                     用户问题：{queryInfo}"
                    """);
            promptTemplate.add("queryInfo", queryInfo.getQuery());
            String content = ExecutionTimeUtils.monitorExecutionTime("ragChatClient", () -> {
                return ragChatClient
                        .prompt(promptTemplate.render())
                        .user(queryInfo.getQuery())
                        .advisors(spec -> spec.params(
                                Map.of("chat_memory_conversation_id", queryInfo.getConversationId(),
                                        "nodeId", "RagQueryNode")
                        ))
                        .call().content();
            });
            if (content == null) {
                log.info(" ragChatClient,问题：{} 输出结果为空", queryInfo.getQuery());
            }
            hypeAnswerList.add(content);
        log.info(" ragChatClient 输出");
        return Map.of("chatResult", hypeAnswerList);
    }
}
