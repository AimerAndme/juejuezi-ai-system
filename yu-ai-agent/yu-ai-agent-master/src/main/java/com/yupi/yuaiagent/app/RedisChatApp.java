package com.yupi.yuaiagent.app;

import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import com.yupi.yuaiagent.chatmemory.RedisChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 基于Redis实现对话记忆的聊天客户端
 */
@Component
@Slf4j
public class RedisChatApp {

    private static final String SYSTEM_PROMPT = "您是一个智能助手，能够帮助用户回答问题并提供有用的信息。"
            + "请基于对话历史进行回复，保持回答的连贯性和上下文相关性。";

    private final ChatClient chatClient;
    private final RedisChatMemory redisChatMemory;

    /**
     * 初始化基于Redis的聊天客户端
     *
     * @param chatModel 聊天模型
     * @param redisChatMemory Redis对话记忆
     */
    @Autowired
    public RedisChatApp(ChatModel dashscopeChatModel, RedisChatMemory redisChatMemory) {
        this.redisChatMemory = redisChatMemory;

        // 构建聊天客户端，使用RedisChatMemory作为对话记忆存储
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(redisChatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();

        org.slf4j.LoggerFactory.getLogger(RedisChatApp.class).info("RedisChatApp初始化成功，使用Redis作为对话记忆存储");
    }

    /**
     * 执行聊天操作
     *
     * @param conversationId 对话ID，用于标识不同的对话会话
     * @param message 用户消息
     * @return 聊天响应
     */
//    public ChatResponse doChat(String conversationId, String message) {
//        return chatClient.prompt()
//                .user(message)
//                .advisors(a -> a.param(MessageChatMemoryAdvisor.CONVERSATION_MEMORY_KEY, conversationId))
//                .system(systemMessage -> systemMessage.content(SYSTEM_PROMPT))
//                .user(userMessage -> userMessage.content(message))
//                .options(options -> options.conversationId(conversationId))
//                .call();
//    }
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
    /**
     * 清空指定对话的历史记录
     *
     * @param conversationId 对话ID
     */
    public void clearChatHistory(String conversationId) {
        redisChatMemory.clear(conversationId);
        log.info("已清空对话历史，会话ID: {}", conversationId);
    }

    /**
     * 获取对话历史消息数量
     *
     * @param conversationId 对话ID
     * @return 消息数量
     */
    public int getMessageCount(String conversationId) {
        int count = redisChatMemory.get(conversationId).size();
        log.info("对话消息数量查询，会话ID: {}, 消息数量: {}", conversationId, count);
        return count;
    }
}
