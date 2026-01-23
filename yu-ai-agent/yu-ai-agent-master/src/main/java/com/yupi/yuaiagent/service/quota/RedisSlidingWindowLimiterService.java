package com.yupi.yuaiagent.service.quota;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisSlidingWindowLimiterService {
    private static final String QUOTA_KEY_PREFIX = "quota:";
    private final RedisSlidingWindowLimiter redisSlidingWindowLimiter;

    public RedisSlidingWindowLimiterService(RedisSlidingWindowLimiter redisSlidingWindowLimiter) {
        this.redisSlidingWindowLimiter = redisSlidingWindowLimiter;
    }

    public LimitResult checkTokenQuota(String userId) {
        if (userId == null) {
            log.error("userId is null");
            throw new RuntimeException("userId is null");
        }
        String quotaKey = buildKey(userId);
        return redisSlidingWindowLimiter.tryConsume(quotaKey);
    }

    public LimitResult searchTokenQuota(String userId) {
        if (userId == null) {
            log.error("userId is null");
            throw new RuntimeException("userId is null");
        }
        String quotaKey = buildKey(userId);
        return redisSlidingWindowLimiter.searchConsume(quotaKey);
    }

    public LimitResult updateTokenQuota(String userId, int tokens) {
        if (userId == null) {
            log.error("userId is null");
            throw new RuntimeException("userId is null");
        }
        String key = buildKey(userId);
        return redisSlidingWindowLimiter.updateConsumption(key, tokens);
    }

    public void resetUserQuota(String userId) {
        if (userId == null) {
            log.error("userId is null");
            throw new RuntimeException("userId is null");
        }
        String quotaKey = buildKey(userId);
        redisSlidingWindowLimiter.reset(quotaKey);
    }

    @NotNull
    @Contract(pure = true)
    private String buildKey(String key) {
        return QUOTA_KEY_PREFIX + "userID:" + key + ":tokens";
    }
}
