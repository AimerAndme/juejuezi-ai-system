package com.yupi.yuaiagent.evaluation.llm;

import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LLMJudgeEvaluator {
    
    @Autowired
    private ChatClient chatClient;
    
    private static final String FAITHFULNESS_PROMPT = """
        你是一个专业的RAG系统评估专家。请评估生成的答案是否忠实于检索到的上下文。
        
        评分标准（0-1分）：
        - 1.0：答案完全基于检索到的上下文，没有幻觉
        - 0.7：答案主要基于上下文，有少量合理推断
        - 0.4：答案部分基于上下文，包含一些未在上下文中出现的信息
        - 0.0：答案与上下文无关或包含大量幻觉
        
        请只返回一个0到1之间的数字分数，不要返回其他内容。
        
        用户问题：%s
        检索到的上下文：%s
        生成的答案：%s
        """;
    
    private static final String RELEVANCE_PROMPT = """
        你是一个专业的问答系统评估专家。请评估生成的答案是否相关且准确地回答了用户问题。
        
        评分标准（0-1分）：
        - 1.0：答案完全回答了问题，准确且全面
        - 0.7：答案基本回答了问题，但不够全面或有小错误
        - 0.4：答案部分相关，但偏离了问题核心
        - 0.0：答案与问题无关或完全错误
        
        请只返回一个0到1之间的数字分数，不要返回其他内容。
        
        用户问题：%s
        生成的答案：%s
        """;
    
    public double evaluateFaithfulness(String question, String answer, List<String> contexts) {
        try {
            String contextText = String.join("\n\n", contexts);
            String prompt = String.format(FAITHFULNESS_PROMPT, question, contextText, answer);
            
            String response = chatClient.prompt()
                .messages(new SystemMessage("你是一个专业的RAG系统评估专家。"))
                .messages(new UserMessage(prompt))
                .call()
                .content();
            
            double score = parseScore(response);
            log.info("LLM评估忠实度: {:.4f}", score);
            return score;
            
        } catch (Exception e) {
            log.error("LLM评估忠实度失败", e);
            return 0.0;
        }
    }
    
    public double evaluateRelevance(String question, String answer) {
        try {
            String prompt = String.format(RELEVANCE_PROMPT, question, answer);
            
            String response = chatClient.prompt()
                .messages(new SystemMessage("你是一个专业的问答系统评估专家。"))
                .messages(new UserMessage(prompt))
                .call()
                .content();
            
            double score = parseScore(response);
            log.info("LLM评估相关性: {:.4f}", score);
            return score;
            
        } catch (Exception e) {
            log.error("LLM评估相关性失败", e);
            return 0.0;
        }
    }
    
    public double evaluateContextPrecision(String question, List<String> contexts) {
        try {
            String contextText = String.join("\n\n", contexts);
            String prompt = String.format("""
                你是一个专业的RAG系统评估专家。请评估检索到的上下文是否包含回答用户问题所需的信息。
                
                评分标准（0-1分）：
                - 1.0：上下文完全包含回答问题所需的所有信息
                - 0.7：上下文包含大部分必要信息
                - 0.4：上下文包含部分相关信息
                - 0.0：上下文与问题无关
                
                请只返回一个0到1之间的数字分数，不要返回其他内容。
                
                用户问题：%s
                检索到的上下文：%s
                """, question, contextText);
            
            String response = chatClient.prompt()
                .messages(new SystemMessage("你是一个专业的RAG系统评估专家。"))
                .messages(new UserMessage(prompt))
                .call()
                .content();
            
            double score = parseScore(response);
            log.info("LLM评估上下文精度: {:.4f}", score);
            return score;
            
        } catch (Exception e) {
            log.error("LLM评估上下文精度失败", e);
            return 0.0;
        }
    }
    
    public void evaluateTestCase(EvaluationTestCase testCase) {
        if (testCase.getRetrievedAnswer() != null && testCase.getRetrievedDocuments() != null) {
            double faithfulness = evaluateFaithfulness(
                testCase.getQuestion(),
                testCase.getRetrievedAnswer(),
                testCase.getRetrievedDocuments()
            );
            
            double relevance = evaluateRelevance(
                testCase.getQuestion(),
                testCase.getRetrievedAnswer()
            );
            
            double contextPrecision = evaluateContextPrecision(
                testCase.getQuestion(),
                testCase.getRetrievedDocuments()
            );
            
            testCase.setEvaluationDetails(String.format(
                "LLM评估 - 忠实度: %.4f, 相关性: %.4f, 上下文精度: %.4f",
                faithfulness, relevance, contextPrecision
            ));
        }
    }
    
    private double parseScore(String response) {
        try {
            String cleaned = response.trim().replaceAll("[^0-9.]", "");
            if (!cleaned.isEmpty()) {
                double score = Double.parseDouble(cleaned);
                return Math.max(0.0, Math.min(1.0, score));
            }
        } catch (NumberFormatException e) {
            log.warn("无法解析LLM返回的分数: {}", response);
        }
        return 0.5;
    }
}
