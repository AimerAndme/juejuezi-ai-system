package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
意图识别节点，闲聊，专业知识获取rag，综合报表查询与导出
 */
@Slf4j
@Component
public class IntentRecognitionNode implements NodeAction {
    private static final int RETRY_TIME = 3;
    private static final long RETRY_INTERVAL_MS = 200;
    private final ChatClient chatClient;

    public IntentRecognitionNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        if (state.value("query").isEmpty()) {
            log.error("无法获取用户输入内容");
        }

        String query = (String) state.value("query").get();
        PromptTemplate promptTemplate = new PromptTemplate("""
                请分析用户输入内容的意图，从以下场景中精准匹配：
                1. 闲聊：日常无业务关联的对话（如问候、闲聊家常、无关话题调侃等）
                2. 矿山专业领域知识问答：关于矿山开采技术、设备原理、安全规范、地质勘探、矿山工程等专业知识的提问
                3. 公司规章制度问答：涉及公司考勤、奖惩、岗位职责、审批流程、福利待遇等制度相关的咨询
                4. 现场业务数据问答：关于矿山现场生产数据、设备运行数据、安全指标数据的查询/统计，需支持表单输出的需求
                
                要求：仅返回匹配场景的对应单词，注意：可能存在多场景的情况，多场景返回所有涉及场景，可选结果为：闲聊、矿山专业问答、公司制度问答、业务数据问答
                用户输入内容为：{query}
                """);
        promptTemplate.add("query", query);
        String result = recognizeContent(promptTemplate.render());
        if (result == null || result.isBlank()) {
            log.error("最终意图识别失败！返回原始查询query");
            return Map.of("recognizeResult", query);

        }
        return Map.of("recognizeResult", result.trim());
    }

    private String recognizeContent(String render) {
        int time = 1;
        while (time <= RETRY_TIME) {
            try {
                String content = chatClient.prompt(render).call().content();
                if (content != null && !content.isBlank()) {
                    return content;
                }
                log.info("意图识别成功！返回数据。");
            } catch (Exception e) {
                log.error("第{}次意图识别失败！", time);
            }
            time++;
            if (time <= RETRY_TIME) {
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException e) {
                    log.warn("重试间隔被中断", e);
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    break;
                }
            }
        }
        return null;
    }
}
