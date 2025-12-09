package com.yupi.yuaiagent.service.impl;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.yupi.yuaiagent.domin.entity.MiningAgentConversation;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import com.yupi.yuaiagent.graph.PreProcessingGraphFactory;
import com.yupi.yuaiagent.mapper.MiningAgentConversationMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MineService implements com.yupi.yuaiagent.service.IMineService {
    private final PreProcessingGraphFactory preProcessingGraphFactory;
    private final MiningAgentConversationMapper miningAgentConversationMapper;

    public MineService(PreProcessingGraphFactory preProcessingGraphFactory, MiningAgentConversationMapper miningAgentConversationMapper) {
        this.preProcessingGraphFactory = preProcessingGraphFactory;
        this.miningAgentConversationMapper = miningAgentConversationMapper;
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
        CompiledGraph graph = preProcessingGraphFactory.getChatInstance();
        Optional<OverAllState> call = graph.call(Map.of("queryInfo", userChatVO));
        return (String) call.map(OverAllState::data).orElse(Map.of()).get("chatResult");
    }
}
