package com.yupi.yuaiagent.domin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对应 PG 中的 msg_type_enum
 */
@Getter
@AllArgsConstructor
public enum MsgTypeEnum {
    TEXT("text", "文本"),
    IMAGE("image", "图片"),
    FILE("file", "文件"),
    VOICE("voice", "语音");

    private final String code;
    private final String desc;

    public static MsgTypeEnum fromCode(String code) {
        for (MsgTypeEnum enumVal : values()) {
            if (enumVal.code.equals(code)) {
                return enumVal;
            }
        }
        return null;
    }
}