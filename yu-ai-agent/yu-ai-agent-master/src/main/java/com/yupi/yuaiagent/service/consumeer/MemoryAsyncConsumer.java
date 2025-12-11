package com.yupi.yuaiagent.service.consumeer;

import com.rabbitmq.client.Channel;
import com.yupi.yuaiagent.config.RabbitMQConfig;
import com.yupi.yuaiagent.domin.entity.MemoryFragment;
import com.yupi.yuaiagent.domin.entity.MiningAgentConversationMsg;
import com.yupi.yuaiagent.mapper.MiningAgentConversationMsgMapper;
import com.yupi.yuaiagent.service.MemoryIdempotentService;
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
import java.util.stream.Collectors;

/**
 * 非核心记忆消费者：批量处理异步写入（Milvus+PG jsonb）
 */
@Slf4j
@Component
public class MemoryAsyncConsumer {

    @Autowired
    private MemoryIdempotentService idempotentService; // 幂等性校验服务
    @Autowired
    private MiningAgentConversationMsgMapper miningAgentConversationMsgMapper;
    @Autowired
    private MessageSerializer messageSerializer;

    /**
     * 批量消费非核心记忆消息
     *
     * @param fragments    批量记忆片段
     * @param channel      消息通道（手动ACK用）
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
            return;
        }

        try {
            // 1. 幂等性校验：过滤已处理的记忆（避免重复写入）
            List<MemoryFragment> validFragments = fragments.stream()
                    .filter(fragment -> idempotentService.checkAndMark(fragment.getMemoryId()))
                    .collect(Collectors.toList());

            if (validFragments.isEmpty()) {
                log.info("批量消息中无有效记忆，直接ACK");
                channel.basicAck(deliveryTags.get(deliveryTags.size() - 1), true);
                return;
            }
            // 2. 批量生成 Embedding（提升效率，降低API调用成本）
            // 3. 批量写入 Milvus 向量库
            // 4. 批量写入 PG jsonb 元数据表
            //从已过滤的会话内容存入msg表，通过会话id定位
            String msgId = UniqueIdGenerator.generateRandomUuid();
            for (MemoryFragment validFragment : validFragments) {
                MiningAgentConversationMsg message = new MiningAgentConversationMsg();
                message.setConversationId(validFragment.getSessionId());
                message.setMsgType(validFragment.getMessageType());
                String json = JsonUtils.toJson(validFragment.getExtraMeta());
                message.setFileMeta(json);
                String content = validFragment.getContent();
                Message deserialize = MessageSerializer.deserialize(content);
                message.setMsgContent(deserialize.getText());
                message.setSenderId(validFragment.getUserId());
                message.setMsgId(msgId);
                miningAgentConversationMsgMapper.insert(message);
            }
            // 5. 批量ACK：确认消息处理完成（手动ACK避免消息丢失）
            channel.basicAck(deliveryTags.get(deliveryTags.size() - 1), true);
            log.info("非核心记忆批量处理成功，有效数量：{}", validFragments.size());

        } catch (Exception e) {
            log.error("非核心记忆批量处理失败", e);
            // 批量NACK：requeue=false（不再重试，直接进入死信队列）
            channel.basicNack(deliveryTags.get(deliveryTags.size() - 1), true, false);
            throw new RuntimeException("批量处理失败，消息进入死信队列", e);
        }
    }
}