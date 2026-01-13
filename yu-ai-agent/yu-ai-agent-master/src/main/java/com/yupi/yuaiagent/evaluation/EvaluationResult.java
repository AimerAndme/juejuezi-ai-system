package com.yupi.yuaiagent.evaluation;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class EvaluationResult {

    private String evaluationId;
    private long timestamp;
    private int totalTestCases;
    private Map<String, Double> metricScores;
    private Map<String, Double> metricAverages;
    private String summary;
    private Map<String, Object> metadata;

    public EvaluationResult() {
        this.metricScores = new HashMap<>();
        this.metricAverages = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    public void addMetricScore(String metricName, double score) {
        metricScores.put(metricName, score);
    }

    public void calculateAverage(String metricName, double total, int count) {
        metricAverages.put(metricName, total / count);
    }
}
