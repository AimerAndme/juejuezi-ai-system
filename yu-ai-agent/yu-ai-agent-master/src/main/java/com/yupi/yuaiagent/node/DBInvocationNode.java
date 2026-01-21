package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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
    @ExecutionTimeMonitor
    @Retryable(
            value = {
                    // 服务端错误，如500、502、503等，这些可能是临时性错误
                    org.springframework.web.client.HttpServerErrorException.class,
                    // 连接超时等网络异常
                    java.net.SocketTimeoutException.class,
                    // 其他运行时异常，但要小心，避免重试不应该重试的错误
                    RuntimeException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info(" DBInvocationNode 节点开始执行");
        Optional<Object> nl2SqlResult = state.value("nl2SqlResult");
        if (nl2SqlResult.isEmpty()) {
            log.error("无法获取NL2SQL结果");
            return Map.of();
        }
        String sql = (String) nl2SqlResult.get();
        PromptTemplate promptTemplate = new PromptTemplate("""
                请将SQL语句转换为数据库查询语句：
                1、首先调用字段元数据查询tools，查询数据库字段和对应的注释解释
                2、调用数据库操作的Tools，并返回调用意图与结果的整体内容。
                返回结果要求：最终生成结果需要包含字段、字段解释和对应的具体数据量！
                sql语句为：{sql}
                """);
        promptTemplate.add("sql", sql);
        String content = dbToolChatClient
                .prompt(promptTemplate.render())
                .advisors(spec -> spec.params(
                        Map.of(
                                "nodeId", "DBInvocationNode")
                ))
                .call().content();
        if (content == null) {
            log.info("DBInvocationNode:bd工具调用结果为空！");
            return Map.of("dbInvocationResult", "工具调用失败！");
        }
        return Map.of("dbInvocationResult", content);
    }
}
