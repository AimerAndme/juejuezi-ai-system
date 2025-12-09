package com.yupi.yuaiagent.domin.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class MiningAgentConversationMsg {

    /**
     * 消息唯一标识（UUID）
     */
    private String msgId;

    /**
     * 关联对话ID
     */
    private String conversationId;

    /**
     * 发送方类型（整数：0-user, 1-agent, 2-system）
     */
    private Integer senderType;

    /**
     * 发送方ID（用户ID/agent/system）
     */
    private String senderId;

    /**
     * 消息类型（整数：0-system, 1-user, 2-assistant）
     */
    private Integer msgType;

    /**
     * 文本消息内容/非文本消息URL
     */
    private String msgContent;

    /**
     * 文件元信息（JSON格式，非文件消息为空）
     */
    private Map<String, Object> fileMeta;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 软删除标识（false-正常，true-删除）
     */
    private Boolean isDeleted;
}
