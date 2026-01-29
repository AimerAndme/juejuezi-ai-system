package com.yupi.yuaiagent.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String key, int expireTime) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", expireTime, TimeUnit.SECONDS));
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
    }
}
