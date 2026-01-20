package com.yupi.yuaiagent.logging;

import com.yupi.yuaiagent.utils.JsonUtils;

import java.util.Map;

public class StructuredLogBuilder {
    public static String buildJsonLog(Map<String, NodeExecutionLog> logs) {
        return JsonUtils.toJson(logs);
    }

    public static String buildJsonLog(NodeExecutionLog log) {
        return JsonUtils.toJson(log);
    }
}