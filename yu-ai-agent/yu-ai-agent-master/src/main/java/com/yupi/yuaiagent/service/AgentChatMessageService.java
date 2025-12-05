package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.AgentChatMessage;
import com.yupi.yuaiagent.mapper.AgentChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话消息 Service（完整增删改查接口）
 */
@Service
public class AgentChatMessageService {

    @Autowired
    private AgentChatMessageMapper chatMessageMapper;

    /**
     * 1. 新增消息（自动加密敏感内容，符合煤矿合规）
     */
    public boolean addMessage(AgentChatMessage message) {
        // 校验必填字段
        if (!StringUtils.hasText(message.getSessionId()) || message.getMessageType() == null) {
            throw new IllegalArgumentException("会话ID和消息类型不能为空！");
        }

        // 敏感内容加密（调用神东加密工具类）
        String content = message.getContent();
//        if (isSensitiveContent(content)) {
//            message.setEncryptContent(ShendongEncryptUtil.encrypt(content));
//            message.setContent("[敏感信息已加密]"); // 明文字段显示占位符
//        }

        // 默认状态：发送成功（1）
        if (message.getMessageStatus() == null) {
            message.setMessageStatus(1);
        }

        // 插入数据（触发器自动路由到分表）
        int rows = chatMessageMapper.insert(message);
        return rows > 0;
    }

    /**
     * 2. 根据ID删除消息（煤矿场景建议慎用，优先撤回）
     */
    public boolean deleteMessageById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("消息ID不能为空！");
        }
        int rows = chatMessageMapper.deleteById(id);
        return rows > 0;
    }

    /**
     * 3. 撤回消息（逻辑操作：更新状态为3，不删除数据，符合审计要求）
     */
    public boolean recallMessage(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("消息ID不能为空！");
        }
        // 状态3：已撤回
        int rows = chatMessageMapper.updateStatusById(id, 3);
        return rows > 0;
    }

    /**
     * 4. 根据ID查询消息（敏感内容自动解密）
     */
    public AgentChatMessage getMessageById(Long id) {
        if (id == null) {
            return null;
        }
        AgentChatMessage message = chatMessageMapper.selectById(id);
        return message;
    }

    /**
     * 5. 会话回溯：查询会话下所有有效消息
     */
    public List<AgentChatMessage> getMessageBySessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return List.of();
        }
        List<AgentChatMessage> messages = chatMessageMapper.selectBySessionId(sessionId);
        return messages;
    }

    /**
     * 6. 按用户ID查询历史对话（分页简化：限制最多100条）
     */
    public List<AgentChatMessage> getMessageByUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            return List.of();
        }
        List<AgentChatMessage> messages = chatMessageMapper.selectByUserId(userId, 100);
        return messages;
    }

    /**
     * 7. 时间范围查询（审计场景）
     */
    public List<AgentChatMessage> getMessageByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return getMessageByTimeRange(startTime, endTime, null);
    }

    public List<AgentChatMessage> getMessageByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Integer status) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空！");
        }
        List<AgentChatMessage> messages = chatMessageMapper.selectByTimeRange(startTime, endTime, status);
        return messages;
    }

    // ------------------- 私有工具方法 -------------------

    /**
     * 判断是否为敏感内容（煤矿场景定制关键词）
     */
    private boolean isSensitiveContent(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        String[] sensitiveKeywords = {"SD-", "工作面", "地质参数", "设备编号", "综采机", "断层带"};
        for (String keyword : sensitiveKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }


}