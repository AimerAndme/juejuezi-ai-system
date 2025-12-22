package com.yupi.yuaiagent.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列配置：
 * 1. 主队列：存储非核心记忆异步写入任务
 * 2. 死信队列：处理消费失败的消息（重试3次后进入）
 */
@Configuration
public class RabbitMQConfig {

    // 队列名称（可配置到 application.yml）
    public static final String MEMORY_ASYNC_QUEUE = "agent.memory.async.queue";
    public static final String FILE_ASYNC_QUEUE = "agent.file.async.queue";
    public static final String MEMORY_DLQ_QUEUE = "agent.memory.dlq.queue"; // 死信队列
    public static final String MEMORY_EXCHANGE = "agent.memory.exchange";
    public static final String FILE_EXCHANGE = "agent.file.exchange";
    public static final String MEMORY_ROUTING_KEY = "agent.memory.routing.key";
    public static final String FILE_ROUTING_KEY = "agent.file.routing.key";
    public static final String MEMORY_DLQ_EXCHANGE = "agent.memory.dlq.exchange";
    public static final String MEMORY_DLQ_ROUTING_KEY = "agent.memory.dlq.routing.key";

    /**
     * 死信交换机（路由失败/过期消息到死信队列）
     */
    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(MEMORY_DLQ_EXCHANGE, true, false);
    }

    /**
     * 死信队列（持久化）
     */
    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(MEMORY_DLQ_QUEUE)
                .build();
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue())
                .to(dlqExchange())
                .with(MEMORY_DLQ_ROUTING_KEY);
    }

    /**
     * 主交换机（非核心记忆异步任务）
     */
    @Bean
    public DirectExchange memoryExchange() {
        return new DirectExchange(MEMORY_EXCHANGE, true, false);
    }

    /**
     * 文件交换机
     */
    @Bean
    public DirectExchange fileExchange() {
        return new DirectExchange(FILE_EXCHANGE, true, false);
    }

    /**
     * 主队列（持久化+绑定死信交换机）
     */
    @Bean
    public Queue memoryAsyncQueue() {
        return QueueBuilder.durable(MEMORY_ASYNC_QUEUE)
                // 绑定死信交换机（消费失败重试后进入）
                .withArgument("x-dead-letter-exchange", MEMORY_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MEMORY_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 60000) // 消息过期时间：60秒（未消费则进入死信）
                .withArgument("x-max-length", 10000) // 队列最大长度：1万条（避免堆积过多）
                .build();
    }

    /**
     * 文件处理队列
     */
    @Bean
    public Queue fileAsyncQueue() {
        return QueueBuilder.durable(FILE_ASYNC_QUEUE)
                // 绑定死信交换机（消费失败重试后进入）
                .withArgument("x-dead-letter-exchange", MEMORY_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MEMORY_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 60000) // 消息过期时间：60秒（未消费则进入死信）
                .withArgument("x-max-length", 10000) // 队列最大长度：1万条（避免堆积过多）
                .build();
    }

    /**
     * 主队列绑定
     */
    @Bean
    public Binding memoryBinding() {
        return BindingBuilder.bind(memoryAsyncQueue())
                .to(memoryExchange())
                .with(MEMORY_ROUTING_KEY);
    }

    /**
     * 文件处理主队列绑定
     */
    @Bean
    public Binding fileBinding() {
        return BindingBuilder.bind(fileAsyncQueue())
                .to(fileExchange())
                .with(FILE_ROUTING_KEY);
    }

    /**
     * 消息序列化器（Jackson 序列化 MemoryFragment 对象）
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 批量消费容器工厂（提升处理效率）
     * 配置：积累10条消息或1秒内无新消息，触发批量消费
     */
    @Bean
    public SimpleRabbitListenerContainerFactory batchRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setBatchListener(true); // 开启批量监听
        factory.setBatchSize(10); // 批量大小：10条/批
        factory.setBatchReceiveTimeout(1000L); // 超时时间：1秒
        factory.setMessageConverter(jsonMessageConverter()); // 序列化器
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 手动ACK（确保消息处理完成后再确认）
        return factory;
    }
}