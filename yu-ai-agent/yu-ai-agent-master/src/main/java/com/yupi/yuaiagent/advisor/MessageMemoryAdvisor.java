package com.yupi.yuaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.Assert;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MessageMemoryAdvisor implements BaseChatMemoryAdvisor {

    private final ChatMemory chatMemory;                 // 对话记忆存储实现
    private final String defaultConversationId;           // 默认对话ID
    private final int order;                              // 顾问执行顺序
    private final Scheduler scheduler;                    // 调度器
    private final int maxContextMessages;                 // 最大上下文消息数
    private final ChatClient summaryChatClient;           // 用于生成摘要的ChatClient
    private final int summaryThreshold;                   // 摘要生成阈值（旧版参数）
    private final boolean enableSummary;                  // 是否启用摘要功能
    private final int maxSummaries;                       // 最大摘要数量
    private final int summaryUpdateInterval;              // 摘要更新间隔（旧版参数）
    private final int batchSizeMin;                       // 最小批次大小（10条）
    private final int batchSizeMax;                       // 最大批次大小（20条）
    private final int batchTokenThreshold;                // 批次token阈值（1500）

    private MessageMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int order, Scheduler scheduler,
            int maxContextMessages, ChatClient summaryChatClient, int summaryThreshold, boolean enableSummary,
            int maxSummaries, int summaryUpdateInterval, int batchSizeMin, int batchSizeMax, int batchTokenThreshold) {
        Assert.notNull(chatMemory, "chatMemory cannot be null");
        Assert.hasText(defaultConversationId, "defaultConversationId cannot be null or empty");
        Assert.notNull(scheduler, "scheduler cannot be null");
        Assert.isTrue(maxContextMessages > 0, "maxContextMessages must be greater than 0");
        if (enableSummary) {
            Assert.notNull(summaryChatClient, "summaryChatClient cannot be null when enableSummary is true");
            Assert.isTrue(summaryThreshold > 0, "summaryThreshold must be greater than 0 when enableSummary is true");
            Assert.isTrue(maxSummaries > 0, "maxSummaries must be greater than 0 when enableSummary is true");
            Assert.isTrue(summaryUpdateInterval > 0, "summaryUpdateInterval must be greater than 0 when enableSummary is true");
            Assert.isTrue(batchSizeMin > 0, "batchSizeMin must be greater than 0 when enableSummary is true");
            Assert.isTrue(batchSizeMax > 0, "batchSizeMax must be greater than 0 when enableSummary is true");
            Assert.isTrue(batchSizeMax > batchSizeMin, "batchSizeMax must be greater than batchSizeMin");
            Assert.isTrue(batchTokenThreshold > 0, "batchTokenThreshold must be greater than 0 when enableSummary is true");
        }
        this.chatMemory = chatMemory;
        this.defaultConversationId = defaultConversationId;
        this.order = order;
        this.scheduler = scheduler;
        this.maxContextMessages = maxContextMessages;
        this.summaryChatClient = summaryChatClient;
        this.summaryThreshold = summaryThreshold;
        this.enableSummary = enableSummary;
        this.maxSummaries = maxSummaries;
        this.summaryUpdateInterval = summaryUpdateInterval;
        this.batchSizeMin = batchSizeMin;
        this.batchSizeMax = batchSizeMax;
        this.batchTokenThreshold = batchTokenThreshold;
    }

    public static MessageMemoryAdvisor.Builder builder(ChatMemory chatMemory) {
        return new MessageMemoryAdvisor.Builder(chatMemory);
    }

    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String conversationId = this.getConversationId(chatClientRequest.context(), this.defaultConversationId);
        List<Message> memoryMessages = this.chatMemory.get(conversationId);
        List<Message> processedMessages = new ArrayList<>();
        if (memoryMessages.isEmpty()) {
            log.info("会话id：{}，未查询到短期记忆", conversationId);
        }
        log.info("会话id：{}，查询到短期记忆{}条", conversationId, memoryMessages.size());

        if (this.enableSummary) {
            List<String> summaryList = null;

            if (this.chatMemory instanceof com.yupi.yuaiagent.chatmemory.RedisChatMemory) {
                com.yupi.yuaiagent.chatmemory.RedisChatMemory redisChatMemory
                        = (com.yupi.yuaiagent.chatmemory.RedisChatMemory) this.chatMemory;
                summaryList = redisChatMemory.getSummaryList(conversationId);
            }

            if (summaryList != null && !summaryList.isEmpty()) {
                String combinedSummary = String.join("\n\n", summaryList);
                Message summaryMessage = new SystemMessage("历史对话摘要：\n" + combinedSummary);
                processedMessages.add(summaryMessage);
                log.info("会话id：{}，使用{}条摘要", conversationId, summaryList.size());
            }

            processedMessages.addAll(memoryMessages);
            log.info("会话id：{}，上下文已组装为：{}条摘要 + {}条当前批次消息", conversationId,
                    summaryList != null ? summaryList.size() : 0, memoryMessages.size());
        } else {
            processedMessages = memoryMessages;
        }

        // 添加空值检查，避免getInstructions()返回null导致异常
        if (chatClientRequest.prompt() != null) {
            List<Message> instructions = chatClientRequest.prompt().getInstructions();
            if (instructions != null && !instructions.isEmpty()) {
                processedMessages.addAll(instructions);
            }
        }
        // 构建处理后的请求
        ChatClientRequest processedChatClientRequest = null;
        if (chatClientRequest.prompt() != null) {
            processedChatClientRequest = chatClientRequest.mutate()
                    .prompt(chatClientRequest.prompt().mutate().messages(processedMessages).build())
                    .build();
            // 保存用户消息到当前批次
            if (processedChatClientRequest != null && processedChatClientRequest.prompt() != null) {
                UserMessage userMessage = processedChatClientRequest.prompt().getUserMessage();
                if (userMessage != null) {
                    this.chatMemory.add(conversationId, userMessage);
                }
            }
        }
        if (processedChatClientRequest == null) {
            processedChatClientRequest = chatClientRequest;
        }
        return processedChatClientRequest;
    }

    private String generateSummary(List<Message> messages) {
        try {
            String conversationText = messages.stream()
                    .map(msg -> {
                        String role = msg.getMessageType().name();
                        String content = msg.getText();
                        return String.format("[%s]: %s", role, content);
                    })
                    .collect(Collectors.joining("\n"));

            String prompt = String.format(
                    "请将以下对话历史压缩成简洁的摘要，保留关键信息、重要决策和上下文。摘要应该清晰、简洁，便于后续对话理解：\n\n%s\n\n摘要：",
                    conversationText
            );

            return summaryChatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("摘要生成失败，将回退到纯滑动窗口模式。错误信息：{}", e.getMessage());
            return null;
        }
    }

    private int calculateTokens(List<Message> messages) {
        return messages.stream()
                .mapToInt(msg -> {
                    String content = msg.getText();
                    return (int) (content.length() * 1.5);
                })
                .sum();
    }

    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        List<AssistantMessage> assistantMessages = new ArrayList();
        if (chatClientResponse.chatResponse() != null) {
            assistantMessages = chatClientResponse.chatResponse().getResults().stream().map(Generation::getOutput).toList();
        }
        String conversationId = this.getConversationId(chatClientResponse.context(), this.defaultConversationId);
        this.chatMemory.add(conversationId, (List) assistantMessages);

        if (this.enableSummary && this.chatMemory instanceof com.yupi.yuaiagent.chatmemory.RedisChatMemory redisChatMemory) {

            List<Message> currentBatch = redisChatMemory.get(conversationId);
            int batchTokens = calculateTokens(currentBatch);

            boolean shouldGenerateSummary = false;

            if (currentBatch.size() >= this.batchSizeMax) {
                shouldGenerateSummary = true;
                log.info("会话id：{}，批次已达到最大限制（{}条），强制生成摘要",
                        conversationId, currentBatch.size());
            } else if (currentBatch.size() >= this.batchSizeMin && batchTokens >= this.batchTokenThreshold) {
                shouldGenerateSummary = true;
                log.info("会话id：{}，批次已达到token阈值（{} tokens, {}条），生成摘要",
                        conversationId, batchTokens, currentBatch.size());
            }

            if (shouldGenerateSummary) {
                String summary = generateSummary(currentBatch);

                if (summary != null) {
                    redisChatMemory.addSummaryToList(conversationId, summary, this.maxSummaries);
                    redisChatMemory.clearCurrentBatch(conversationId);
                    log.info("会话id：{}，摘要已生成并保存，当前批次已清空", conversationId);
                } else {
                    log.warn("会话id：{}，摘要生成失败", conversationId);
                }
            }
        }

        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public static final class Builder {

        private String conversationId = "default";
        private int order = -2147482648;
        private Scheduler scheduler;
        private ChatMemory chatMemory;
        private int maxContextMessages = 10;
        private ChatClient summaryChatClient;
        private int summaryThreshold = 5;
        private boolean enableSummary = false;
        private int maxSummaries = 5;
        private int summaryUpdateInterval = 10;
        private int batchSizeMin = 10;
        private int batchSizeMax = 20;
        private int batchTokenThreshold = 1500;

        private Builder(ChatMemory chatMemory) {
            this.scheduler = BaseAdvisor.DEFAULT_SCHEDULER;
            this.chatMemory = chatMemory;
        }

        public MessageMemoryAdvisor.Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public MessageMemoryAdvisor.Builder order(int order) {
            this.order = order;
            return this;
        }

        public MessageMemoryAdvisor.Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public MessageMemoryAdvisor.Builder maxContextMessages(int maxContextMessages) {
            this.maxContextMessages = maxContextMessages;
            return this;
        }

        public MessageMemoryAdvisor.Builder summaryChatClient(ChatClient summaryChatClient) {
            this.summaryChatClient = summaryChatClient;
            return this;
        }

        public MessageMemoryAdvisor.Builder summaryThreshold(int summaryThreshold) {
            this.summaryThreshold = summaryThreshold;
            return this;
        }

        public MessageMemoryAdvisor.Builder enableSummary(boolean enableSummary) {
            this.enableSummary = enableSummary;
            return this;
        }

        public MessageMemoryAdvisor.Builder maxSummaries(int maxSummaries) {
            this.maxSummaries = maxSummaries;
            return this;
        }

        public MessageMemoryAdvisor.Builder summaryUpdateInterval(int summaryUpdateInterval) {
            this.summaryUpdateInterval = summaryUpdateInterval;
            return this;
        }

        public MessageMemoryAdvisor.Builder batchSizeMin(int batchSizeMin) {
            this.batchSizeMin = batchSizeMin;
            return this;
        }

        public MessageMemoryAdvisor.Builder batchSizeMax(int batchSizeMax) {
            this.batchSizeMax = batchSizeMax;
            return this;
        }

        public MessageMemoryAdvisor.Builder batchTokenThreshold(int batchTokenThreshold) {
            this.batchTokenThreshold = batchTokenThreshold;
            return this;
        }

        public MessageMemoryAdvisor build() {
            return new MessageMemoryAdvisor(this.chatMemory, this.conversationId, this.order, this.scheduler,
                    this.maxContextMessages, this.summaryChatClient, this.summaryThreshold, this.enableSummary,
                    this.maxSummaries, this.summaryUpdateInterval, this.batchSizeMin, this.batchSizeMax, this.batchTokenThreshold);
        }
    }
}
