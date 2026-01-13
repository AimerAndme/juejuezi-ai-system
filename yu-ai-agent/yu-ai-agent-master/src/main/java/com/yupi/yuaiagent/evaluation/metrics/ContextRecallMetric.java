package com.yupi.yuaiagent.evaluation.metrics;

import com.yupi.yuaiagent.evaluation.EvaluationMetric;
import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ContextRecallMetric implements EvaluationMetric {
    
    @Override
    public String getName() {
        return "ContextRecall";
    }
    
    @Override
    public double calculate(EvaluationTestCase testCase) {
        List<String> retrievedDocs = testCase.getRetrievedDocuments();
        List<String> relevantDocs = testCase.getRelevantDocuments();
        
        if (relevantDocs == null || relevantDocs.isEmpty()) {
            return 0.0;
        }
        
        if (retrievedDocs == null || retrievedDocs.isEmpty()) {
            return 0.0;
        }
        
        int retrievedRelevantCount = 0;
        for (String doc : retrievedDocs) {
            if (relevantDocs.contains(doc)) {
                retrievedRelevantCount++;
            }
        }
        
        return (double) retrievedRelevantCount / relevantDocs.size();
    }
    
    @Override
    public String getDescription() {
        return "检索到的文档是否覆盖了所有必要信息（上下文召回）";
    }
}
