package com.yupi.yuaiagent.domin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对应 PG 中的 sender_type_enum
 */
@Getter
@AllArgsConstructor
public enum SenderTypeEnum {
    USER("user", "用户"),
    AGENT("agent", "矿山Agent"),
    SYSTEM("system", "系统");

    private final String code; // 数据库存储值
    private final String desc; // 描述

    // 从数据库值反向解析枚举
    public static SenderTypeEnum fromCode(String code) {
        for (SenderTypeEnum enumVal : values()) {
            if (enumVal.code.equals(code)) {
                return enumVal;
            }
        }
        return null;
    }
}