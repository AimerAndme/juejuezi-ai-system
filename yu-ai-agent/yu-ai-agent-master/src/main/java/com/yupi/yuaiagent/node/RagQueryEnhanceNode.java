package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import com.yupi.yuaiagent.logging.LogContextHolder;
import com.yupi.yuaiagent.logging.NodeExecutionLog;
import com.yupi.yuaiagent.utils.ExecutionTimeUtils;
import com.yupi.yuaiagent.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component("ragQueryEnhanceNode")
public class RagQueryEnhanceNode implements NodeAction {
    private final ChatClient ragChatClient;
    private final ChatClient chatClient;
    private final ThreadPoolTaskExecutor pdfPageExecutor;
    private final ChatClient logChatClient;

    public RagQueryEnhanceNode(ChatClient ragChatClient, ChatClient chatClient, ThreadPoolTaskExecutor pdfPageExecutor, @Qualifier("logChatClient") ChatClient logChatClient) {
        this.ragChatClient = ragChatClient;
        this.chatClient = chatClient;
        this.pdfPageExecutor = pdfPageExecutor;
        this.logChatClient = logChatClient;
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
        List<String> hydeQueryAnswer = new CopyOnWriteArrayList<>();
        //2、hyde假设文档
        List<CompletableFuture<ChatClientResponse>> hydeFutureList = new ArrayList<>();
        for (String query : queries) {
            CompletableFuture<ChatClientResponse> completableFuture = CompletableFuture.supplyAsync(() -> {
                log.info(" hydePromptTemplate 开始执行");
                PromptTemplate hydePromptTemplate = new PromptTemplate("""
                        根据用户问题，生成一段详细、专业的模拟标准答案文档，用于优化向量检索。仅输出文档内容
                         用户问题：{queryInfo}"
                        """);
                hydePromptTemplate.add("queryInfo", query);
                ChatClientResponse call = logChatClient
                        .prompt(hydePromptTemplate.render())
                        .user(queryInfo.getQuery())
                        .advisors(spec -> spec.params(
                                Map.of("chat_memory_conversation_id", queryInfo.getConversationId(),
                                        "nodeId", "RagQueryEnhanceNode")
                        ))
                        .call().chatClientResponse();
                return call;
            }, pdfPageExecutor).exceptionally(e -> {
                log.error("hydePromptTemplate执行时发生异常:{},query:{}", e.getMessage(), query);
                throw new RuntimeException("HYDE generation failed for query: " + query, e);
            });
            hydeFutureList.add(completableFuture);
        }
        try {
            CompletableFuture.allOf(hydeFutureList.toArray(new CompletableFuture[0]))
                    .orTimeout(30, TimeUnit.SECONDS)
                    .join(); // ← 使用 join()，不处理 checked exception
        } catch (CompletionException e) {
            log.warn("部分 hyde 任务超时或失败，继续聚合可用结果", e);
        }
        int i = 1;
        for (CompletableFuture<ChatClientResponse> future : hydeFutureList) {
            try {
                ChatClientResponse chatClientResponse = future.join();
                hydeQueryAnswer.add(chatClientResponse.chatResponse().getResult().getOutput().getText());
                NodeExecutionLog chatLog = (NodeExecutionLog) chatClientResponse.context().get("chatLog");
                LogContextHolder.addNodeLog("RagQueryEnhanceNode-hyde-" + i, chatLog);
            } catch (Exception e) {
                log.warn("获取 hyde #{} 结果时异常（已记录失败日志）", i, e);
            }
            i++;
        }
        log.info("ragQueryEnhanceNode 执行完成, hydeQueryAnswer: {}", hydeQueryAnswer);
        return Map.of("hydeQueryAnswer", hydeQueryAnswer);
    }
}
