package com.yupi.yuaiagent.service.quota;

import com.yupi.yuaiagent.utils.TxtReader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RedisSlidingWindowLimiter implements QuotaLimiter {

    private static String SLIDING_WINDOW_LUA;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> luaScriptRedisTemplate;
    private DefaultRedisScript<List> slidingWindowScript;

    public RedisSlidingWindowLimiter(RedisTemplate<String, Object> redisTemplate, @Qualifier("luaScriptRedisTemplate") RedisTemplate<String, Object> luaScriptRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.luaScriptRedisTemplate = luaScriptRedisTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            log.info("初始化限流脚本");
            SLIDING_WINDOW_LUA = TxtReader.readTxtToString("classpath:lua/quota.lua");
            slidingWindowScript = new DefaultRedisScript<>();
            slidingWindowScript.setScriptText(SLIDING_WINDOW_LUA);
            slidingWindowScript.setResultType(List.class);
        } catch (Exception e) {
            log.error("无法读取限流脚本");
        }
    }

    @Override
    public LimitResult tryConsume(String key) {
        log.info("tryConsume: {}", key);
        return tryConsume(key, 0, 60000, 10000, 90);
    }

    @Override
    public LimitResult tryConsume(String key, long tokens) {
        return tryConsume(key, tokens, 60000, 10000, 90);
    }

    @Override
    public LimitResult tryConsume(String key, long tokens, long windowsMs, long maxTokens) {
        return tryConsume(key, tokens, windowsMs, maxTokens, 90);
    }

    @Override
    public LimitResult tryConsume(String key, long tokens, long windowsMs, long maxTokens, int thresholdPercent) {
        if (key == null) {
            log.error("key is null");
            throw new IllegalStateException("key is null");
        }
        long now = System.currentTimeMillis();
        try {
            log.info("tryConsume: {}, {}, {}, {}, {}", key, tokens, windowsMs, maxTokens, thresholdPercent);
            List<Long> execute = luaScriptRedisTemplate.execute(
                    slidingWindowScript,
                    List.of(key),
                    String.valueOf(now),
                    String.valueOf(windowsMs),
                    String.valueOf(maxTokens),
                    String.valueOf(tokens),
                    String.valueOf(thresholdPercent),
                    "1");
            if (execute == null) {
                log.error("限流脚本执行结果为空");
                throw new RuntimeException("限流脚本执行结果为空");
            }
            boolean allowed = execute.get(0) == 1;
            long remaining = execute.get(1);
            long resetTime = execute.get(2);
            int usagePercent = execute.get(3).intValue();
            int code = execute.get(4).intValue();
            LimitResult.LimitReason limitReason = LimitResult.LimitReason.fromCode(code);
            return new LimitResult(allowed, remaining, resetTime, now - windowsMs, now + windowsMs, usagePercent, limitReason);
        } catch (Exception e) {
            log.error("限流脚本执行失败", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public LimitResult searchConsume(String key, long windowsMs, long maxTokens) {
        if (key == null) {
            log.error("key is null");
            throw new IllegalStateException("key is null");
        }
        long now = System.currentTimeMillis();
        try {
            log.info("searchConsume: {}, {}, {}", key, windowsMs, maxTokens);
            List<Long> execute = luaScriptRedisTemplate.execute(
                    slidingWindowScript,
                    List.of(key),
                    String.valueOf(now),
                    String.valueOf(windowsMs),
                    String.valueOf(maxTokens),
                    "0",
                    "0",
                    "2");
            if (execute == null) {
                log.error("限流脚本执行结果为空");
                throw new RuntimeException("限流脚本执行结果为空");
            }
            boolean allowed = execute.get(0) == 1;
            long remaining = execute.get(1);
            long resetTime = execute.get(2);
            int usagePercent = execute.get(3).intValue();
            int code = execute.get(4).intValue();
            LimitResult.LimitReason limitReason = LimitResult.LimitReason.fromCode(code);
            return new LimitResult(allowed, remaining, resetTime, now - windowsMs, now + windowsMs, usagePercent, limitReason);
        } catch (Exception e) {
            log.error("限流脚本执行失败", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public LimitResult searchConsume(String key) {
        return searchConsume(key, 60000, 10000);
    }

    @Override
    public LimitResult updateConsumption(String key, long tokens) {
        return updateConsumption(key, tokens, 60000, 10000, 90);
    }

    @Override
    public LimitResult updateConsumption(String key, long tokens, long windowsMs, long maxTokens, int thresholdPercent) {
        if (key == null) {
            log.error("key is null");
            throw new IllegalStateException("key is null");
        }
        long now = System.currentTimeMillis();
        try {
            log.info("tryConsume: {}, {}, {}, {}, {}", key, tokens, windowsMs, maxTokens, thresholdPercent);
            List<Long> execute = luaScriptRedisTemplate.execute(
                    slidingWindowScript,
                    List.of(key),
                    String.valueOf(now),
                    String.valueOf(windowsMs),
                    String.valueOf(maxTokens),
                    String.valueOf(tokens),
                    String.valueOf(thresholdPercent),
                    "0"
            );
            if (execute == null) {
                log.error("限流脚本执行结果为空");
                throw new RuntimeException("限流脚本执行结果为空");
            }
            boolean allowed = execute.get(0) == 1;
            long remaining = execute.get(1);
            long resetTime = execute.get(2);
            int usagePercent = execute.get(3).intValue();
            int code = execute.get(4).intValue();
            LimitResult.LimitReason limitReason = LimitResult.LimitReason.fromCode(code);
            return new LimitResult(allowed, remaining, resetTime, now - windowsMs, now + windowsMs, usagePercent, limitReason);
        } catch (Exception e) {
            log.error("限流脚本执行失败", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getRemainingTokens(String key) {
        if (key == null) {
            log.error("key is null");
            throw new IllegalStateException("key is null");
        }
        log.info("getRemainingTokens: {}", key);
        return tryConsume(key, 0, 0, 0, 0).getRemainingTokens();
    }

    @Override
    public void reset(String key) {
        redisTemplate.delete(key);
        log.info("reset: {}", key);
    }
}
