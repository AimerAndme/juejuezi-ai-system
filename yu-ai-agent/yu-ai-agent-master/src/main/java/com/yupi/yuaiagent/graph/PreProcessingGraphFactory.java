package com.yupi.yuaiagent.graph;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.yupi.yuaiagent.node.IntentRecognitionNode;
import com.yupi.yuaiagent.node.QueryRewritingNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PreProcessingGraphFactory {
    private final ChatClient basicsChatClient;

    public PreProcessingGraphFactory(ChatClient basicsChatClient) {
        this.basicsChatClient = basicsChatClient;
    }

    public CompiledGraph getInstance() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactory() {
            @Override
            public Map<String, KeyStrategy> apply() {
                return Map.of(
                        "query", new ReplaceStrategy(),
                        "recognizeResult", new AppendStrategy());
            }
        };
        StateGraph stateGraph = new StateGraph("PreProcessingGraph", keyStrategyFactory);
        stateGraph.addNode("查询重写", AsyncNodeAction.node_async(new QueryRewritingNode(basicsChatClient)));
        stateGraph.addEdge(StateGraph.START, "查询重写");
        stateGraph.addNode("意图识别", AsyncNodeAction.node_async(new IntentRecognitionNode(basicsChatClient)));
        stateGraph.addEdge("查询重写", "意图识别");
        stateGraph.addEdge("意图识别", StateGraph.END);
        return stateGraph.compile();
    }

}
