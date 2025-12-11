package com.yupi.yuaiagent.domin.vo;

import lombok.Data;

@Data


public class UserChatVO {
    private String UserId;
    /**
     * 对话唯一标识（UUID）
     */
    private String conversationId;
    private String UserRole;
    private String query;
}
