package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class DBResult2NlNode implements NodeAction {
    private final ChatClient memoryChatClient;

    public DBResult2NlNode(ChatClient memoryChatClient) {
        this.memoryChatClient = memoryChatClient;
    }


    @Override
    @ExecutionTimeMonitor
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info(" DBResult2NlNode 节点开始执行");
        UserChatVO queryInfo = (UserChatVO) state.value("queryInfo").get();
        if (queryInfo.getQuery() == null) {
            log.error("无法获取用户输入内容");
        }
        Optional<Object> dbResult = state.value("dbInvocationResult");
        if (dbResult.isEmpty()) {
            log.error("无法获取数据库查询结果");
            return Map.of();
        }
        String result = (String) dbResult.get();
        log.info("DBResult2NlNode: 数据库查询结果为：{}", result);
        PromptTemplate promptTemplate = new PromptTemplate("""
                # 角色 (Role)
                你是一个智能矿山辅助决策助手，负责根据数据库查询结果，向用户生成清晰、准确且友好的回复。
                
                # 技能 (Skills)
                - 准确理解 JSON 格式的数据库查询结果。
                - 将结构化数据转换为自然语言。
                - 根据用户原始问题，提炼关键信息进行回答。
                - 识别并过滤掉不必要或敏感的内部字段。
                - 优雅地处理查询无结果或错误的情况。
                
                # 行为规范 (Constraints)
                - **语言风格**: 使用礼貌、清晰、易懂的自然语言。
                - **数据处理**:
                    - 仅使用 JSON 结果中与用户问题直接相关的、非敏感的信息。
                    - 忽略数据库内部字段（如 `id`, `created_at`, `updated_at`, `internal_status` 等，除非用户明确询问）。
                    - 将代码化的值（如 `status: "A"`）借助字段对应的注释，转换为可读的描述（如 "状态: 已激活"）。
                    - 对数字（如金额、数量）进行适当的格式化（如添加货币符号、千分位分隔符）。
                - **无结果处理**: 如果 JSON 结果为空数组 `[]` 或表示无数据，应告知用户 "根据您的条件，未找到相关信息。"
                - **错误处理**: 如果 JSON 结果包含错误信息，不要直接暴露技术细节，应告知用户 "抱歉，查询时遇到问题，请稍后再试。" 或类似安抚性语言。
                - **信息提炼**: 仅提供用户所需信息，避免罗列所有字段。将信息组织成易于理解的句子或列表。
                - **上下文**: 回复应紧密围绕用户的原始问题展开。
                
                # 输入 (Input)
                - 用户的原始问题。
                - 数据库工具返回的 JSON 格式结果。
                
                # 任务 (Task)
                根据用户的原始问题和接收到的 JSON 格式数据库查询结果，生成一个最终的、自然语言的回复发送给用户。
                
                # 输出格式 (Output Format)
                - 直接输出给用户的自然语言回复，无需额外说明。
                - 如果有多个结果，可以使用列表或分点描述。
                - 确保回复内容准确，与 JSON 数据一致。
                
                # 注意 (Note)
                请严格按照以上规范，将接收到的 JSON 数据处理成用户友好的回复。
                数据库查询结果为：{dbResult}
                """);
        promptTemplate.add("dbResult", result);
        String content = memoryChatClient
                .prompt(promptTemplate.render())
                .user(queryInfo.getQuery())
                .advisors(spec -> spec.param("chat_memory_conversation_id", queryInfo.getConversationId()))
                .call().content();
        if (content == null) {
            log.info("DBResult2NlNode: 智能客服助手无结果！");
            return Map.of("dbChatResult", "无结果！");
        }
        return Map.of("chatResult", content);
    }
}
