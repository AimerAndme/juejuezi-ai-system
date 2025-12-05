package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent 对话消息实体类（对应 t_agent_chat_message 表及分表）
 */
@Data
public class AgentChatMessage {
    /** 消息ID（自增主键） */
    private Long id;

    /** 会话ID（关联会话表 t_agent_chat_session 的 session_id） */
    private String sessionId;

    /** 消息类型：1-用户输入，2-Agent回复，3-系统提示 */
    private Integer messageType; // PG SMALLINT 对应 Java Integer

    /** 非敏感内容明文存储 */
    private String content;

    /** 加密敏感内容（AES加密后存储） */
    private String encryptContent;

    /** 消息状态：1-发送成功，2-发送失败，3-已撤回 */
    private Integer messageStatus;

    /** Agent请求ID（关联调用日志） */
    private String requestId;

    /** 消息发送时间（PG自动填充，无需手动设置） */
    private LocalDateTime createTime;
}