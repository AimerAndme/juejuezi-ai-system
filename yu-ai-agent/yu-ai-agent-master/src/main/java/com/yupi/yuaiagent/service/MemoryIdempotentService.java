package com.yupi.yuaiagent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 幂等性校验：基于 Redis 实现（记忆ID作为key，避免重复处理）
 */
@Service
public class MemoryIdempotentService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 幂等性key前缀
    private static final String IDEMPOTENT_KEY_PREFIX = "agent:memory:idempotent:";
    // 过期时间：24小时（避免Redis堆积）
    private static final long EXPIRE_TIME = 86400;

    /**
     * 检查并标记记忆ID（已存在返回false，不存在返回true并标记）
     */
    public boolean checkAndMark(String memoryId) {
        String key = IDEMPOTENT_KEY_PREFIX + memoryId;
        // Redis SETNX：不存在则设置，存在则忽略（原子操作）
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", EXPIRE_TIME, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }
}