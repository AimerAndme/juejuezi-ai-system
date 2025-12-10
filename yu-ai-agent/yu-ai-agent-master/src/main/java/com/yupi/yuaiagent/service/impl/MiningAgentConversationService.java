package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.MiningAgentConversation;
import com.yupi.yuaiagent.domin.entity.MiningAgentConversationMsg;
import com.yupi.yuaiagent.mapper.MiningAgentConversationMapper;
import com.yupi.yuaiagent.mapper.MiningAgentConversationMsgMapper;
import com.yupi.yuaiagent.service.IMiningAgentConversationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 矿山对话服务实现
 */
@Service
@Transactional
public class MiningAgentConversationService implements IMiningAgentConversationService {

    private final MiningAgentConversationMapper conversationMapper;
    private final MiningAgentConversationMsgMapper msgMapper;

    public MiningAgentConversationService(
            MiningAgentConversationMapper conversationMapper,
            MiningAgentConversationMsgMapper msgMapper
    ) {
        this.conversationMapper = conversationMapper;
        this.msgMapper = msgMapper;
    }

    @Override
    public MiningAgentConversation getLatestConversationByUserId(String userId) {
        List<MiningAgentConversation> conversations = conversationMapper.selectByUserId(userId);
        if (conversations == null || conversations.isEmpty()) {
            return null;
        }
        // 返回最新的对话（最后一条）
        return conversations.get(conversations.size() - 1);
    }

    @Override
    public List<MiningAgentConversationMsg> getMessagesByConversationId(String conversationId) {
        return msgMapper.selectByConversationId(conversationId);
    }

    @Override
    public int addConversation(MiningAgentConversation conversation) {
        return conversationMapper.insert(conversation);
    }

    @Override
    public int addMessage(MiningAgentConversationMsg message) {
        return msgMapper.insert(message);
    }

    @Override
    public int updateLastMsgTime(String conversationId, LocalDateTime lastMsgTime) {
        return msgMapper.selectByConversationId(conversationId).isEmpty() ? 0
                : conversationMapper.updateLastMsgTime(conversationId, lastMsgTime);
    }
}
