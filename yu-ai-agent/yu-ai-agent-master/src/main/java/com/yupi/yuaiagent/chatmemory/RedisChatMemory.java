package com.yupi.yuaiagent.chatmemory;

import com.yupi.yuaiagent.domin.entity.MemoryFragment;
import com.yupi.yuaiagent.domin.entity.MiningAgentConversation;
import com.yupi.yuaiagent.domin.entity.MiningAgentConversationMsg;
import com.yupi.yuaiagent.domin.enums.ChatMessageEnum;
import com.yupi.yuaiagent.mapper.MiningAgentConversationMapper;
import com.yupi.yuaiagent.mapper.MiningAgentConversationMsgMapper;
import com.yupi.yuaiagent.service.MemoryAsyncProducer;
import com.yupi.yuaiagent.util.UniqueIdGenerator;
import com.yupi.yuaiagent.utils.JsonUtils;
import com.yupi.yuaiagent.utils.MessageSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的对话记忆存储实现
 */
@Service
@Slf4j
public class RedisChatMemory implements ChatMemory {
    // Redis 键前缀，避免键冲突
    private static final String KEY_PREFIX = "chat:memory:";
    private static final Integer LIMIT_MESSAGES = 20;//包含问与答
    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    MemoryAsyncProducer memoryAsyncProducer;
    @Autowired
    MiningAgentConversationMapper miningAgentConversationMapper;
    @Autowired
    MiningAgentConversationMsgMapper miningAgentConversationMsgMapper;

