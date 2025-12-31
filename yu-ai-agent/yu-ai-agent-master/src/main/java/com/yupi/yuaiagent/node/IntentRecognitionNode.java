package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
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
        log.info("意图识别开始执行");
        if (state.value("queryInfo").isEmpty()) {
            log.error("无法获取用户输入内容");
        }
        UserChatVO userChatVO = (UserChatVO) state.value("queryInfo").get();
        Object reWriteQuery = state.value("reWriteQuery").get();
        String query = userChatVO.getQuery();
        PromptTemplate promptTemplate = new PromptTemplate("""
                请分析用户输入内容的意图，从以下场景中精准匹配：
                1. 闲聊：日常无业务关联的对话（如问候、闲聊家常、无关话题调侃等），输出：chat
                2. 矿山专业领域知识问答：关于矿山开采技术、设备原理、安全规范、地质勘探、矿山工程等专业知识的提问，输出：ragChat
                3. 公司规章制度问答和软件技术类问答：涉及开发技术啊、公司考勤、奖惩、岗位职责、审批流程、福利待遇等制度相关的咨询，输出：ragChat
                4. 现场业务相关数据问答：
                                       1、关于矿山现场生产数据、设备运行数据、安全指标数据的查询/统计，需支持表单输出的需求，
                                       2、矿山基础信息、钻探信息、巷道工作面信息、储量统计信息、地表检测等
                                       输出：dbChat
                要求：仅返回匹配场景的对应单词，注意：可能存在多场景的情况，多场景返回所有涉及场景，可选结果依次为：chat、ragChat、ragChat、dbChat
                用户输入内容为：{query}
                """);
        promptTemplate.add("query", reWriteQuery);
        String result = recognizeContent(promptTemplate.render());
        if (result == null || result.isBlank()) {
            log.error("最终意图识别失败！返回原始查询query");
            return Map.of("recognizeResult", reWriteQuery);

        }
        return Map.of("recognizeResult", result.trim());
    }

    private String recognizeContent(String render) {
        int time = 1;
        while (time <= RETRY_TIME) {
            try {
                String content = chatClient.prompt(render).call().content();
                if (content != null && !content.isBlank()) {
                    log.info("意图识别成功！返回数据。{}", content);
                    return content;
                }
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
