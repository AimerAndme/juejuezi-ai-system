package com.yupi.yuaiagent.domin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data


public class UserChatVO {
    private String UserId;
    /**
     * 对话唯一标识（UUID）
     */
    private String conversationId;
    private String query;
}
