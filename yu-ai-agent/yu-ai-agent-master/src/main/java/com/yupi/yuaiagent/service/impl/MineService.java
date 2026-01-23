package com.yupi.yuaiagent.service.impl;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.yupi.yuaiagent.domin.context.RagRequestContext;
import com.yupi.yuaiagent.domin.entity.MiningAgentConversation;
import com.yupi.yuaiagent.domin.entity.RagRequestContextData;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import com.yupi.yuaiagent.graph.PreProcessingGraphFactory;
import com.yupi.yuaiagent.logging.LogContextHolder;
import com.yupi.yuaiagent.logging.NodeExecutionLog;
import com.yupi.yuaiagent.logging.StructuredLogBuilder;
import com.yupi.yuaiagent.mapper.MiningAgentConversationMapper;
import com.yupi.yuaiagent.service.quota.RedisSlidingWindowLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class MineService implements com.yupi.yuaiagent.service.IMineService {
    private final PreProcessingGraphFactory preProcessingGraphFactory;
    private final MiningAgentConversationMapper miningAgentConversationMapper;
    private final RedisSlidingWindowLimiterService redisSlidingWindowLimiterService;

    public MineService(PreProcessingGraphFactory preProcessingGraphFactory, MiningAgentConversationMapper miningAgentConversationMapper, RedisSlidingWindowLimiterService redisSlidingWindowLimiterService) {
        this.preProcessingGraphFactory = preProcessingGraphFactory;
        this.miningAgentConversationMapper = miningAgentConversationMapper;
        this.redisSlidingWindowLimiterService = redisSlidingWindowLimiterService;
    }

    @Override
    public String intentRecognize(String query) throws GraphStateException {
        CompiledGraph graph = preProcessingGraphFactory.getIntentRecognizeInstance();
        Optional<OverAllState> call = graph.call(Map.of("query", query));
        Map<String, Object> stringObjectMap = call.map(OverAllState::data).orElse(Map.of());
        return Optional.ofNullable(stringObjectMap.get("recognizeResult"))
                // 校验是List类型
                .filter(obj -> obj instanceof List<?>)
                .map(obj -> (List<?>) obj)
                // 校验List非空
                .filter(list -> !list.isEmpty())
                // 取第一个元素
                .map(list -> list.get(0))
                // 校验是String类型
                .filter(obj -> obj instanceof String)
                .map(obj -> (String) obj)
                // 没有的话给默认值（比如空字符串）
                .orElse("");
    }

    @Override
    public String chat(UserChatVO userChatVO) throws GraphStateException {
        String userId = userChatVO.getUserId();
        String conversationId = userChatVO.getConversationId();
        List<MiningAgentConversation> conversations = miningAgentConversationMapper.selectByUserId(userId);
        if (conversations == null || conversations.isEmpty()) {
            throw new RuntimeException("当前用户不存在对话！！");
        }
        //CompiledGraph graph = preProcessingGraphFactory.getChatInstance();
        //CompiledGraph graph = preProcessingGraphFactory.getRagChatInstance();
        // CompiledGraph graph = preProcessingGraphFactory.getDBInvocationChatInstance();
        //CompiledGraph graph = preProcessingGraphFactory.getRagChatInstanceWithWrapper();
        CompiledGraph graph = preProcessingGraphFactory.getDBInvocationChatInstanceWithWrapper();
        Optional<OverAllState> call = graph.call(Map.of("queryInfo", userChatVO));
        //Todo解析Rag回答的上下文
        //parseRagContext(call);
        //获取graph的执行流程跟踪日志
        try {
            Map<String, NodeExecutionLog> allNodeLogs = LogContextHolder.getAllNodeLogs();
            String jsonLog = StructuredLogBuilder.buildJsonLog(allNodeLogs);
            int totalTokens = LogContextHolder.getTotalTokens();
            redisSlidingWindowLimiterService.updateTokenQuota(userId, totalTokens);
            log.info("Graph Execution Logs: {}", jsonLog);
        } finally {
            LogContextHolder.clear();
        }
        return (String) call.map(OverAllState::data).orElse(Map.of()).get("chatResult");
    }

    private void parseRagContext(Optional<OverAllState> call) {
        Map<String, Object> stringObjectMap = call.map(OverAllState::data).orElse(Map.of());
        RagRequestContextData ragRequestContextData = RagRequestContext.get();
        ragRequestContextData.setQuery(stringObjectMap.get("queryInfo").toString());
        ragRequestContextData.setChatAnswer(stringObjectMap.get("chatResult").toString());
    }
}
