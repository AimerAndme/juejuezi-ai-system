package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MiningAgentConversation {

    /**
     * 对话唯一标识（UUID）
     */
    private String conversationId;

    /**
     * 发起对话的用户ID
     */
    private String userId;

    /**
     * 对话主题（如“掘进机故障排查”）
     */
    private String topic;

    /**
     * 对话状态（整数：0-进行中, 1-已结束, 2-已归档）
     */
    private Integer status;

    /**
     * 对话开始时间
     */
    private LocalDateTime startTime;

    /**
     * 对话结束时间（未结束为空）
     */
    private LocalDateTime endTime;

    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMsgTime;

    /**
     * 软删除标识（false-正常，true-删除）
     */
    private Boolean isDeleted;
}
