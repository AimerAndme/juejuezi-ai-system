package com.yupi.yuaiagent.domin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
// 上下文数据载体（可扩展）
public class RagRequestContextData {
    //用户 ID
    private String userId;
    //    会话 ID
    private String conversationId;
    // 问题
    private String query;
    // rag回答
    private String chatAnswer;
    // 意图识别结果
    private String referenceAnswer;
    // 检索结果
    private List<String> retrievedDocuments;
}