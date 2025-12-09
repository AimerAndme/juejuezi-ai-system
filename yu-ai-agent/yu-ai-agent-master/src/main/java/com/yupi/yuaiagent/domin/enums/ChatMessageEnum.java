package com.yupi.yuaiagent.domin.enums;

import lombok.Getter;

/**
 * 定义消息类型的枚举类
 */
@Getter
public enum ChatMessageEnum {
    ASSISTANT(2, "ASSISTANT"),
    USER(1, "USER"),
    SYSTEM(0, "SYSTEm");
    private final int code;     // 目标数字（核心）
    private final String bizKey; // 业务标识（如接口传入的字符串参数）

    // 构造器
    ChatMessageEnum(int code, String bizKey) {
        this.code = code;
        this.bizKey = bizKey;
    }
    // ------------------------------ 反向调用核心方法 ------------------------------

    /**
     * 反向调用：传入数字 code，返回对应的枚举常量（严格匹配）
     * 特点：无效数字（如 3、-1）直接抛异常，避免隐藏错误（推荐生产环境用）
     */
    public static ChatMessageEnum getByCode(int code) {
        // 遍历枚举常量（仅 3 个，性能极高）
        for (ChatMessageEnum type : ChatMessageEnum.values()) {
            if (type.code == code) {
                return type; // 匹配成功，返回枚举
            }
        }
        // 匹配失败，抛异常（明确错误原因）
        throw new IllegalArgumentException("无效的 BizType 数字：" + code + "，合法值：0/1/2");
    }

    // ------------------------------ 以下是「传入参数获取 code」的核心方法 ------------------------------

    /**
     * 场景 1：传入枚举名称（字符串），获取对应的 code（大小写不敏感）
     * 示例：传入 "CORE" → 0，传入 "non_core"（不支持，枚举名称是 NON_CORE）→ 抛异常
     */
    public static int getCodeByName(String enumName) {
        if (enumName == null || enumName.isEmpty()) {
            throw new IllegalArgumentException("枚举名称不可为空");
        }
        // 遍历匹配枚举名称（忽略大小写）
        for (ChatMessageEnum type : ChatMessageEnum.values()) {
            if (type.name().equalsIgnoreCase(enumName)) {
                return type.getCode();
            }
        }
        throw new IllegalArgumentException("无效的枚举名称：" + enumName);
    }

    /**
     * 场景 2：传入业务标识（bizKey），获取对应的 code（大小写敏感，可按需修改）
     * 示例：传入 "core" → 0，传入 "non_core" → 1，传入 "TEMP" → 抛异常（bizKey 是 "temp"）
     */
    public static int getCodeByBizKey(String bizKey) {
        if (bizKey == null || bizKey.isEmpty()) {
            throw new IllegalArgumentException("业务标识不可为空");
        }
        // 遍历匹配业务标识（按需改为 equalsIgnoreCase 忽略大小写）
        for (ChatMessageEnum type : ChatMessageEnum.values()) {
            if (type.getBizKey().equals(bizKey)) {
                return type.getCode();
            }
        }
        throw new IllegalArgumentException("无效的业务标识：" + bizKey);
    }
}
