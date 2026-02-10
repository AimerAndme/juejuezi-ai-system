package com.yupi.yuaiagent.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yupi.yuaiagent.model.CachedSearchResult;

import java.util.HashMap;
import java.util.Map;

public class RedisSerializerTest {
    public static void main(String[] args) {
        // 创建与 Redis 配置相同的 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new SimpleModule());
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY);

        // 创建测试对象
        CachedSearchResult testResult = new CachedSearchResult();
        Map<String, String> docVersions = new HashMap<>();
        docVersions.put("doc1", "v1");
        testResult.setDocVersions(docVersions);

        try {
            // 序列化对象
            String json = objectMapper.writeValueAsString(testResult);
            System.out.println("序列化结果:");
            System.out.println(json);

            // 反序列化
            Object deserialized = objectMapper.readValue(json, Object.class);
            System.out.println("\n反序列化结果类型:");
            System.out.println(deserialized.getClass().getName());
            System.out.println("反序列化结果:");
            System.out.println(deserialized);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}