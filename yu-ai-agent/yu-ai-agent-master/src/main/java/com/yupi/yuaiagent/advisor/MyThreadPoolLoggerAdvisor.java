package com.yupi.yuaiagent.advisor;

import com.yupi.yuaiagent.logging.NodeExecutionLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Component;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
@Component
@Slf4j
public class MyThreadPoolLoggerAdvisor implements BaseAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        String id = chatClientResponse.chatResponse().getMetadata().getId();
        Usage usage = chatClientResponse.chatResponse().getMetadata().getUsage();
        Integer promptTokens = usage.getPromptTokens();
        Integer completionTokens = usage.getCompletionTokens();
        Integer totalTokens = usage.getTotalTokens();
        //获取节点id
        String nodeId = (String) chatClientResponse.context().get("nodeId");
        NodeExecutionLog nodeLog = new NodeExecutionLog();
        if (nodeId != null) {
            try {
                nodeLog.setTokenUsed(totalTokens);
                nodeLog.setNodeId(nodeId);
            } catch (Exception e) {
                log.error("advisor：{}，nodeId:{},日志记录失败: {}", "logAdvisor", nodeId, e.getMessage());
            }
        }
        chatClientResponse.context().put("chatLog",nodeLog);
        //log.info("AI Response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
        //log.info("本次模型调用，会话id为：{}，token使用量：输入：{}，输出：{}，总计：{}", id, promptTokens, completionTokens, totalTokens);
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private ChatClientRequest before(ChatClientRequest request) {
        log.info("AI Request: before");
        return request;
    }

    private void observeAfter(ChatClientResponse chatClientResponse) {
        String id = chatClientResponse.chatResponse().getMetadata().getId();
        Usage usage = chatClientResponse.chatResponse().getMetadata().getUsage();
        Integer promptTokens = usage.getPromptTokens();
        Integer completionTokens = usage.getCompletionTokens();
        Integer totalTokens = usage.getTotalTokens();
        //log.info("AI Response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
        log.info("本次模型调用，会话id为：{}，token使用量：输入：{}，输出：{}，总计：{}", id, promptTokens, completionTokens, totalTokens);
    }

//    @Override
//    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
//        chatClientRequest = before(chatClientRequest);
//        ChatClientResponse chatClientResponse = chain.nextCall(chatClientRequest);
//        observeAfter(chatClientResponse);
//        return chatClientResponse;
//    }
//
//    @Override
//    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
//        chatClientRequest = before(chatClientRequest);
//        Flux<ChatClientResponse> chatClientResponseFlux = chain.nextStream(chatClientRequest);
//        return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponseFlux, this::observeAfter);
//    }
}
