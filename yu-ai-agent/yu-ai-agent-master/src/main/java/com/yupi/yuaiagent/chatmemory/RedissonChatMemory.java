package com.yupi.yuaiagent.chatmemory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson 的会话记忆实现
 */
public class RedissonChatMemory implements ChatMemory {

    // Redis 键前缀（如 "mine:chat:history:"）
    private final String keyPrefix;

    // 单会话最大消息数
    private final int maxMessages;

    // 会话过期时间（秒）
    private final long expireSeconds;

    // Redisson 客户端
    private final RedissonClient redissonClient;

    // JSON 序列化工具（处理 Message 对象）
    private final ObjectMapper objectMapper;

    public RedissonChatMemory(String keyPrefix, int maxMessages, long expireSeconds, RedissonClient redissonClient) {
        Assert.hasText(keyPrefix, "keyPrefix 不能为空");
        Assert.isTrue(maxMessages > 0, "maxMessages 必须大于0");
        Assert.isTrue(expireSeconds > 0, "expireSeconds 必须大于0");
        Assert.notNull(redissonClient, "redissonClient 不能为空");

        this.keyPrefix = keyPrefix;
        this.maxMessages = maxMessages;
        this.expireSeconds = expireSeconds;
        this.redissonClient = redissonClient;
        this.objectMapper = new ObjectMapper(); // 可自定义序列化配置
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
// 启用多态类型支持
        this.objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    // 生成 Redis 键（prefix + sessionId）
    private String getRedisKey(String sessionId) {
        return keyPrefix + sessionId;
    }

    /**
     * 添加消息到会话历史
     */
    @Override
    public void add(String sessionId, Message message) {
        Assert.hasText(sessionId, "sessionId 不能为空");
        Assert.notNull(message, "message 不能为空");

        try {
            // 1. 获取分布式列表（RList），键为 "prefix:sessionId"
            RList<String> messageList = redissonClient.getList(getRedisKey(sessionId));

            // 2. 序列化消息为 JSON 字符串
            String messageJson = objectMapper.writeValueAsString(message);

            // 3. 添加消息到列表头部（最新消息在前）
            messageList.add(0, messageJson); // 等同于 LPUSH

            // 4. 截断列表，只保留最近 maxMessages 条消息
            if (messageList.size() > maxMessages) {
                messageList.trim(0, maxMessages - 1); // 保留前 maxMessages 条
            }

            // 5. 设置过期时间（若未设置过，或更新过期时间）
            if (messageList.remainTimeToLive() <= 0) {
                messageList.expire(expireSeconds, TimeUnit.SECONDS);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("消息序列化失败", e);
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {

    }

    /**
     * 获取会话历史消息
     */
    @Override
    public List<Message> get(String sessionId) {
        Assert.hasText(sessionId, "sessionId 不能为空");

        // 1. 获取分布式列表
        RList<String> messageList = redissonClient.getList(getRedisKey(sessionId));
        if (messageList.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 反序列化为 Message 对象（按时间正序排列）
        List<Message> messages = new ArrayList<>(messageList.size());
        for (String messageJson : messageList) { // RList 迭代顺序为存储顺序（头部是最新消息）
            try {
                Message message = objectMapper.readValue(messageJson, UserMessage.class);
                messages.add(message);
            } catch (Exception e) {
                throw new RuntimeException("消息反序列化失败", e);
            }
        }

        // 3. 反转列表，将最早的消息放在前面（时间正序）
        java.util.Collections.reverse(messages);
        return messages;
    }

    /**
     * 清空会话历史
     */
    @Override
    public void clear(String sessionId) {
        Assert.hasText(sessionId, "sessionId 不能为空");
        redissonClient.getList(getRedisKey(sessionId)).delete();
    }
}