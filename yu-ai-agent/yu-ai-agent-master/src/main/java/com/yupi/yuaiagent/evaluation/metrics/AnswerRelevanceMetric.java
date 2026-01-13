package com.yupi.yuaiagent.evaluation.metrics;

import com.yupi.yuaiagent.evaluation.EvaluationMetric;
import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AnswerRelevanceMetric implements EvaluationMetric {
    
    @Override
    public String getName() {
        return "AnswerRelevance";
    }
    
    @Override
    public double calculate(EvaluationTestCase testCase) {
        String question = testCase.getQuestion();
        String answer = testCase.getRetrievedAnswer();
        
        if (question == null || question.isEmpty()) {
            return 0.0;
        }
        
        if (answer == null || answer.isEmpty()) {
            return 0.0;
        }
        
        double score = calculateRelevanceScore(question, answer);
        
        return score;
    }
    
    private double calculateRelevanceScore(String question, String answer) {
        String[] questionKeywords = extractKeywords(question);
        String[] answerKeywords = extractKeywords(answer);
        
        int matchedKeywords = 0;
        for (String qKeyword : questionKeywords) {
            for (String aKeyword : answerKeywords) {
                if (qKeyword.equalsIgnoreCase(aKeyword) || 
                    aKeyword.toLowerCase().contains(qKeyword.toLowerCase())) {
                    matchedKeywords++;
                    break;
                }
            }
        }
        
        return questionKeywords.length > 0 ? 
            (double) matchedKeywords / questionKeywords.length : 0.0;
    }
    
    private String[] extractKeywords(String text) {
        return text.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5\\s]", "")
            .split("\\s+");
    }
    
    @Override
    public String getDescription() {
        return "答案是否回答了用户问题";
    }
}
