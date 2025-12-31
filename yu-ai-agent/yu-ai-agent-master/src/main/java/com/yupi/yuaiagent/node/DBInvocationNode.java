package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class DBInvocationNode implements NodeAction {
    private final ChatClient dbToolChatClient;

    public DBInvocationNode(@Qualifier("DbToolChatClient") ChatClient dbToolChatClient) {
        this.dbToolChatClient = dbToolChatClient;
    }


    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info(" DBInvocationNode 节点开始执行");
        Optional<Object> nl2SqlResult = state.value("nl2SqlResult");
        if (nl2SqlResult.isEmpty()) {
            log.error("无法获取NL2SQL结果");
            return Map.of();
        }
        String sql = (String) nl2SqlResult.get();
        PromptTemplate promptTemplate = new PromptTemplate("""
                请将SQL语句转换为数据库查询语句，调用数据库操作的Tools，并返回调用意图与结果的整体内容。
                sql语句为：{sql}
                """);
        promptTemplate.add("sql", sql);
        String content = dbToolChatClient.prompt(promptTemplate.render()).call().content();
        if (content == null) {
            log.info("DBInvocationNode:bd工具调用结果为空！");
            return Map.of("dbInvocationResult", "工具调用失败！");
        }
        return Map.of("dbInvocationResult", content);
    }
}
