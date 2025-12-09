CREATE TABLE mining_agent_conversation_msg
(
    msg_id          VARCHAR(64)      NOT NULL,
    conversation_id VARCHAR(64)      NOT NULL,
    sender_type     sender_type_enum NOT NULL,
    sender_id       VARCHAR(64)      NOT NULL,
    msg_type        msg_type_enum             DEFAULT 'text',
    msg_content     TEXT,
    file_meta       JSONB,
    send_time       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN          NOT NULL DEFAULT FALSE,
    PRIMARY KEY (msg_id),
    -- 外键关联对话表（对话删除时级联删除消息）
    CONSTRAINT fk_msg_conv FOREIGN KEY (conversation_id)
        REFERENCES mining_agent_conversation (conversation_id)
        ON DELETE CASCADE
);

-- 表注释
COMMENT ON TABLE mining_agent_conversation_msg IS '对话消息详情表（简化版）';

-- 字段注释
COMMENT ON COLUMN mining_agent_conversation_msg.msg_id IS '消息唯一标识（UUID）';
COMMENT ON COLUMN mining_agent_conversation_msg.conversation_id IS '关联对话ID';
COMMENT ON COLUMN mining_agent_conversation_msg.sender_type IS '发送方类型';
COMMENT ON COLUMN mining_agent_conversation_msg.sender_id IS '发送方ID（用户ID/agent/system）';
COMMENT ON COLUMN mining_agent_conversation_msg.msg_type IS '消息类型';
COMMENT ON COLUMN mining_agent_conversation_msg.msg_content IS '文本消息内容/非文本消息URL';
COMMENT ON COLUMN mining_agent_conversation_msg.file_meta IS '文件元信息（JSONB格式，非文件消息为空）';
COMMENT ON COLUMN mining_agent_conversation_msg.send_time IS '发送时间';
COMMENT ON COLUMN mining_agent_conversation_msg.is_deleted IS '软删除标识（false-正常，true-删除）';

-- 核心索引：优化高频查询
CREATE INDEX idx_msg_conv_id ON mining_agent_conversation_msg (conversation_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_msg_send_time ON mining_agent_conversation_msg (send_time ASC) WHERE is_deleted = FALSE;

-- 可选索引：按文件元信息查询（如筛选特定类型文件）
CREATE INDEX idx_msg_file_meta ON mining_agent_conversation_msg USING GIN (file_meta)
    WHERE is_deleted = FALSE AND file_meta IS NOT NULL;