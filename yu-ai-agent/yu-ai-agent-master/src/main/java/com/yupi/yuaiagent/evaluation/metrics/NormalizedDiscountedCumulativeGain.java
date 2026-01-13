package com.yupi.yuaiagent.evaluation.metrics;

import com.yupi.yuaiagent.evaluation.EvaluationMetric;
import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class NormalizedDiscountedCumulativeGain implements EvaluationMetric {
    private final int k;
    
    public NormalizedDiscountedCumulativeGain(int k) {
        this.k = k;
    }
    
    @Override
    public String getName() {
        return "NDCG@" + k;
    }
    
    @Override
    public double calculate(EvaluationTestCase testCase) {
        List<String> retrievedDocs = testCase.getRetrievedDocuments();
        List<String> relevantDocs = testCase.getRelevantDocuments();
        
        if (retrievedDocs == null || retrievedDocs.isEmpty()) {
            return 0.0;
        }
        
        int topK = Math.min(k, retrievedDocs.size());
        double dcg = calculateDCG(retrievedDocs.subList(0, topK), relevantDocs);
        double idcg = calculateIDCG(topK, relevantDocs.size());
        
        return idcg > 0 ? dcg / idcg : 0.0;
    }
    
    private double calculateDCG(List<String> retrievedDocs, List<String> relevantDocs) {
        double dcg = 0.0;
        for (int i = 0; i < retrievedDocs.size(); i++) {
            int relevance = relevantDocs.contains(retrievedDocs.get(i)) ? 1 : 0;
            dcg += relevance / (Math.log(i + 2) / Math.log(2));
        }
        return dcg;
    }
    
    private double calculateIDCG(int k, int totalRelevant) {
        double idcg = 0.0;
        int maxRelevant = Math.min(k, totalRelevant);
        for (int i = 0; i < maxRelevant; i++) {
            idcg += 1.0 / (Math.log(i + 2) / Math.log(2));
        }
        return idcg;
    }
    
    @Override
    public String getDescription() {
        return "考虑排序质量的归一化折扣累积增益";
    }
}
