package com.yupi.yuaiagent.util.strategy;

/**
 * 技术策略类
 */
public interface PromptStrategy {
    String getPrompt(String role);
}
