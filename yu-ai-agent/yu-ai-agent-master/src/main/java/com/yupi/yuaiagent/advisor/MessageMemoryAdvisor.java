package com.yupi.yuaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.util.Assert;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MessageMemoryAdvisor implements BaseChatMemoryAdvisor {
    private final ChatMemory chatMemory;
    private final String defaultConversationId;
    private final int order;
    private final Scheduler scheduler;

    private MessageMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int order, Scheduler scheduler) {
        Assert.notNull(chatMemory, "chatMemory cannot be null");
        Assert.hasText(defaultConversationId, "defaultConversationId cannot be null or empty");
        Assert.notNull(scheduler, "scheduler cannot be null");
        this.chatMemory = chatMemory;
        this.defaultConversationId = defaultConversationId;
        this.order = order;
        this.scheduler = scheduler;
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
        //提取当前会话的用户提问内容
        processedMessages.addAll(chatClientRequest.prompt().getInstructions());
        //创建chat请求，将最新的message放入
        ChatClientRequest processedChatClientRequest = chatClientRequest.mutate().prompt(chatClientRequest.prompt().mutate().messages(processedMessages).build()).build();
        UserMessage userMessage = processedChatClientRequest.prompt().getUserMessage();
        this.chatMemory.add(conversationId, userMessage);
        return processedChatClientRequest;
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

        public MessageMemoryAdvisor build() {
            return new MessageMemoryAdvisor(this.chatMemory, this.conversationId, this.order, this.scheduler);
        }
    }
}
