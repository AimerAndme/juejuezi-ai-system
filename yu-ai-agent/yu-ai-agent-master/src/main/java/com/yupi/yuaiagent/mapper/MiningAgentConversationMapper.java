package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.MiningAgentConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MiningAgentConversationMapper {

    /**
     * 新增对话
     */
    int insert(MiningAgentConversation conversation);

    /**
     * 根据ID查询对话（未删除）
     */
    MiningAgentConversation selectById(@Param("conversationId") String conversationId);

    /**
     * 根据用户ID查询对话（未删除）
     */
    List<MiningAgentConversation> selectByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和时间范围查询对话（未删除）
     */
    List<MiningAgentConversation> selectByUserIdAndTime(
            @Param("userId") String userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 更新对话状态
     */
    int updateStatus(
            @Param("conversationId") String conversationId,
            @Param("status") Integer status,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 更新最后一条消息时间
     */
    int updateLastMsgTime(
            @Param("conversationId") String conversationId,
            @Param("lastMsgTime") LocalDateTime lastMsgTime
    );

    /**
     * 软删除对话
     */
    int softDelete(@Param("conversationId") String conversationId);
}
