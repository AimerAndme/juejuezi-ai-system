package com.yupi.yuaiagent.domin.context;

import com.yupi.yuaiagent.domin.entity.RagRequestContextData;

/**
 * Rag 请求上下文
 */
public class RagRequestContext {
    // 使用 static final 保证全局唯一，同时避免内存泄漏
    private static final ThreadLocal<RagRequestContextData> contextHolder = new ThreadLocal<>();

    public static void set(RagRequestContextData data) {
        contextHolder.set(data);
    }

    public static RagRequestContextData get() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove(); // 关键！防止内存泄漏
    }
}