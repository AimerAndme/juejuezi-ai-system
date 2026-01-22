package com.yupi.yuaiagent.service.cache;

import com.yupi.yuaiagent.utils.JsonUtils;
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
            String json = JsonUtils.toJson(value);
            json = JsonUtils.cleanInvalidJsonChars(json);
            redisTemplate.opsForValue().set(key, json, time);
            log.debug("缓存设置成功：{}", key);
        } catch (Exception e) {
            log.error("缓存设置失败：{}", key, e);
        }
    }

    public Object getCache(String key, Class<?> valueType) {
        try {
            Object value = JsonUtils.fromJson((String) redisTemplate.opsForValue().get(key), valueType);
            if (value != null) {
                log.debug("缓存命中：{}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("缓存获取异常：{}", key, e.getCause());
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
