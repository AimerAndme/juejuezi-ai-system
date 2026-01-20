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
import com.yupi.yuaiagent.logging.LoggingNodeActionWrapper;
import com.yupi.yuaiagent.node.IntentRecognitionNode;
import com.yupi.yuaiagent.node.QueryRewritingNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PreProcessingGraphFactory {
    private final ChatClient chatClient;
    private final NodeAction normalChatNode;
    private final NodeAction ragQueryNode;
    private final NodeAction nl2SqlNode;
    private final NodeAction dBInvocationNode;
    private final NodeAction dBResult2NlNode;

    public PreProcessingGraphFactory(ChatClient chatClient, NodeAction normalChatNode, NodeAction ragQueryNode, NodeAction nl2SqlNode, @Qualifier("DBInvocationNode") NodeAction dBInvocationNode, @Qualifier("DBResult2NlNode") NodeAction dBResult2NlNode) {
        this.chatClient = chatClient;
        this.normalChatNode = normalChatNode;
        this.ragQueryNode = ragQueryNode;
        this.nl2SqlNode = nl2SqlNode;
        this.dBInvocationNode = dBInvocationNode;
        this.dBResult2NlNode = dBResult2NlNode;
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

    public CompiledGraph getDBInvocationChatInstance() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            return Map.of(
                    "queryInfo", new ReplaceStrategy(),//查询信息USerVo
                    "reWriteQuery", new ReplaceStrategy(),//查询重写结果
                    "recognizeResult", new ReplaceStrategy(),//识别结果
                    "ragQueryResult", new ReplaceStrategy(),//rag查询结果
                    "nl2SqlResult", new ReplaceStrategy(),//nl2sql结果
                    "dbInvocationResult", new ReplaceStrategy(),//db调用结果
                    "chatResult", new ReplaceStrategy());//db查询结果
        };
        StateGraph stateGraph = new StateGraph("PreProcessingGraph", keyStrategyFactory);
        stateGraph.addNode("查询重写", AsyncNodeAction.node_async(new QueryRewritingNode(chatClient)));
        stateGraph.addEdge(StateGraph.START, "查询重写");
        stateGraph.addNode("意图识别", AsyncNodeAction.node_async(new IntentRecognitionNode(chatClient)));
        stateGraph.addEdge("查询重写", "意图识别");
        stateGraph.addNode("闲聊", AsyncNodeAction.node_async(normalChatNode));
        stateGraph.addNode("rag专业知识库查询", AsyncNodeAction.node_async(ragQueryNode));
        stateGraph.addNode("nl2sql", AsyncNodeAction.node_async(nl2SqlNode));
        stateGraph.addNode("db调用", AsyncNodeAction.node_async(dBInvocationNode));
        stateGraph.addNode("最终聊天结果", AsyncNodeAction.node_async(dBResult2NlNode));
        stateGraph.addConditionalEdges(
                "意图识别",
                AsyncEdgeAction.edge_async(state -> state.value("recognizeResult", "chat")),
                Map.of("chat", "闲聊",
                        "ragChat", "rag专业知识库查询",
                        "dbChat", "nl2sql")
        );
        stateGraph.addEdge("闲聊", StateGraph.END);
        stateGraph.addEdge("rag专业知识库查询", StateGraph.END);
        stateGraph.addEdge("nl2sql", "db调用");
        stateGraph.addEdge("db调用", "最终聊天结果");
        stateGraph.addEdge("最终聊天结果", StateGraph.END);
        return stateGraph.compile();
    }

    public CompiledGraph getDBInvocationChatInstanceWithWrapper() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            return Map.of(
                    "queryInfo", new ReplaceStrategy(),//查询信息USerVo
                    "reWriteQuery", new ReplaceStrategy(),//查询重写结果
                    "recognizeResult", new ReplaceStrategy(),//识别结果
                    "ragQueryResult", new ReplaceStrategy(),//rag查询结果
                    "nl2SqlResult", new ReplaceStrategy(),//nl2sql结果
                    "dbInvocationResult", new ReplaceStrategy(),//db调用结果
                    "chatResult", new ReplaceStrategy());//db查询结果
        };
        StateGraph stateGraph = new StateGraph("PreProcessingGraph", keyStrategyFactory);
        stateGraph.addNode("查询重写", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(new QueryRewritingNode(chatClient), "QueryRewritingNode", "查询重写")
        ));
        stateGraph.addEdge(StateGraph.START, "查询重写");
        stateGraph.addNode("意图识别", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(new IntentRecognitionNode(chatClient), "IntentRecognitionNode", "意图识别")
        ));
        stateGraph.addEdge("查询重写", "意图识别");
        stateGraph.addNode("闲聊", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(normalChatNode, "NormalChatNode", "闲聊")
        ));
        stateGraph.addNode("rag专业知识库查询", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(ragQueryNode, "RagQueryNode", "rag专业知识库查询")
        ));
        stateGraph.addNode("nl2sql", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(nl2SqlNode, "Nl2SqlNode", "nl2sql")
        ));
        stateGraph.addNode("db调用", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(dBInvocationNode, "DbInvocationNode", "db调用")
        ));
        stateGraph.addNode("最终聊天结果", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(dBResult2NlNode, "DbResult2NlNode", "最终聊天结果")
        ));
        stateGraph.addConditionalEdges(
                "意图识别",
                AsyncEdgeAction.edge_async(state -> state.value("recognizeResult", "chat")),
                Map.of("chat", "闲聊",
                        "ragChat", "rag专业知识库查询",
                        "dbChat", "nl2sql")
        );
        stateGraph.addEdge("闲聊", StateGraph.END);
        stateGraph.addEdge("rag专业知识库查询", StateGraph.END);
        stateGraph.addEdge("nl2sql", "db调用");
        stateGraph.addEdge("db调用", "最终聊天结果");
        stateGraph.addEdge("最终聊天结果", StateGraph.END);
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

    public CompiledGraph getRagChatInstanceWithWrapper() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            return Map.of(
                    "queryInfo", new ReplaceStrategy(),//查询信息USerVo
                    "reWriteQuery", new ReplaceStrategy(),//查询重写结果
                    "recognizeResult", new ReplaceStrategy(),//识别结果
                    "ragQueryResult", new ReplaceStrategy());//rag查询结果

        };
        StateGraph stateGraph = new StateGraph("PreProcessingGraph", keyStrategyFactory);
        stateGraph.addNode("查询重写", AsyncNodeAction.node_async(new LoggingNodeActionWrapper(new QueryRewritingNode(chatClient), "QueryRewritingNode", "查询重写")));
        stateGraph.addEdge(StateGraph.START, "查询重写");
        stateGraph.addNode("意图识别", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(new IntentRecognitionNode(chatClient), "IntentRecognitionNode", "意图识别")
        ));
        stateGraph.addEdge("查询重写", "意图识别");
        stateGraph.addNode("闲聊", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(normalChatNode, "NormalChatNode", "闲聊")
        ));
        stateGraph.addNode("rag专业知识库查询", AsyncNodeAction.node_async(
                new LoggingNodeActionWrapper(ragQueryNode, "RagQueryNode", "rag专业知识库查询")
        ));
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
