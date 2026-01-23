-- KEYS[1]: 限流key (如: quota:user:123:tokens)
-- ARGV[1]: 当前时间戳 (毫秒)
-- ARGV[2]: 时间窗口大小 (毫秒，如60000=1分钟)
-- ARGV[3]: 窗口内最大token数
-- ARGV[4]: 本次请求消耗的token数 (正数=增加，负数=减少)
-- ARGV[5]: 触发限流的阈值百分比 (如90表示90%)
-- ARGV[6]: 操作模式 (0=消费模式, 1=检查限流, 2=查询状态)

local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local maxTokens = tonumber(ARGV[3])
local requestTokens = tonumber(ARGV[4])
local thresholdPercent = tonumber(ARGV[5])
local mode = tonumber(ARGV[6])

-- 计算触发限流的token阈值
local thresholdTokens = math.floor(maxTokens * thresholdPercent / 100)

-- 获取当前窗口内的所有记录
local items = redis.call('ZRANGEBYSCORE', key, now - window, '+inf', 'WITHSCORES')

-- 计算已使用的token数
local usedTokens = 0
for i = 1, #items, 2 do
    usedTokens = usedTokens + tonumber(items[i])
end

-- 查询状态模式 (mode=2): 只返回配额状态，不检查限流
if mode == 2 then
    local oldestItem = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
    local resetTime = now
    if #oldestItem > 0 then
        resetTime = tonumber(oldestItem[2]) + window
    end
    local usagePercent = math.floor(usedTokens / maxTokens * 100)
    local remaining = maxTokens - usedTokens
    return {1, remaining, resetTime, usagePercent, 0}
end

-- 检查限流模式 (mode=1): 检查请求是否会被限流，不插入数据
if mode == 1 then
    local oldestItem = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
    local resetTime = now
    if #oldestItem > 0 then
        resetTime = tonumber(oldestItem[2]) + window
    end
    local usagePercent = math.floor(usedTokens / maxTokens * 100)
    local remaining = maxTokens - usedTokens
    local allowed = 1
    
    -- 检查是否达到阈值百分比
    if usedTokens >= thresholdTokens then
        allowed = 0
        return {0, remaining, resetTime, usagePercent, 1}
    end
    
    -- 检查是否超限
    if usedTokens + requestTokens > maxTokens then
        allowed = 0
        return {0, remaining, resetTime, usagePercent, 2}
    end
    
    return {allowed, remaining, resetTime, usagePercent, 0}
end

-- 消费模式 (mode=0): 直接插入数据，不做限流检查（调用方应提前检查）
-- 添加当前请求记录
redis.call('ZADD', key, now, requestTokens)
-- 设置过期时间（窗口大小+1秒，防止残留）
redis.call('EXPIRE', key, math.ceil(window / 1000) + 1)

-- 返回成功状态
local newUsedTokens = usedTokens + requestTokens
local usagePercent = math.floor(newUsedTokens / maxTokens * 100)
return {1, maxTokens - newUsedTokens, now + window, usagePercent, 0}