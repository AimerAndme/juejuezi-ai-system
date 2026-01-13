package com.yupi.yuaiagent.evaluation.metrics;

import com.yupi.yuaiagent.evaluation.EvaluationMetric;
import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ContextPrecisionMetric implements EvaluationMetric {
    
    @Override
    public String getName() {
        return "ContextPrecision";
    }
    
    @Override
    public double calculate(EvaluationTestCase testCase) {
        List<String> retrievedDocs = testCase.getRetrievedDocuments();
        List<String> relevantDocs = testCase.getRelevantDocuments();
        
        if (retrievedDocs == null || retrievedDocs.isEmpty()) {
            return 0.0;
        }
        
        if (relevantDocs == null || relevantDocs.isEmpty()) {
            return 0.0;
        }
        
        double precisionSum = 0.0;
        int relevantCount = 0;
        
        for (int i = 0; i < retrievedDocs.size(); i++) {
            String doc = retrievedDocs.get(i);
            if (relevantDocs.contains(doc)) {
                relevantCount++;
                precisionSum += (double) relevantCount / (i + 1);
            }
        }
        
        return precisionSum / retrievedDocs.size();
    }
    
    @Override
    public String getDescription() {
        return "检索到的文档是否包含答案（上下文精度）";
    }
}