    public RedisChatMemory(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 添加单条消息到对话历史
     *
     * @param userID  对话 ID
     * @param message 消息对象
     */
    @Override
    public void add(String userID, Message message) {
        add(userID, List.of(message));
    }

    /**
     * 添加多条消息到对话历史
     *
     * @param conversationId 对话 ID
     * @param messages       消息列表
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        // 获取现有消息列表
        List<Message> existingMessages = getFromRedis(conversationId);
        // 合并消息
        existingMessages.addAll(messages);
        // 保存更新后的消息列表
        setToRedis(conversationId, existingMessages);
        // MQ异步存储到数据库
        MiningAgentConversation conversation = miningAgentConversationMapper.selectById(conversationId);
        if (conversation == null) {
            log.error("会话id：{}，当前会话不存在！", conversationId);
        }
        //将所有message封装并 分发给mq处理
        for (Message message : messages) {
            MemoryFragment memoryFragment = new MemoryFragment();
            String memoryId = UniqueIdGenerator.generateMemoryId(conversationId);
            memoryFragment.setMemoryId(memoryId);
            memoryFragment.setSessionId(conversationId);
            memoryFragment.setUserId(conversation.getUserId());
            String json = JsonUtils.toJson(message.getMetadata());
            memoryFragment.setExtraMeta(message.getMetadata());
            String serialize = MessageSerializer.serialize(message);
            memoryFragment.setContent(serialize);
            int codeByBizKey = ChatMessageEnum.getCodeByBizKey(message.getMessageType().name());
            memoryFragment.setMessageType(codeByBizKey);//参入当前消息的类型
            memoryAsyncProducer.sendMemoryFragment(memoryFragment);
        }

        log.debug("已向对话 [{}] 添加 {} 条消息，当前总消息数: {}",
                conversationId, messages.size(), Math.min(existingMessages.size(), 20));
    }

    /**
     * 获取对话的最近 N 条消息
     *
     * @param conversationId 对话 ID
     * @return 消息列表
     */
    @Override
    public List<Message> get(String conversationId) {
        List<Message> allMessages = getFromRedis(conversationId);
        return allMessages.stream()
                .toList();
    }

    /**
     * 清空对话历史
     *
     * @param conversationId 对话 ID
     */
    @Override
    public void clear(String conversationId) {
        String key = getRedisKey(conversationId);
        redisTemplate.delete(key);
        log.debug("已清空对话 [{}] 的历史消息", conversationId);
    }

    /**
     * 清理对话历史，只保留最新的20条消息
     *
     * @param conversationId 对话 ID
     */
    public void trimConversation(String conversationId) {
        List<Message> allMessages = getFromRedis(conversationId);
        // 检查消息数量，如果超过limit条则删除多余部分，只保留最新的limit条
        if (allMessages.size() > LIMIT_MESSAGES) {
            List<Message> recentMessages = allMessages.subList(allMessages.size() - LIMIT_MESSAGES, allMessages.size());
            setToRedis(conversationId, recentMessages);
            log.debug("已清理对话 [{}] 的历史消息，从 {} 条减少到 {} 条", conversationId, allMessages.size(), LIMIT_MESSAGES);
        }
    }

    /**
     * 获取所有对话ID
     *
     * @return 对话ID集合
     */
    public List<String> getAllConversationIds() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null) {
            // 移除前缀，只返回conversationId
            List<String> conversationIds = new ArrayList<>();
            for (String key : keys) {
                conversationIds.add(key.substring(KEY_PREFIX.length()));
            }
            return conversationIds;
        }
        return new ArrayList<>();
    }

    /**
     * 从 Redis 获取消息列表
     */
    @SuppressWarnings("unchecked")
    private List<Message> getFromRedis(String conversationId) {
        String key = getRedisKey(conversationId);
        Object value = redisTemplate.opsForValue().get(key);
        // 处理空值或类型不匹配的情况
        if (value == null) {
            log.info("当前对话redis无缓存内容，开始重建");
            //如果redis为空
            //1、数据库拉取数据进行重建
            List<MiningAgentConversationMsg> msgList = miningAgentConversationMsgMapper.selectByConversationIdOnLimit(conversationId, LIMIT_MESSAGES);
            if (msgList == null || msgList.isEmpty()) {
                log.info("redis、数据库均为对话记录，返回空数组");
                //2、若数据库也为空，直接返回空数组
                return new ArrayList<>();
            }
            //组装消息
            //TODO待测试！！！
            List<Message> list = new ArrayList<>();
            for (MiningAgentConversationMsg conversationMsg : msgList) {
                UserMessage userMessage = UserMessage.builder()
                        .media(new ArrayList<>())
                        .text(conversationMsg.getMsgContent())
                        .metadata(conversationMsg.getFileMeta() == null ? Map.of() : JsonUtils.jsonToMap(conversationMsg.getFileMeta())).build();
                list.add(userMessage);
            }
            return list;
        }
        if (!(value instanceof List)) {
            log.error("对话 [{}] 的消息存储格式不正确，预期为 List，实际为: {}",
                    conversationId, value.getClass().getName());
            return new ArrayList<>();
        }
        List<String> serializedMessages = new ArrayList<>();
        for (Object item : (List<?>) value) {
            if (item instanceof String) {
                serializedMessages.add((String) item);
            } else {
                log.warn("对话 [{}] 中发现非字符串类型的消息，跳过: {}",
                        conversationId, item.getClass().getName());
            }
        }
        List<Message> messages = new ArrayList<>(serializedMessages.size());
        for (String serialized : serializedMessages) {
            try {
                Message message = MessageSerializer.deserialize(serialized);
                messages.add(message);
            } catch (Exception e) {
                log.error("反序列化消息失败，跳过该消息: {}", serialized, e);
            }
        }
        return messages;
    }

    /**
     * 将消息列表存入 Redis
     */
    private void setToRedis(String conversationId, List<Message> messages) {
        String key = getRedisKey(conversationId);
        if (messages.size() > LIMIT_MESSAGES) {
            messages = messages.subList(messages.size() - LIMIT_MESSAGES, messages.size());
            log.debug("已清理对话 [{}] 的历史消息，从 {} 条减少到 {} 条", conversationId, messages.size(), LIMIT_MESSAGES);
        }
        List<String> serializedMessages = new ArrayList<>(messages.size());
        for (Message message : messages) {
            try {
                String serialized = MessageSerializer.serialize(message);
                serializedMessages.add(serialized);
            } catch (Exception e) {
                log.error("序列化消息失败，跳过该消息: {}", message, e);
            }
        }
        redisTemplate.opsForValue().set(key, serializedMessages, 1L, TimeUnit.DAYS);
    }

    /**
     * 生成带前缀的 Redis 键
     */
    private String getRedisKey(String conversationId) {
        return KEY_PREFIX + conversationId;
    }
}



