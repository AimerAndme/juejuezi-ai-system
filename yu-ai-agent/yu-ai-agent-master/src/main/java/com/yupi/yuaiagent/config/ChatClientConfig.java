package com.yupi.yuaiagent.config;

import com.yupi.yuaiagent.advisor.*;
import com.yupi.yuaiagent.chatmemory.RedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class ChatClientConfig {
    private static final String SYSTEM_PROMPT = "榆林市神东煤矿生产协同专有对话系统，" +
            "服务于煤矿地质勘探、井下设备调度、生产参数优化、基础对话等场景，所有操作需符合《煤矿安全生产规程》" +
            "及神东煤矿内部数据安全规范。";
    private final ToolCallbackProvider toolCallbackProvider;
    private final RedisChatMemory redisChatMemory;
    private final RetrievalRerankAdvisor retrievalRerankAdvisor;
    private final MyThreadPoolLoggerAdvisor myThreadPoolLoggerAdvisor;
    private final MyLoggerAdvisor myLoggerAdvisor;
    private final ToolCallback[] mineControllerTools;
    @Autowired
    public ChatModel dashscopeChatModel;
    @Autowired
    public RedisTemplate redisTemplate;

    public ChatClientConfig(ToolCallbackProvider toolCallbackProvider, RedisChatMemory redisChatMemory, RetrievalRerankAdvisor retrievalRerankAdvisor, MyThreadPoolLoggerAdvisor myThreadPoolLoggerAdvisor, MyLoggerAdvisor myLoggerAdvisor, ToolCallback[] mineControllerTools) {
        this.toolCallbackProvider = toolCallbackProvider;
        this.redisChatMemory = redisChatMemory;
        this.retrievalRerankAdvisor = retrievalRerankAdvisor;
        this.myThreadPoolLoggerAdvisor = myThreadPoolLoggerAdvisor;
        this.myLoggerAdvisor = myLoggerAdvisor;


        this.mineControllerTools = mineControllerTools;
    }

    @Bean
    public ChatClient agentChatClient() {
        RedisChatMemory redisChatMemory = new RedisChatMemory(redisTemplate);
        return ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(redisChatMemory).build(),
                        new MyThreadPoolLoggerAdvisor()
                        , new ReReadingAdvisor()
                ).build();
    }

    @Bean
    public ChatClient chatClient() {
        return ChatClient
                .builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(myLoggerAdvisor)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }

    //线程池日志收集chatClient
    @Bean
    public ChatClient logChatClient() {
        return ChatClient
                .builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(myThreadPoolLoggerAdvisor)
                .build();
    }

    //意图识别client
    @Bean
    public ChatClient intentChatClient() {
        return ChatClient
                .builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(myLoggerAdvisor)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }

    //记忆通用client
    @Bean
    public ChatClient memoryChatClient() {
        return ChatClient
                .builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultAdvisors(
                        myLoggerAdvisor,
                        MessageMemoryAdvisor.builder(redisChatMemory).enableSummary(true).summaryChatClient(summaryChatClient()).build()
                )
                .build();
    }

    /**
     * 带记忆的rag问答客户端
     *
     * @return
     */
    @Bean
    public ChatClient ragChatClient() {
        return ChatClient
                .builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultAdvisors(
                        myLoggerAdvisor,
                        retrievalRerankAdvisor,
                        MessageMemoryAdvisor.builder(redisChatMemory).enableSummary(true).summaryChatClient(summaryChatClient()).build()
                )
                .build();
    }

    //Nltosql专用client
    @Bean
    public ChatClient nl2SqlChatClient() {
        return ChatClient
                .builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    //DB工具调用专用client
    @Bean
    public ChatClient DbToolChatClient() {
        return ChatClient
                .builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(mineControllerTools)
                .build();
    }

    //摘要总结专用client
    @Bean
    public ChatClient summaryChatClient() {
        return ChatClient
                .builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }
}
