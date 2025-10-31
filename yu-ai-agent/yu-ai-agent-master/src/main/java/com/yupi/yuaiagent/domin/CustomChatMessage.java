package com.yupi.yuaiagent.domin;

import lombok.Data;

import java.time.LocalDateTime;
@Data
// 自定义消息类：仅包含业务必需字段
public class CustomChatMessage {
    // 角色：用户/AI/系统（用简单枚举，避免依赖Spring AI的MessageType）
    public enum Role { USER, ASSISTANT, SYSTEM }

    private Role role;         // 角色（自定义枚举，简单可控）
    private String content;    // 消息内容（字符串）
    private LocalDateTime time; // 时间戳（JDK基础类型）

    // 构造函数、getter、setter
    public CustomChatMessage(Role role, String content) {
        this.role = role;
        this.content = content;
        this.time = LocalDateTime.now(); // 自动生成时间戳
    }

    // getter和setter省略...
}