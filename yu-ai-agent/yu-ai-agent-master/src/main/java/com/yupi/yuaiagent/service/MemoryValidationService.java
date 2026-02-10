package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.MemoryFragment;
import com.yupi.yuaiagent.exception.MemoryDataValidationException;
import com.yupi.yuaiagent.exception.MemoryFormatException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Set;

@Service
public class MemoryValidationService {

    private static final Set<Integer> VALID_MESSAGE_TYPES = Set.of(0, 1, 2);

    public void validateMemoryFragment(MemoryFragment fragment) {
        if (fragment == null) {
            throw new MemoryDataValidationException("fragment", null, "记忆片段不能为空");
        }

        validateMemoryId(fragment.getMemoryId());
        validateUserId(fragment.getUserId());
        validateSessionId(fragment.getSessionId());
        validateContent(fragment.getContent());
        validateMessageType(fragment.getMessageType());
        validateExtraMeta(fragment.getExtraMeta());
    }

    private void validateMemoryId(String memoryId) {
        if (!StringUtils.hasText(memoryId)) {
            throw new MemoryDataValidationException("memoryId", memoryId, "记忆ID不能为空");
        }
    }

    private void validateUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new MemoryDataValidationException("userId", userId, "用户ID不能为空");
        }
    }

    private void validateSessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new MemoryDataValidationException("sessionId", sessionId, "会话ID不能为空");
        }
    }

    private void validateContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new MemoryDataValidationException("content", content, "记忆内容不能为空");
        }

        try {
            Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException e) {
            throw new MemoryFormatException("content", "Base64编码", content, "记忆内容必须是有效的Base64编码");
        }
    }

    private void validateMessageType(Integer messageType) {
        if (messageType == null) {
            throw new MemoryDataValidationException("messageType", messageType, "消息类型不能为空");
        }

        if (!VALID_MESSAGE_TYPES.contains(messageType)) {
            throw new MemoryDataValidationException("messageType", messageType,
                    String.format("消息类型[%d]不合法，必须是0(system)、1(user)或2(assistant)", messageType));
        }
    }

    private void validateExtraMeta(Object extraMeta) {
        if (extraMeta == null) {
            return;
        }

        try {
            com.yupi.yuaiagent.utils.JsonUtils.toJson(extraMeta);
        } catch (Exception e) {
            throw new MemoryDataValidationException("extraMeta", extraMeta, "额外元数据无法序列化为JSON");
        }
    }
}
