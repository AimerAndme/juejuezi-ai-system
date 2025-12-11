package com.yupi.yuaiagent.util.strategy;

import com.yupi.yuaiagent.utils.TxtReader;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NormalChatPromptStrategy {
    private static final Map<String, String> PROMPT_MAP = new HashMap<>();

    static {

        try {
            // 2. 修正：Key与文件路径一一对应，修正错别字（旷工→矿工）
            String minerPrompt = TxtReader.readTxtToString("classpath:prompt/对话端闲聊提示词(工友).txt");
            PROMPT_MAP.put("miner", minerPrompt);
            log.info("成功加载矿工闲聊提示词");

            String technicianPrompt = TxtReader.readTxtToString("classpath:prompt/对话端闲聊提示词(技术员).txt");
            PROMPT_MAP.put("technician", technicianPrompt);
            log.info("成功加载技术员闲聊提示词");

            String managerPrompt = TxtReader.readTxtToString("classpath:prompt/对话端闲聊提示词(经理).txt");
            PROMPT_MAP.put("manager", managerPrompt);
            log.info("成功加载经理闲聊提示词");
        } catch (Exception e) {
            String errorMsg = "闲聊节点提示词加载失败！请检查文件路径或文件是否存在";
            log.error(errorMsg, e); // 日志记录完整异常栈
            // 4. 抛运行时异常：阻止Bean初始化失败，便于排查（Spring会打印完整栈）
            throw new IllegalStateException("闲聊节点提示词加载失败！", e);
        }
    }

    public NormalChatPromptStrategy() {
    }


    public static String getPrompt(String role) {
        // 4. 修正：空值兜底，避免返回null（可根据业务调整兜底提示词）
        String prompt = PROMPT_MAP.get(role);
        if (prompt == null) {
            log.warn("未找到角色[{}]对应的闲聊提示词，使用默认提示词", role);
            // 兜底提示词（根据你的业务场景调整）
            return "你好！请问有什么我能帮助你的吗？";
        }
        return prompt;
    }
}

