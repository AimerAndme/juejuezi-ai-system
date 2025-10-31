package com.yupi.yuaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;

public class TimeTool {
    @Tool(description = "今天的日期")
    public LocalDateTime todayDate() {
        return LocalDateTime.now();
    }
}
