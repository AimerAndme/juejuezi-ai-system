package com.yupi.yuaiagent.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yupi.yuaiagent.utils.JsonUtils;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

/**
 * 自定义JSON序列化器，自动清理非法控制字符
 */
public class CleanGenericJackson2JsonRedisSerializer extends GenericJackson2JsonRedisSerializer {

    private final ObjectMapper objectMapper;

    public CleanGenericJackson2JsonRedisSerializer(ObjectMapper objectMapper) {
        super(objectMapper);
        this.objectMapper = objectMapper;
    }

    /**
     * 序列化：写入Redis前，清理对象转JSON后的非法字符
     */
    @Override
    public byte[] serialize(Object source) throws SerializationException {
        if (source == null) {
            return new byte[0];
        }
        try {
            // 1. 转JSON字符串（使用配置了enableDefaultTyping的objectMapper）
            String json = objectMapper.writeValueAsString(source);
            // 2. 清理非法字符
            String cleanJson = JsonUtils.cleanInvalidJsonChars(json);
            // 3. 转字节数组返回
            return cleanJson.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Could not serialize object to JSON", e);
        }
    }

    /**
     * 反序列化：从Redis读取后，先清理字节数组转字符串的非法字符，再解析
     */
    @Override
    public <T> T deserialize(byte[] source, Class<T> type) throws SerializationException {
        if (source == null || source.length == 0) {
            return null;
        }
        try {
            // 1. 字节数组转字符串
            String json = new String(source, StandardCharsets.UTF_8);
            // 2. 清理非法字符
            String cleanJson = JsonUtils.cleanInvalidJsonChars(json);
            // 3. 解析为指定类型
            return objectMapper.readValue(cleanJson, type);
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize JSON to type " + type, e);
        }
    }
}
