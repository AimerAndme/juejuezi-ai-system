package com.yupi.yuaiagent.service.cache;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void setCache(String key, Object value, long time) {
        try {
            redisTemplate.opsForValue().set(key, value, time);
            log.debug("缓存设置成功：{}", key);
        } catch (Exception e) {
            log.error("缓存设置失败：{}", key, e);
        }
    }

    public Object getCache(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("缓存命中：{}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("缓存获取异常，尝试删除缓存：{}", key, e);
            try {
                deleteCache(key);
            } catch (Exception deleteException) {
                log.error("删除缓存失败：{}", key, deleteException);
            }
            return null;
        }
    }

    public void deleteCache(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("缓存删除：{}，结果：{}", key, result);
        } catch (Exception e) {
            log.error("缓存删除失败：{}", key, e);
        }
    }

    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return result != null && result;
        } catch (Exception e) {
            log.error("检查缓存存在性失败：{}", key, e);
            return false;
        }
    }

    public void clearAllCache() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.info("清空所有缓存成功");
        } catch (Exception e) {
            log.error("清空所有缓存失败", e);
        }
    }
}
