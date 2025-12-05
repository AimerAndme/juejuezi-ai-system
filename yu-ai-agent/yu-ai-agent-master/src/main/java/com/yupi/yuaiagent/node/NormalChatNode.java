package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.yupi.yuaiagent.utils.TxtReader;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class NormalChatNode implements NodeAction {
    private static final String PROMPT_PATH = "classpath:prompt/对话端闲聊提示词.txt";
    private final ChatClient memoryChatClient;
    private String PROMPT_STRING;

    public NormalChatNode(ChatClient memoryChatClient) {
        this.memoryChatClient = memoryChatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        if (state.value("query").isEmpty()) {
            log.error("无法获取用户query");
        }
        Object query = state.value("query").get();
        PromptTemplate promptTemplate = new PromptTemplate(PROMPT_STRING + "  用户输入的闲聊内容：{query}");
        promptTemplate.add("query", query);
        String content = memoryChatClient.prompt(promptTemplate.render()).call().content();
        return Map.of("chatResult", content);
    }

    // 3. 初始化方法：Bean构造完成后执行资源加载
    @PostConstruct
    public void initPromptContent() {
        try {
            PROMPT_STRING = TxtReader.readTxtToString(PROMPT_PATH);
        } catch (Exception e) {
            // 4. 抛运行时异常：阻止Bean初始化失败，便于排查（Spring会打印完整栈）
            throw new IllegalStateException("闲聊节点提示词加载失败！路径：" + PROMPT_PATH, e);
        }
    }
}
