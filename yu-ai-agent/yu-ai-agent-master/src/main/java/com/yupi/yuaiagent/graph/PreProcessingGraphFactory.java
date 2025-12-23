package com.yupi.yuaiagent.graph;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.NodeAction;
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
    private final ChatClient chatClient;
    private final NodeAction normalChatNode;
    private final NodeAction ragQueryNode;

    public PreProcessingGraphFactory(ChatClient chatClient, NodeAction normalChatNode, NodeAction ragQueryNode) {
        this.chatClient = chatClient;
        this.normalChatNode = normalChatNode;
        this.ragQueryNode = ragQueryNode;
    }

    public CompiledGraph getIntentRecognizeInstance() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> Map.of(
                "query", new ReplaceStrategy(),
                "chatResult", new AppendStrategy(),
                "recognizeResult", new AppendStrategy());
        StateGraph stateGraph = new StateGraph("PreProcessingGraph", keyStrategyFactory);
        stateGraph.addNode("查询重写", AsyncNodeAction.node_async(new QueryRewritingNode(chatClient)));
        stateGraph.addEdge(StateGraph.START, "查询重写");
        stateGraph.addNode("意图识别", AsyncNodeAction.node_async(new IntentRecognitionNode(chatClient)));
        stateGraph.addEdge("查询重写", "意图识别");
        stateGraph.addEdge("意图识别", StateGraph.END);
        return stateGraph.compile();
    }

    public CompiledGraph getChatInstance() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactory() {
            @Override
            public Map<String, KeyStrategy> apply() {
                return Map.of(
                        "queryInfo", new ReplaceStrategy(),//查询信息USerVo
                        "reWriteQuery", new ReplaceStrategy(),//查询重写结果
                        "recognizeResult", new AppendStrategy());//查询结果
            }
        };
        StateGraph stateGraph = new StateGraph("PreProcessingGraph", keyStrategyFactory);
        stateGraph.addNode("查询重写", AsyncNodeAction.node_async(new QueryRewritingNode(chatClient)));
        stateGraph.addEdge(StateGraph.START, "查询重写");
        stateGraph.addNode("意图识别", AsyncNodeAction.node_async(new IntentRecognitionNode(chatClient)));
        stateGraph.addEdge("查询重写", "意图识别");
        stateGraph.addNode("闲聊", AsyncNodeAction.node_async(normalChatNode));
        stateGraph.addConditionalEdges(
                "意图识别",
                AsyncEdgeAction.edge_async(state -> state.value("意图识别", "chat")),
                Map.of("chat", "闲聊")
        );
        stateGraph.addEdge("闲聊", StateGraph.END);
        return stateGraph.compile();
    }

    public CompiledGraph getRagChatInstance() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            return Map.of(
                    "queryInfo", new ReplaceStrategy(),//查询信息USerVo
                    "reWriteQuery", new ReplaceStrategy(),//查询重写结果
                    "recognizeResult", new ReplaceStrategy(),//识别结果
                    "ragQueryResult", new ReplaceStrategy());//rag查询结果
        };
        StateGraph stateGraph = new StateGraph("PreProcessingGraph", keyStrategyFactory);
        stateGraph.addNode("查询重写", AsyncNodeAction.node_async(new QueryRewritingNode(chatClient)));
        stateGraph.addEdge(StateGraph.START, "查询重写");
        stateGraph.addNode("意图识别", AsyncNodeAction.node_async(new IntentRecognitionNode(chatClient)));
        stateGraph.addEdge("查询重写", "意图识别");
        stateGraph.addNode("闲聊", AsyncNodeAction.node_async(normalChatNode));
        stateGraph.addNode("rag专业知识库查询", AsyncNodeAction.node_async(ragQueryNode));
        stateGraph.addConditionalEdges(
                "意图识别",
                AsyncEdgeAction.edge_async(state -> state.value("recognizeResult", "chat")),
                Map.of("chat", "闲聊",
                        "ragChat", "rag专业知识库查询")
        );
        stateGraph.addEdge("闲聊", StateGraph.END);
        stateGraph.addEdge("rag专业知识库查询", StateGraph.END);
        return stateGraph.compile();
    }
}
