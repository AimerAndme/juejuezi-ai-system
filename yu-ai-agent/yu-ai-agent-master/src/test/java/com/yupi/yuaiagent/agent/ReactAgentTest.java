//package com.yupi.yuaiagent.agent;
//
//import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
//import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//public class ReactAgentTest {
//    @Test
//    public void text() {
//        // 创建模型实例
//        DashScopeApi dashScopeApi = DashScopeApi.builder()
//                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
//                .build();
//        ChatModel chatModel = DashScopeChatModel.builder()
//                .dashScopeApi(dashScopeApi)
//                .build();
//        // 创建 Agent
//        ReactAgent agent = ReactAgent.builder()
//                .name("weather_agent")
//                .model(chatModel)
//                .instruction("You are a helpful weather forecast assistant.")
//                .build();
//
//        // 运行 Agent
//        agent.call("what is the weather in Hangzhou?");
//    }
//}
