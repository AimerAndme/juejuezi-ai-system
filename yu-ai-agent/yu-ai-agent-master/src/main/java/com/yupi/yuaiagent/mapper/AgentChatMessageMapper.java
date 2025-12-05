package com.yupi.yuaiagent.mapper;


import com.yupi.yuaiagent.domin.entity.AgentChatMessage;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话消息 Mapper 接口（MyBatis）
 */
@Repository
public interface AgentChatMessageMapper {
    /**
     * 新增消息（写入主表，触发器自动路由到对应分表）
     */
    int insert(AgentChatMessage message);

    /**
     * 根据ID删除消息（物理删除，谨慎使用；煤矿场景建议只更新状态）
     */
    int deleteById(Long id);

    /**
     * 根据ID更新消息状态（如撤回消息：status=3）
     * 注：消息内容不允许修改，符合煤矿审计要求
     */
    int updateStatusById(@Param("id") Long id, @Param("messageStatus") Integer messageStatus);

    /**
     * 根据ID查询消息（单条查询）
     */
    AgentChatMessage selectById(Long id);

    /**
     * 根据会话ID查询消息列表（会话回溯，核心接口）
     */
    List<AgentChatMessage> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据会话ID+消息类型查询（如仅查用户输入/Agent回复）
     */
    List<AgentChatMessage> selectBySessionIdAndType(
            @Param("sessionId") String sessionId,
            @Param("messageType") Integer messageType
    );

    /**
     * 按时间范围查询消息（审计/统计场景）
     */
    List<AgentChatMessage> selectByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("messageStatus") Integer messageStatus // 可选：筛选状态
    );

//    /**
//     * 关联会话表：按用户ID查询消息（煤矿场景：用户历史对话）
//     */
    List<AgentChatMessage> selectByUserId(
            @Param("userId") String userId,
            @Param("limit") Integer limit // 限制返回条数，避免数据过多
    );
}