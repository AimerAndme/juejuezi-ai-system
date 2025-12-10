package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.domin.entity.MiningAgentConversation;
import com.yupi.yuaiagent.domin.entity.MiningAgentConversationMsg;
import com.yupi.yuaiagent.service.IMiningAgentConversationService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 矿山对话管理 Controller
 */
@RestController
@RequestMapping("/conversation")
public class ConversationController {

    private final IMiningAgentConversationService conversationService;

    public ConversationController(IMiningAgentConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * 获取用户的最新对话及其消息
     */
    @GetMapping("/latest/{userId}")
    public Map<String, Object> getLatestConversationWithMessages(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();

        // 查询最新对话
        MiningAgentConversation conversation = conversationService.getLatestConversationByUserId(userId);

        if (conversation == null) {
            result.put("code", 404);
            result.put("message", "未找到对话记录");
            result.put("data", null);
            return result;
        }

        // 查询对话消息
        List<MiningAgentConversationMsg> messages
                = conversationService.getMessagesByConversationId(conversation.getConversationId());

        result.put("code", 200);
        result.put("message", "获取成功");

        Map<String, Object> data = new HashMap<>();
        data.put("conversation", conversation);
        data.put("messages", messages);
        result.put("data", data);

        return result;
    }

    /**
     * 创建新对话
     */
    @PostMapping("/create")
    public Map<String, Object> createConversation(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            MiningAgentConversation newConversation = new MiningAgentConversation();
            newConversation.setConversationId(java.util.UUID.randomUUID().toString());
            newConversation.setUserId(userId);
            newConversation.setTopic("矿山专家对话");
            newConversation.setStatus(0); // 0-进行中
            newConversation.setStartTime(java.time.LocalDateTime.now());
            newConversation.setLastMsgTime(java.time.LocalDateTime.now());
            newConversation.setIsDeleted(false);

            conversationService.addConversation(newConversation);

            result.put("code", 200);
            result.put("message", "创建成功");
            result.put("data", newConversation);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "创建失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 根据对话 ID查询消息列表
     */
    @GetMapping("/messages/{conversationId}")
    public Map<String, Object> getMessages(@PathVariable String conversationId) {
        Map<String, Object> result = new HashMap<>();

        List<MiningAgentConversationMsg> messages
                = conversationService.getMessagesByConversationId(conversationId);

        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", messages);

        return result;
    }
}
