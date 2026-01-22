package com.yupi.yuaiagent.service.consumer;

import com.rabbitmq.client.Channel;
import com.yupi.yuaiagent.config.RabbitMQConfig;
import com.yupi.yuaiagent.domin.entity.MemoryFragment;
import com.yupi.yuaiagent.domin.entity.MiningAgentConversationMsg;
import com.yupi.yuaiagent.exception.MemoryDataValidationException;
import com.yupi.yuaiagent.exception.MemoryFormatException;
import com.yupi.yuaiagent.exception.MemorySerializationException;
import com.yupi.yuaiagent.mapper.MiningAgentConversationMsgMapper;
import com.yupi.yuaiagent.service.MemoryIdempotentService;
import com.yupi.yuaiagent.service.MemoryValidationService;
import com.yupi.yuaiagent.util.UniqueIdGenerator;
import com.yupi.yuaiagent.utils.JsonUtils;
import com.yupi.yuaiagent.utils.MessageSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * 非核心记忆消费者：批量处理异步写入（Milvus+PG jsonb）
 */
@Slf4j
@Component
public class MemoryAsyncConsumer {

    @Autowired
    private MemoryIdempotentService idempotentService;
    @Autowired
    private MemoryValidationService validationService;
    @Autowired
    private MiningAgentConversationMsgMapper miningAgentConversationMsgMapper;
    @Autowired
    private MessageSerializer messageSerializer;

    /**
     * 批量消费非核心记忆消息
     *
     * @param fragments 批量记忆片段
     * @param channel 消息通道（手动ACK用）
     * @param deliveryTags 消息投递标签（批量ACK用）
     */
    @RabbitListener(
            queues = RabbitMQConfig.MEMORY_ASYNC_QUEUE,
            containerFactory = "batchRabbitListenerContainerFactory" // 批量消费工厂
    )
    @Transactional(rollbackFor = Exception.class)
    public void batchConsumeMemory(
            List<MemoryFragment> fragments,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) List<Long> deliveryTags
    ) throws IOException {
        log.info("收到非核心记忆批量消息，数量：{}", fragments.size());

        if (fragments.isEmpty()) {
            channel.basicAck(deliveryTags.get(deliveryTags.size() - 1), true); // 批量ACK空消息
            log.info("收到空批量消息，直接ACK");
            return;
        }

        try {
            for (MemoryFragment fragment : fragments) {
                validationService.validateMemoryFragment(fragment);
            }

            List<MemoryFragment> validFragments = fragments.stream()
                    .filter(fragment -> idempotentService.checkAndMark(fragment.getMemoryId()))
                    .toList();

            if (validFragments.isEmpty()) {
                log.info("批量消息中无有效记忆，直接ACK");
                channel.basicAck(deliveryTags.get(deliveryTags.size() - 1), true);
                return;
            }

            String msgId = UniqueIdGenerator.generateRandomUuid();
            for (MemoryFragment validFragment : validFragments) {
                MiningAgentConversationMsg message = new MiningAgentConversationMsg();
                message.setConversationId(validFragment.getSessionId());
                message.setMsgType(validFragment.getMessageType());
                String json = JsonUtils.toJson(validFragment.getExtraMeta());
                message.setFileMeta(json);
                String content = validFragment.getContent();
                Message deserialize;
                try {
                    deserialize = MessageSerializer.deserialize(content);
                } catch (Exception e) {
                    throw new MemorySerializationException("deserialize", content, "消息反序列化失败", e);
                }
                message.setMsgContent(deserialize.getText());
                message.setSenderId(validFragment.getUserId());
                message.setMsgId(msgId);
                miningAgentConversationMsgMapper.insert(message);
            }

            channel.basicAck(deliveryTags.get(deliveryTags.size() - 1), true);
            log.info("非核心记忆批量处理成功，有效数量：{}", validFragments.size());

        } catch (MemoryDataValidationException | MemoryFormatException | MemorySerializationException e) {
            log.error("业务异常，消息进入死信队列：{}", e.getMessage());
            channel.basicNack(deliveryTags.get(deliveryTags.size() - 1), true, false);
        }
    }
}
