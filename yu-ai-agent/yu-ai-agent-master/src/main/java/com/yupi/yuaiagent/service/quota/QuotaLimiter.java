package com.yupi.yuaiagent.service.quota;

public interface QuotaLimiter {
    LimitResult tryConsume(String key);

    LimitResult tryConsume(String key, long tokens);

    LimitResult tryConsume(String key, long tokens, long windowsMs, long maxTokens);

    LimitResult tryConsume(String key, long tokens, long windowsMs, long maxTokens, int thresholdPercent);

    LimitResult searchConsume(String key, long windowsMs, long maxTokens);

    LimitResult searchConsume(String key);
    LimitResult updateConsumption(String key, long tokens);

    LimitResult updateConsumption(String key, long tokens, long windowsMs, long maxTokens, int thresholdPercent);

    long getRemainingTokens(String key);

    void reset(String key);
}
