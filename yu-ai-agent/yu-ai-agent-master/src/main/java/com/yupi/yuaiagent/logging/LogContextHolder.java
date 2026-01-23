package com.yupi.yuaiagent.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * 节点日志上下文持有者(线程隔离)
 */
public class LogContextHolder {
    private final static ThreadLocal<Map<String, NodeExecutionLog>> threadLocalLogContext = ThreadLocal.withInitial(HashMap::new);

    public static void addNodeLog(String nodeId, NodeExecutionLog log) {
        threadLocalLogContext.get().put(nodeId, log);
    }

    public static NodeExecutionLog getNodeLog(String nodeId) {
        return threadLocalLogContext.get().get(nodeId);
    }

    public static Map<String, NodeExecutionLog> getAllNodeLogs() {
        return threadLocalLogContext.get();
    }

    public static int getTotalTokens() {
        int total = 0;
        Map<String, NodeExecutionLog> allNodeLogs = getAllNodeLogs();
        for (String key : allNodeLogs.keySet()) {
            total += allNodeLogs.get(key).getTokenUsed();
        }
        return total;
    }

    public static void clear() {
        threadLocalLogContext.remove();
    }
}
