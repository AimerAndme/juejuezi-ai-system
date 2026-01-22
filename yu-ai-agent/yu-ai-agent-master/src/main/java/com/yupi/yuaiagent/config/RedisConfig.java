package com.yupi.yuaiagent.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yupi.yuaiagent.service.cache.CleanGenericJackson2JsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public CleanGenericJackson2JsonRedisSerializer cleanGenericJackson2JsonRedisSerializer(ObjectMapper redisObjectMapper) {
        return new CleanGenericJackson2JsonRedisSerializer(redisObjectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, CleanGenericJackson2JsonRedisSerializer cleanGenericJackson2JsonRedisSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 1. 创建 ObjectMapper 并进行配置
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置可见性，让 Jackson 可以访问到对象的所有字段
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 启用默认的类型推断，这在反序列化时非常重要，可以正确识别对象类型
        // 配置 JSON 序列化器（支持存储对象/Map，同时兼容纯文本表情）
        objectMapper.registerModule(new JavaTimeModule()); // 支持 LocalDateTime
        objectMapper.registerModule(new SimpleModule()); // 兼容复杂类型
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL); // 避免反序列化类型丢失
        // 注意：新版本中推荐使用 LaissezFaireSubTypeValidator
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);

        // 2. 创建 Jackson2JsonRedisSerializer 时，直接将 ObjectMapper 通过构造函数传入
//        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer =
//                new GenericJackson2JsonRedisSerializer(objectMapper);

        // 3. 配置 RedisTemplate 的序列化器
        // key 使用 String 序列化器
        template.setKeySerializer(new StringRedisSerializer());
        // hash 的 key 也使用 String 序列化器
        template.setHashKeySerializer(new StringRedisSerializer());
        // value 使用 Jackson 序列化器
        template.setValueSerializer(cleanGenericJackson2JsonRedisSerializer);
        // hash 的 value 也使用 Jackson 序列化器
        template.setHashValueSerializer(cleanGenericJackson2JsonRedisSerializer);

        // 初始化 RedisTemplate
        template.afterPropertiesSet();

        return template;
    }
}
