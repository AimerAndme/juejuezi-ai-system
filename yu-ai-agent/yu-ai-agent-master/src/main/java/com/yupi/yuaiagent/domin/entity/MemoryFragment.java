package com.yupi.yuaiagent.domin.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 记忆片段实体（非核心记忆，用于 RabbitMQ 消息传输）
 */
@Data
public class MemoryFragment implements Serializable {
    private static final long serialVersionUID = 1L;
    private String memoryId; // 唯一ID（幂等性校验用：user_id+session_id+msg_id+fragment_idx）
    private String userId; // 关联用户
    private String sessionId; // 会话ID
    private String content; // 记忆内容（对话片段/摘要）
    private Float relevanceScore; // 相关性评分（＜0.8的非核心记忆）
    private Integer messageType;//消息类型,0:system,1:user,2:assistant
    private List<String> entities; // 非核心实体（如普通名词）
    private LocalDateTime createTime; // 创建时间
    private Map<String, Object> extraMeta; // 额外元数据（如场景标识）
}