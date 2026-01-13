package com.yupi.yuaiagent.evaluation.metrics;

import com.yupi.yuaiagent.evaluation.EvaluationMetric;
import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RecallAtK implements EvaluationMetric {
    private final int k;
    
    public RecallAtK(int k) {
        this.k = k;
    }
    
    @Override
    public String getName() {
        return "Recall@" + k;
    }
    
    @Override
    public double calculate(EvaluationTestCase testCase) {
        List<String> retrievedDocs = testCase.getRetrievedDocuments();
        List<String> relevantDocs = testCase.getRelevantDocuments();
        List<String> retrievedContents = testCase.getRetrievedDocumentContents();
        
        if (relevantDocs == null || relevantDocs.isEmpty()) {
            return 0.0;
        }
        
        if (retrievedDocs == null || retrievedDocs.isEmpty()) {
            return 0.0;
        }
        
        int totalRelevant = relevantDocs.size();
        int topK = Math.min(k, retrievedDocs.size());
        
        List<String> matchedRelevantDocs = new java.util.ArrayList<>();
        
        for (int i = 0; i < topK; i++) {
            String retrievedContent = retrievedContents != null && i < retrievedContents.size() 
                    ? retrievedContents.get(i) 
                    : retrievedDocs.get(i);
            
            for (String relevantDoc : relevantDocs) {
                if (!matchedRelevantDocs.contains(relevantDoc) && isContentMatch(retrievedContent, relevantDoc)) {
                    matchedRelevantDocs.add(relevantDoc);
                    break;
                }
            }
        }
        
        return (double) matchedRelevantDocs.size() / totalRelevant;
    }
    
    private boolean isContentMatch(String content1, String content2) {
        if (content1 == null || content2 == null) {
            return false;
        }
        
        String normalized1 = content1.trim().toLowerCase();
        String normalized2 = content2.trim().toLowerCase();
        
        if (normalized1.equals(normalized2)) {
            return true;
        }
        
        if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public String getDescription() {
        return "前" + k + "个检索结果中覆盖了多少相关文档（基于内容匹配）";
    }
}
