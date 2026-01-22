package com.yupi.yuaiagent.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消息队列错误处理配置
 */
@Configuration
public class ErrorMessageConfig {
    private final RabbitTemplate rabbitTemplate;

    public ErrorMessageConfig(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public MessageRecoverer messageRecoverer() {
        // 参数：rabbitTemplate, 目标交换机名称, 路由键
        return new RepublishMessageRecoverer(rabbitTemplate, RabbitMQConfig.MEMORY_DLQ_EXCHANGE, RabbitMQConfig.MEMORY_DLQ_ROUTING_KEY);
    }
}
