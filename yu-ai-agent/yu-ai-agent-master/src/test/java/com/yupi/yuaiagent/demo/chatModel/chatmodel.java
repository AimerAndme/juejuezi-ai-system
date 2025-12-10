package com.yupi.yuaiagent.demo.chatModel;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class chatmodel {
    @Autowired
    private ChatModel dashscopeChatModel;

    @Test
    public void test() {
        String call = dashscopeChatModel.call("你是啥");
        System.out.println(call);
    }

}
