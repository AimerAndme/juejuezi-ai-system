package com.yupi.yuaiagent.config;

import com.yupi.yuaiagent.chatmemory.RedissonChatMemory;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonChatConfig {

    // 注册基于 Redisson 的 ChatMemory
    @Bean
    public RedissonChatMemory redissonChatMemory(@Autowired RedissonClient redissonClient) {
        return new RedissonChatMemory(
                "mine:chat:history:redisson:",  // 键前缀（矿山系统业务标识）
                20,                             // 单会话最大20条消息
                7 * 24 * 3600,                  // 过期时间7天
                redissonClient
        );
    }

}