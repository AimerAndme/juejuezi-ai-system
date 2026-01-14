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
import org.springframework.util.Assert;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MessageMemoryAdvisor implements BaseChatMemoryAdvisor {

    private final ChatMemory chatMemory;
    private final String defaultConversationId;
    private final int order;
    private final Scheduler scheduler;
    private final int maxContextMessages;
    private final ChatClient summaryChatClient;
    private final int summaryThreshold;
    private final boolean enableSummary;
    private final int maxSummaries;
    private final int summaryUpdateInterval;

    private MessageMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int order, Scheduler scheduler,
            int maxContextMessages, ChatClient summaryChatClient, int summaryThreshold, boolean enableSummary,
            int maxSummaries, int summaryUpdateInterval) {
        Assert.notNull(chatMemory, "chatMemory cannot be null");
        Assert.hasText(defaultConversationId, "defaultConversationId cannot be null or empty");
        Assert.notNull(scheduler, "scheduler cannot be null");
        Assert.isTrue(maxContextMessages > 0, "maxContextMessages must be greater than 0");
        if (enableSummary) {
            Assert.notNull(summaryChatClient, "summaryChatClient cannot be null when enableSummary is true");
            Assert.isTrue(summaryThreshold > 0, "summaryThreshold must be greater than 0 when enableSummary is true");
            Assert.isTrue(maxSummaries > 0, "maxSummaries must be greater than 0 when enableSummary is true");
            Assert.isTrue(summaryUpdateInterval > 0, "summaryUpdateInterval must be greater than 0 when enableSummary is true");
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
    }

    public static MessageMemoryAdvisor.Builder builder(ChatMemory chatMemory) {
        return new MessageMemoryAdvisor.Builder(chatMemory);
    }

    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String conversationId = this.getConversationId(chatClientRequest.context(), this.defaultConversationId);
        List<Message> memoryMessages = this.chatMemory.get(conversationId);
        List<Message> processedMessages = new ArrayList(memoryMessages);
        if (processedMessages == null || processedMessages.size() == 0) {
            log.info("会话id：{}，未查询到短期记忆", conversationId);
        }
        log.info("会话id：{}，查询到短期记忆{}条", conversationId, processedMessages.size());

        if (processedMessages.size() > this.maxContextMessages) {
            if (this.enableSummary && processedMessages.size() > this.summaryThreshold) {
                List<String> summaryList = null;

                if (this.chatMemory instanceof com.yupi.yuaiagent.chatmemory.RedisChatMemory) {
                    com.yupi.yuaiagent.chatmemory.RedisChatMemory redisChatMemory
                            = (com.yupi.yuaiagent.chatmemory.RedisChatMemory) this.chatMemory;
                    summaryList = redisChatMemory.getSummaryList(conversationId);
                }

                List<Message> recentMessages = processedMessages.subList(
                        processedMessages.size() - this.summaryThreshold,
                        processedMessages.size()
                );

                processedMessages = new ArrayList<>();

                if (summaryList != null && !summaryList.isEmpty()) {
                    String combinedSummary = String.join("\n\n", summaryList);
                    Message summaryMessage = new SystemMessage("历史对话摘要：\n" + combinedSummary);
                    processedMessages.add(summaryMessage);
                    log.info("会话id：{}，使用{}条摘要", conversationId, summaryList.size());
                }

                processedMessages.addAll(recentMessages);
                log.info("会话id：{}，上下文已组装为：{}条摘要 + {}条最近消息", conversationId,
                        summaryList != null ? summaryList.size() : 0, recentMessages.size());
            } else {
                processedMessages = processedMessages.subList(
                        processedMessages.size() - this.maxContextMessages,
                        processedMessages.size()
                );
                log.info("会话id：{}，已裁剪至最近{}条消息", conversationId, this.maxContextMessages);
            }
        }
        processedMessages.addAll(chatClientRequest.prompt().getInstructions());
        ChatClientRequest processedChatClientRequest = chatClientRequest.mutate().prompt(chatClientRequest.prompt().mutate().messages(processedMessages).build()).build();
        UserMessage userMessage = processedChatClientRequest.prompt().getUserMessage();
        this.chatMemory.add(conversationId, userMessage);
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

    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        List<AssistantMessage> assistantMessages = new ArrayList();
        if (chatClientResponse.chatResponse() != null) {
            assistantMessages = chatClientResponse.chatResponse().getResults().stream().map((g) -> {
                return g.getOutput();
            }).toList();
        }
        this.chatMemory.add(this.getConversationId(chatClientResponse.context(), this.defaultConversationId), (List) assistantMessages);
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

        public MessageMemoryAdvisor build() {
            return new MessageMemoryAdvisor(this.chatMemory, this.conversationId, this.order, this.scheduler,
                    this.maxContextMessages, this.summaryChatClient, this.summaryThreshold, this.enableSummary,
                    this.maxSummaries, this.summaryUpdateInterval);
        }
    }
}
