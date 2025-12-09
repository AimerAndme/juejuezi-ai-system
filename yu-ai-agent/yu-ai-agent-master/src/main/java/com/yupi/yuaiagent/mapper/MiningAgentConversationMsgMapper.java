package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.MiningAgentConversationMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MiningAgentConversationMsgMapper {

    /**
     * 新增消息
     */
    int insert(MiningAgentConversationMsg msg);

    /**
     * 根据消息ID查询（未删除）
     */
    MiningAgentConversationMsg selectById(@Param("msgId") String msgId);

    /**
     * 根据对话ID查询消息列表（未删除，按发送时间升序）
     */
    List<MiningAgentConversationMsg> selectByConversationId(@Param("conversationId") String conversationId);


    /**
     * 根据对话ID查询消息列表（未删除，按发送时间升序,限制输入数量）
     */
    List<MiningAgentConversationMsg> selectByConversationIdOnLimit(@Param("conversationId") String conversationId, @Param("limit") Integer limit);

    /**
     * 根据用户ID和消息类型查询（未删除）
     */
    List<MiningAgentConversationMsg> selectByUserIdAndType(
            @Param("senderId") String senderId,
            @Param("msgType") Integer msgType
    );

    /**
     * 软删除消息
     */
    int softDelete(@Param("msgId") String msgId);

    /**
     * 批量软删除对话下的所有消息
     */
    int batchSoftDeleteByConversationId(@Param("conversationId") String conversationId);
}
