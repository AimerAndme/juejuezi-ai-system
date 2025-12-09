package com.yupi.yuaiagent.domin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对应 PG 中的 conversation_status_enum
 */
@Getter
@AllArgsConstructor
public enum ConversationStatusEnum {
    ONGOING("ongoing", "进行中"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String desc;

    public static ConversationStatusEnum fromCode(String code) {
        for (ConversationStatusEnum enumVal : values()) {
            if (enumVal.code.equals(code)) {
                return enumVal;
            }
        }
        return null;
    }
}