package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import com.yupi.yuaiagent.utils.ExecutionTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class RagQueryNode implements NodeAction {
    private final ChatClient ragChatClient;

    public RagQueryNode(ChatClient ragChatClient) {
        this.ragChatClient = ragChatClient;

    }

    @Override
    @ExecutionTimeMonitor
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info(" ragChatClient 开始执行");
        UserChatVO queryInfo = (UserChatVO) state.value("queryInfo").get();
        if (queryInfo.getQuery() == null) {
            log.error("无法获取用户输入内容");
        }
        PromptTemplate promptTemplate = new PromptTemplate("""
                "对于下面的问题，你需要先执行一个信息检索步骤。请识别出回答该问题所需的关键信息点，
                并设想你正在从一个大型文档集合中查找包含这些信息点的段落。
                获取到相关段落后，请综合这些信息，给出一个详尽且准确的回答。
                你的回答应完全基于检索到的内容。问题：{queryInfo}"
                """);
        promptTemplate.add("queryInfo", queryInfo.getQuery());
        String content = ExecutionTimeUtils.monitorExecutionTime("ragChatClient", () -> {
            return ragChatClient
                    .prompt(promptTemplate.render())
                    .user(queryInfo.getQuery())
                    .advisors(spec -> spec.param("chat_memory_conversation_id", queryInfo.getConversationId()))
                    .call().content();
        });
        if (content == null) {
            log.error(" ragChatClient 输出结果为空");
            return Map.of("chatResult", "");
        }
        log.info(" ragChatClient 输出");
        return Map.of("chatResult", content);
    }
}
