package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.MiningAgentConversation;
import com.yupi.yuaiagent.domin.entity.MiningAgentConversationMsg;
import java.util.List;

/**
 * 矿山对话服务接口
 */
public interface IMiningAgentConversationService {

    /**
     * 根据用户ID查询最新的未删除对话
     */
    MiningAgentConversation getLatestConversationByUserId(String userId);

    /**
     * 根据对话ID查询消息列表
     */
    List<MiningAgentConversationMsg> getMessagesByConversationId(String conversationId);

    /**
     * 新增对话
     */
    int addConversation(MiningAgentConversation conversation);

    /**
     * 新增消息
     */
    int addMessage(MiningAgentConversationMsg message);

    /**
     * 更新对话最后消息时间
     */
    int updateLastMsgTime(String conversationId, java.time.LocalDateTime lastMsgTime);
}
