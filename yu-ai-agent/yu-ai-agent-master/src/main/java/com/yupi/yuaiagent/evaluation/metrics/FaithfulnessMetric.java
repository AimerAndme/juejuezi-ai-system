package com.yupi.yuaiagent.evaluation.metrics;

import com.yupi.yuaiagent.evaluation.EvaluationMetric;
import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class FaithfulnessMetric implements EvaluationMetric {
    
    @Override
    public String getName() {
        return "Faithfulness";
    }
    
    @Override
    public double calculate(EvaluationTestCase testCase) {
        String answer = testCase.getRetrievedAnswer();
        List<String> contexts = testCase.getRetrievedDocuments();
        
        if (answer == null || answer.isEmpty()) {
            return 0.0;
        }
        
        if (contexts == null || contexts.isEmpty()) {
            return 0.0;
        }
        
        double score = 0.0;
        String contextText = String.join(" ", contexts);
        
        String[] sentences = answer.split("[.!?。！？]");
        int totalSentences = sentences.length;
        int supportedSentences = 0;
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) {
                continue;
            }
            
            if (isSentenceSupported(sentence, contextText)) {
                supportedSentences++;
            }
        }
        
        if (totalSentences > 0) {
            score = (double) supportedSentences / totalSentences;
        }
        
        return score;
    }
    
    private boolean isSentenceSupported(String sentence, String context) {
        String[] keywords = sentence.split("\\s+");
        int matchedKeywords = 0;
        
        for (String keyword : keywords) {
            if (keyword.length() > 2 && context.toLowerCase().contains(keyword.toLowerCase())) {
                matchedKeywords++;
            }
        }
        
        return matchedKeywords >= Math.max(1, keywords.length / 3);
    }
    
    @Override
    public String getDescription() {
        return "生成内容是否基于检索到的文档（忠实度）";
    }
}
