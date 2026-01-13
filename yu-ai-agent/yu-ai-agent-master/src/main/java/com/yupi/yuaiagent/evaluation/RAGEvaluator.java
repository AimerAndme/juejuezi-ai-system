package com.yupi.yuaiagent.evaluation;

import com.yupi.yuaiagent.evaluation.metrics.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RAGEvaluator {
    
    private final List<EvaluationMetric> retrievalMetrics;
    private final List<EvaluationMetric> generationMetrics;
    
    public RAGEvaluator() {
        this.retrievalMetrics = new ArrayList<>();
        this.generationMetrics = new ArrayList<>();
        
        initializeDefaultMetrics();
    }
    
    private void initializeDefaultMetrics() {
        retrievalMetrics.add(new PrecisionAtK(3));
        retrievalMetrics.add(new PrecisionAtK(5));
        retrievalMetrics.add(new RecallAtK(3));
        retrievalMetrics.add(new RecallAtK(5));
        retrievalMetrics.add(new MeanReciprocalRank());
        retrievalMetrics.add(new NormalizedDiscountedCumulativeGain(5));
        
        generationMetrics.add(new FaithfulnessMetric());
        generationMetrics.add(new AnswerRelevanceMetric());
        generationMetrics.add(new ContextPrecisionMetric());
        generationMetrics.add(new ContextRecallMetric());
    }
    
    public EvaluationResult evaluate(List<EvaluationTestCase> testCases) {
        EvaluationResult result = new EvaluationResult();
        result.setEvaluationId("eval_" + System.currentTimeMillis());
        result.setTimestamp(System.currentTimeMillis());
        result.setTotalTestCases(testCases.size());
        
        log.info("开始RAG评估，测试用例数量: {}", testCases.size());
        
        evaluateRetrievalMetrics(testCases, result);
        evaluateGenerationMetrics(testCases, result);
        
        generateSummary(result);
        
        log.info("RAG评估完成: {}", result.getSummary());
        
        return result;
    }
    
    private void evaluateRetrievalMetrics(List<EvaluationTestCase> testCases, EvaluationResult result) {
        log.info("评估检索质量指标...");
        
        for (EvaluationMetric metric : retrievalMetrics) {
            double totalScore = 0.0;
            int validCases = 0;
            
            for (EvaluationTestCase testCase : testCases) {
                if (testCase.getRetrievedDocuments() != null && 
                    testCase.getRelevantDocuments() != null) {
                    double score = metric.calculate(testCase);
                    totalScore += score;
                    validCases++;
                }
            }
            
            if (validCases > 0) {
                double averageScore = totalScore / validCases;
                result.addMetricScore(metric.getName(), averageScore);
                result.calculateAverage(metric.getName(), totalScore, validCases);
                
                log.info("{}: {:.4f}", metric.getName(), averageScore);
            }
        }
    }
    
    private void evaluateGenerationMetrics(List<EvaluationTestCase> testCases, EvaluationResult result) {
        log.info("评估生成质量指标...");
        
        for (EvaluationMetric metric : generationMetrics) {
            double totalScore = 0.0;
            int validCases = 0;
            
            for (EvaluationTestCase testCase : testCases) {
                if (testCase.getRetrievedAnswer() != null && 
                    testCase.getRetrievedDocuments() != null) {
                    double score = metric.calculate(testCase);
                    totalScore += score;
                    validCases++;
                }
            }
            
            if (validCases > 0) {
                double averageScore = totalScore / validCases;
                result.addMetricScore(metric.getName(), averageScore);
                result.calculateAverage(metric.getName(), totalScore, validCases);
                
                log.info("{}: {:.4f}", metric.getName(), averageScore);
            }
        }
    }
    
    private void generateSummary(EvaluationResult result) {
        StringBuilder summary = new StringBuilder();
        summary.append("RAG评估报告\n");
        summary.append("==========\n");
        summary.append(String.format("测试用例数: %d\n", result.getTotalTestCases()));
        summary.append("评估时间: ").append(new java.util.Date(result.getTimestamp())).append("\n\n");
        
        summary.append("检索质量指标:\n");
        for (EvaluationMetric metric : retrievalMetrics) {
            Double score = result.getMetricAverages().get(metric.getName());
            if (score != null) {
                summary.append(String.format("  %s: %.4f\n", metric.getName(), score));
            }
        }
        
        summary.append("\n生成质量指标:\n");
        for (EvaluationMetric metric : generationMetrics) {
            Double score = result.getMetricAverages().get(metric.getName());
            if (score != null) {
                summary.append(String.format("  %s: %.4f\n", metric.getName(), score));
            }
        }
        
        result.setSummary(summary.toString());
    }
    
    public void addRetrievalMetric(EvaluationMetric metric) {
        retrievalMetrics.add(metric);
    }
    
    public void addGenerationMetric(EvaluationMetric metric) {
        generationMetrics.add(metric);
    }
    
    public List<EvaluationMetric> getRetrievalMetrics() {
        return new ArrayList<>(retrievalMetrics);
    }
    
    public List<EvaluationMetric> getGenerationMetrics() {
        return new ArrayList<>(generationMetrics);
    }
}
