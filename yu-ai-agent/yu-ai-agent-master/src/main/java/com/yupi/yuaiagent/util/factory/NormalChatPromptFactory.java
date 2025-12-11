package com.yupi.yuaiagent.util.factory;

import com.yupi.yuaiagent.util.strategy.NormalChatPromptStrategy;

public class NormalChatPromptFactory {

    public static String getPrompt(String role) {
        return NormalChatPromptStrategy.getPrompt(role);
    }
}
