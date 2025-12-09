CREATE TABLE mining_agent_conversation
(
    conversation_id VARCHAR(64) NOT NULL,
    user_id         VARCHAR(64) NOT NULL,
    topic           VARCHAR(256),
    status          conversation_status_enum DEFAULT 'ongoing',
    start_time      TIMESTAMP   NOT NULL     DEFAULT CURRENT_TIMESTAMP,
    end_time        TIMESTAMP,
    last_msg_time   TIMESTAMP   NOT NULL     DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN     NOT NULL     DEFAULT FALSE,
    PRIMARY KEY (conversation_id),
    -- 外键关联用户表（用户删除时级联删除对话）
    CONSTRAINT fk_conv_user FOREIGN KEY (user_id)
        REFERENCES mining_agent_user (user_id)
        ON DELETE CASCADE
);

-- 表注释
COMMENT ON TABLE mining_agent_conversation IS '对话主表（简化版）';

-- 字段注释
COMMENT ON COLUMN mining_agent_conversation.conversation_id IS '对话唯一标识（UUID）';
COMMENT ON COLUMN mining_agent_conversation.user_id IS '发起对话的用户ID';
COMMENT ON COLUMN mining_agent_conversation.topic IS '对话主题（如“掘进机故障排查”）';
COMMENT ON COLUMN mining_agent_conversation.status IS '对话状态';
COMMENT ON COLUMN mining_agent_conversation.start_time IS '对话开始时间';
COMMENT ON COLUMN mining_agent_conversation.end_time IS '对话结束时间（未结束为空）';
COMMENT ON COLUMN mining_agent_conversation.last_msg_time IS '最后一条消息时间';
COMMENT ON COLUMN mining_agent_conversation.is_deleted IS '软删除标识（false-正常，true-删除）';

-- 索引：高频查询场景
CREATE INDEX idx_conv_user_id ON mining_agent_conversation (user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_conv_start_time ON mining_agent_conversation (start_time DESC) WHERE is_deleted = FALSE;