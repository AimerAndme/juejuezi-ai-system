-- 发送方类型枚举
CREATE TYPE sender_type_enum AS ENUM ('user', 'agent', 'system');

-- 消息类型枚举
CREATE TYPE msg_type_enum AS ENUM ('text', 'image', 'file', 'voice');

-- 对话状态枚举
CREATE TYPE conversation_status_enum AS ENUM ('ongoing', 'completed', 'cancelled');

CREATE TABLE mining_agent_user
(
    user_id     VARCHAR(64) NOT NULL,
    user_name   VARCHAR(64) NOT NULL,
    user_role   VARCHAR(32)          DEFAULT 'common',
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id)
);

-- 添加表注释
COMMENT ON TABLE mining_agent_user IS '矿山Agent用户表（简化版）';

-- 添加字段注释
COMMENT ON COLUMN mining_agent_user.user_id IS '用户唯一标识（UUID/员工编号）';
COMMENT ON COLUMN mining_agent_user.user_name IS '用户名（真实姓名/昵称）';
COMMENT ON COLUMN mining_agent_user.user_role IS '用户角色（如miner/technician/manager）';
COMMENT ON COLUMN mining_agent_user.create_time IS '创建时间';
COMMENT ON COLUMN mining_agent_user.is_deleted IS '软删除标识（false-正常，true-删除）';

-- 索引：按用户角色查询（过滤已删除数据）
CREATE INDEX idx_user_role ON mining_agent_user (user_role) WHERE is_deleted = FALSE;