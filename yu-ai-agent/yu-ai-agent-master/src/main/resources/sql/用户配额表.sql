-- 1. 创建表
CREATE TABLE user_quota (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT NOT NULL UNIQUE,
    
    -- 核心限流指标
    quota_qpm INTEGER NOT NULL DEFAULT 60 CHECK (quota_qpm >= 0),
    quota_tpm INTEGER NOT NULL DEFAULT 100000 CHECK (quota_tpm >= 0),
    quota_concurrent SMALLINT NOT NULL DEFAULT 3 CHECK (quota_concurrent >= 1),
    
    -- 用户属性
    user_level SMALLINT NOT NULL DEFAULT 1,
    status SMALLINT NOT NULL DEFAULT 1 CHECK (status IN (0, 1)),
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. 添加表注释
COMMENT ON TABLE user_quota IS 'RAG系统用户配额表';

-- 3. 添加字段注释
COMMENT ON COLUMN user_quota.user_id IS '用户唯一ID (如 UUID, OpenID 或 API Key)';
COMMENT ON COLUMN user_quota.quota_qpm IS '每分钟请求数配额 (Queries Per Minute)';
COMMENT ON COLUMN user_quota.quota_tpm IS '每分钟 Token 配额 (Tokens Per Minute) - AI 核心指标';
COMMENT ON COLUMN user_quota.quota_concurrent IS '最大并发请求数 (防止 GPU 显存溢出)';
COMMENT ON COLUMN user_quota.user_level IS '用户等级: 1-普通, 2-VIP, 3-SVIP';
COMMENT ON COLUMN user_quota.status IS '状态: 1-正常, 0-禁用';