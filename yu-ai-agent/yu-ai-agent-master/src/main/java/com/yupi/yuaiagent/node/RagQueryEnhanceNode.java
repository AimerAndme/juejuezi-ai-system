package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import com.yupi.yuaiagent.utils.ExecutionTimeUtils;
import com.yupi.yuaiagent.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component("ragQueryEnhanceNode")
public class RagQueryEnhanceNode implements NodeAction {
    private final ChatClient ragChatClient;
    private final ChatClient chatClient;
    private final ThreadPoolTaskExecutor pdfPageExecutor;
    public RagQueryEnhanceNode(ChatClient ragChatClient, ChatClient chatClient, ThreadPoolTaskExecutor   pdfPageExecutor) {
        this.ragChatClient = ragChatClient;
        this.chatClient = chatClient;
        this.pdfPageExecutor = pdfPageExecutor;
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
        log.info(" ragQueryEnhanceNode 开始执行");
        UserChatVO queryInfo = (UserChatVO) state.value("queryInfo").get();
        if (queryInfo.getQuery() == null) {
            log.error("无法获取用户输入内容");
        }
        //1、多查询生成+并发处理
        PromptTemplate multiPromptTemplate = new PromptTemplate("""
                你是查询优化专家，将用户原始问题改写为3个语义一致、表述不同的查询，用于提升检索覆盖率。输出严格为JSON数组格式，不要额外文字。
                 用户问题：{queryInfo}"
                """);
        multiPromptTemplate.add("queryInfo", queryInfo.getQuery());
        String content = ExecutionTimeUtils.monitorExecutionTime("multiPromptChatClient", () -> {
            return chatClient
                    .prompt(multiPromptTemplate.render())
                    .user(queryInfo.getQuery())
                    .advisors(spec -> spec.params(
                            Map.of("chat_memory_conversation_id", queryInfo.getConversationId(),
                                    "nodeId", "RagQueryEnhanceNode")
                    ))
                    .call().content();
        });
        List<String> queries = JsonUtils.fromJson(content, ArrayList.class);
        List<String> hydeQueryAnswer = new ArrayList<>();
        //2、hyde假设文档
        List<CompletableFuture<Void>> hydeFutureList = new ArrayList<>();
        for (String query : queries) {
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                log.info(" hydePromptTemplate 开始执行");
                PromptTemplate hydePromptTemplate = new PromptTemplate("""
                        根据用户问题，生成一段详细、专业的模拟标准答案文档，用于优化向量检索。仅输出文档内容
                         用户问题：{queryInfo}"
                        """);
                hydePromptTemplate.add("queryInfo", query);
                String hydeContent = ExecutionTimeUtils.monitorExecutionTime("hypeChatClient", () -> {
                    return chatClient
                            .prompt(hydePromptTemplate.render())
                            .user(queryInfo.getQuery())
                            .advisors(spec -> spec.params(
                                    Map.of("chat_memory_conversation_id", queryInfo.getConversationId(),
                                            "nodeId", "RagQueryEnhanceNode")
                            ))
                            .call().content();
                });
                hydeQueryAnswer.add(hydeContent);
            },pdfPageExecutor).exceptionally(e -> {
                log.error("hydePromptTemplate执行时发生异常:{},query:{}", e.getMessage(), query);
                return null;
            });
            hydeFutureList.add(completableFuture);
        }
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(hydeFutureList.toArray(new CompletableFuture[0]));
        try {
            allFutures.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            log.error("等待hydeFutureList完成时发生异常:{}", e.getMessage());
            return Map.of("hydeQueryAnswer", hydeQueryAnswer); // Return the result even if an exception occurs
        }
        log.info("ragQueryEnhanceNode 执行完成, hydeQueryAnswer: {}", hydeQueryAnswer);
        return Map.of("hydeQueryAnswer", hydeQueryAnswer);
    }
}
