package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import com.yupi.yuaiagent.util.factory.NormalChatPromptFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class NormalChatNode implements NodeAction {
    //    private static final String PROMPT_PATH = "classpath:prompt/对话端闲聊提示词(工友).txt";
    private final ChatClient memoryChatClient;
    private String PROMPT_STRING;

    public NormalChatNode(ChatClient memoryChatClient) {
        this.memoryChatClient = memoryChatClient;
    }

    @Override
    @ExecutionTimeMonitor
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info(" memoryChatClient 闲聊开始执行");
        if (state.value("queryInfo").isEmpty() || state.value("reWriteQuery").isEmpty()) {
            log.error("无法获取用户query");
        }
        UserChatVO queryInfo = (UserChatVO) state.value("queryInfo").get();
        String userRole = queryInfo.getUserRole();
        if (userRole.isBlank()) {
            log.error("无法获取用户角色");
        }
        //获取系统提示词
        String systemPrompt = NormalChatPromptFactory.getPrompt(userRole);
        String query = (String) state.value("reWriteQuery").get();
        PromptTemplate promptTemplate = new PromptTemplate(systemPrompt + "  用户输入的闲聊内容：{query}");
        promptTemplate.add("query", query);
        String content = memoryChatClient
                .prompt(promptTemplate.render())
                .user(query)
                .advisors(spec -> spec.params(
                        Map.of("chat_memory_conversation_id", queryInfo.getConversationId(),
                                "nodeId", "NormalChatNode")
                ))
                .call()
                .content();
        return Map.of("chatResult", content);
    }

//    // 3. 初始化方法：Bean构造完成后执行资源加载
//    @PostConstruct
//    public void initPromptContent() { try {
//        PROMPT_STRING = TxtReader.readTxtToString(PROMPT_PATH);
//    } catch (Exception e) {
//        // 4. 抛运行时异常：阻止Bean初始化失败，便于排查（Spring会打印完整栈）
//        throw new IllegalStateException("闲聊节点提示词加载失败！路径：" + PROMPT_PATH, e);
//    }
//
//    }
}
