package com.yupi.yuaiagent.service.producer;

import com.yupi.yuaiagent.config.RabbitMQConfig;
import com.yupi.yuaiagent.domin.entity.MemoryFragment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 非核心记忆消息生产者：发送到 RabbitMQ 队列
 */
@Slf4j
@Component
public class MqAsyncProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送非核心记忆到队列
     *
     * @param fragment 记忆片段（已生成唯一 memoryId 用于幂等性）
     */
    public void sendMemoryFragment(MemoryFragment fragment) {
        try {
            // 构建消息唯一标识（用于发布确认）
            CorrelationData correlationData = new CorrelationData(fragment.getMemoryId());

            // 发送消息到队列
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.MEMORY_EXCHANGE,
                    RabbitMQConfig.MEMORY_ROUTING_KEY,
                    fragment,
                    correlationData
            );

            log.info("非核心记忆消息发送成功，memoryId: {}", fragment.getMemoryId());
        } catch (Exception e) {
            log.error("非核心记忆消息发送失败，memoryId: {}", fragment.getMemoryId(), e);
            // 发送失败可记录到本地日志，后续通过对账机制补全
            throw new RuntimeException("消息发送失败", e);
        }
    }

    public void sendFileParseFragment(String fileMd5, String userId) {
        CorrelationData correlationData = new CorrelationData(fileMd5);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.FILE_EXCHANGE,
                    RabbitMQConfig.FILE_ROUTING_KEY,
                    Map.of("fileMd5", fileMd5, "userId", userId),
                    correlationData);
            log.info("文件解析向量化任务提交成功，fileMd5：{}", fileMd5);
        } catch (Exception e) {
            log.error("文件解析向量化任务提交失败，fileMd5: {}", fileMd5, e);
            // 发送失败可记录到本地日志，后续通过对账机制补全
            throw new RuntimeException("消息发送失败", e);
        }

    }

    /**
     * 初始化 RabbitMQ 发布确认回调（可选，确保消息到达队列）
     */
    @Autowired
    public void initRabbitTemplate() {
        // 消息发布确认回调（确认消息是否到达交换机）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                if (correlationData != null) {
                    log.info("消息发布到交换机成功，correlationId: {}", correlationData.getId());
                } else {
                    log.info("消息发布到交换机成功，但 correlationId 为空");
                }
            } else {
                if (correlationData != null) {
                    log.error("消息发布到交换机失败，correlationId: {}, cause: {}", correlationData.getId(), cause);
                } else {
                    log.error("消息发布到交换机失败，但 correlationId 为空");
                }
            }
        });

        // 消息返回回调（交换机路由到队列失败时触发）
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息路由到队列失败，message: {}, routingKey: {}, reason: {}",
                    returned.getMessage(), returned.getRoutingKey(), returned.getReplyText());
        });
    }
}