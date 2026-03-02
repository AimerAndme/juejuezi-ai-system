# Token限额方案文档

## 一、概述

本文档介绍基于 Redis 和 Lua 脚本的配额限流工具，用于限制用户在单位时间内的 Token 消耗量。该方案采用滑动窗口算法，支持阈值百分比限流，精确控制用户的 Token 使用配额。

## 二、核心特性

### 1. 滑动窗口算法

- 精确控制时间窗口内的 Token 消耗
- 突发流量平滑处理
- 适合 Token 计费场景（按实际使用量限制）

### 2. 阈值百分比限流

- 支持提前限流（达到阈值百分比即限流）
- 避免用户在临界点频繁请求
- 不同用户类型可配置不同阈值

### 3. 使用率反馈

- 返回当前使用百分比
- 提供剩余 Token 数量
- 显示重置时间

### 4. 限流原因区分

- 区分"达到阈值"和"超过上限"两种情况
- 提供不同的错误提示
- 便于前端展示不同状态

## 三、技术架构

### 3.1 整体架构

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                               应用层                                           │
│ ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│ │  ChatController │ │  RAGController │ │  其他业务Controller │ │          │
│ └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘          │
└───────────────────────────┬──────────────────────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────────────────────┐
│                               服务层                                           │
│ ┌───────────────────────────────────────────────────────────────────────────┐ │
│ │                           QuotaService                                    │ │
│ │  - 检查 Token 配额                                                          │ │
│ │  - 管理配额规则                                                             │ │
│ │  - 提供配额查询接口                                                         │ │
│ └───────────────────────────────────────────────────────────────────────────┘ │
└───────────────────────────┬──────────────────────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────────────────────┐
│                               限流层                                           │
│ ┌───────────────────────────────────────────────────────────────────────────┐ │
│ │                      RedisSlidingWindowLimiter                            │ │
│ │  - 执行 Lua 脚本                                                            │ │
│ │  - 管理限流状态                                                             │ │
│ │  - 提供限流结果                                                             │ │
│ └───────────────────────────────────────────────────────────────────────────┘ │
└───────────────────────────┬──────────────────────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────────────────────┐
│                               数据层                                           │
│ ┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐      │
│ │  Redis (ZSet)       │ │  配置文件           │ │  数据库             │      │
│ │  - 存储时间窗口记录  │ │  - 限流规则配置     │ │  - 用户信息         │      │
│ │  - 自动过期清理     │ │  - 阈值百分比       │ │  - 配额记录         │      │
│ └─────────────────────┘ └─────────────────────┘ └─────────────────────┘      │
└───────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 核心组件

| 组件名称                  | 实现类                         | 功能说明                 | 应用场景          |
| ------------------------- | ------------------------------ | ------------------------ | ----------------- |
| QuotaLimiter              | QuotaLimiter.java              | 限流器接口               | 定义限流器规范    |
| RedisSlidingWindowLimiter | RedisSlidingWindowLimiter.java | Redis 滑动窗口限流器实现 | 基于 Redis 的限流 |
| QuotaService              | QuotaService.java              | 配额管理服务             | 业务层配额管理    |
| QuotaConfig               | QuotaConfig.java               | 配额配置类               | 配置限流规则      |
| LimitResult               | LimitResult.java               | 限流结果对象             | 封装限流结果      |

## 四、Lua 脚本设计

### 4.1 脚本参数

| 参数    | 类型   | 说明                                      | 示例值                | 查询模式是否需要        |
| ------- | ------ | ----------------------------------------- | --------------------- | ----------------------- |
| KEYS[1] | String | 限流 key                                  | quota:user:123:tokens | ✅ 是                   |
| ARGV[1] | Number | 当前时间戳（毫秒）                        | 1704067200000         | ✅ 是                   |
| ARGV[2] | Number | 时间窗口大小（毫秒）                      | 60000                 | ✅ 是                   |
| ARGV[3] | Number | 窗口内最大 token 数                       | 10000                 | ✅ 是                   |
| ARGV[4] | Number | 本次请求消耗的 token 数                   | 500                   | 消费/检查: ✅, 查询: ❌ |
| ARGV[5] | Number | 触发限流的阈值百分比（0-100）             | 90                    | 消费/检查: ✅, 查询: ❌ |
| ARGV[6] | Number | 操作模式 (0=消费, 1=检查限流, 2=查询状态) | 0                     | ✅ 是                   |

### 4.2 脚本逻辑

```lua
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

-- 更新模式（负数）：直接更新并返回
if requestTokens < 0 then
    redis.call('ZADD', key, now, requestTokens)
    redis.call('EXPIRE', key, math.ceil(window / 1000) + 1)

    local newUsedTokens = usedTokens + requestTokens
    local usagePercent = math.floor(newUsedTokens / maxTokens * 100)
    return {1, maxTokens - newUsedTokens, now + window, usagePercent, 0}
end

-- 消费模式 (mode=0): 检查是否限流并插入数据
-- 检查是否达到阈值百分比，直接限流
if usedTokens >= thresholdTokens then
    local oldestItem = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
    local resetTime = now
    if #oldestItem > 0 then
        resetTime = tonumber(oldestItem[2]) + window
    end
    local usagePercent = math.floor(usedTokens / maxTokens * 100)
    return {0, maxTokens - usedTokens, resetTime, usagePercent, 1}
end

-- 检查是否超限（兜底逻辑，防止超过100%）
if usedTokens + requestTokens > maxTokens then
    local oldestItem = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
    local resetTime = now
    if #oldestItem > 0 then
        resetTime = tonumber(oldestItem[2]) + window
    end
    local usagePercent = math.floor(usedTokens / maxTokens * 100)
    return {0, maxTokens - usedTokens, resetTime, usagePercent, 2}
end

-- 添加当前请求记录
redis.call('ZADD', key, now, requestTokens)
-- 设置过期时间（窗口大小+1秒，防止残留）
redis.call('EXPIRE', key, math.ceil(window / 1000) + 1)

-- 返回成功状态
local newUsedTokens = usedTokens + requestTokens
local usagePercent = math.floor(newUsedTokens / maxTokens * 100)
return {1, maxTokens - newUsedTokens, now + window, usagePercent, 0}
```

### 4.3 限流逻辑说明

脚本采用**双重检查机制**，确保不会超过配额上限：

1. **第一层检查：阈值百分比限流**
   - 当 `usedTokens >= thresholdTokens` 时，直接拒绝请求
   - 避免用户在临界点频繁请求
   - 提前限流，保护系统资源

2. **第二层检查：maxToken 兜底**
   - 当 `usedTokens + requestTokens > maxTokens` 时，拒绝请求
   - 确保即使未达到阈值，也不会超过 100%
   - 防止配额超支

3. **查询模式**
   - 只查询配额状态，不插入任何数据
   - 不会产生无用的 0 token 记录
   - 适合前端实时查询配额使用情况

### 4.4 限流场景示例

| 场景  | usedTokens | requestTokens | thresholdTokens | maxTokens | 结果    | 说明             |
| ----- | ---------- | ------------- | --------------- | --------- | ------- | ---------------- |
| 场景1 | 8500       | 500           | 9000            | 10000     | ✅ 允许 | 未达阈值，未超限 |
| 场景2 | 8500       | 1000          | 9000            | 10000     | ❌ 拒绝 | 未达阈值，但超限 |
| 场景3 | 9500       | 500           | 9000            | 10000     | ❌ 拒绝 | 已达阈值         |
| 场景4 | 5000       | 3000          | 9000            | 10000     | ✅ 允许 | 未达阈值，未超限 |

### 4.5 返回值说明

| 索引 | 类型   | 说明                                      | 示例值        |
| ---- | ------ | ----------------------------------------- | ------------- |
| 0    | Number | 是否允许请求 (0=拒绝, 1=允许)             | 1             |
| 1    | Number | 剩余 token 数                             | 9500          |
| 2    | Number | 重置时间戳（毫秒）                        | 1704067260000 |
| 3    | Number | 当前使用百分比（0-100）                   | 5             |
| 4    | Number | 限流原因 (0=正常, 1=达到阈值, 2=超过上限) | 0             |

## 五、更新机制

### 5.1 核心思路

由于预估 tokens 和实际 tokens 可能存在差异，需要在请求完成后根据实际消耗进行调整：

1. **请求开始**：预估 tokens，预扣减配额
2. **请求处理**：调用 LLM，获得响应
3. **请求结束**：计算实际 tokens，调整差额

### 5.2 更新流程

```
┌─────────────────────────────────────────────────────────────────┐
│                        请求开始                                  │
│  1. 估算 tokens 消耗                                              │
│  2. 调用 checkTokenQuota() 预扣减配额                              │
│  3. 检查是否允许请求                                               │
│     ├─ 不允许 → 返回错误                                           │
│     └─ 允许 → 继续                                                │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                        请求处理                                  │
│  1. 调用 LLM 获取响应                                             │
│  2. 计算实际 tokens 消耗                                           │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                        请求结束                                  │
│  1. 计算 token 差额 = 实际消耗 - 预估消耗                          │
│  2. 调用 updateTokenConsumption() 更新配额                         │
│     ├─ 差额 > 0 → 补扣 tokens                                     │
│     ├─ 差额 < 0 → 退还 tokens                                     │
│     └─ 差额 = 0 → 无需更新                                        │
└─────────────────────────────────────────────────────────────────┘
```

### 5.3 操作模式说明

Lua 脚本支持三种操作模式：

| 操作模式 | mode值 | 是否插入数据 | 需要的参数             | 使用场景                             |
| -------- | ------ | ------------ | ---------------------- | ------------------------------------ |
| 消费模式 | 0      | 是           | 全部参数               | 预扣减配额、更新实际消耗             |
| 检查限流 | 1      | 否           | 全部参数               | 检查某个请求是否会被限流             |
| 查询状态 | 2      | 否           | key, window, maxTokens | 查询当前配额状态（剩余多少、使用率） |

### 5.4 操作模式对比

| 方法                   | 操作模式     | 是否插入数据 | 需要的参数             | 使用场景                 |
| ---------------------- | ------------ | ------------ | ---------------------- | ------------------------ |
| `tryConsume()`         | 消费模式 (0) | 是           | 全部参数               | 预扣减配额               |
| `updateConsumption()`  | 消费模式 (0) | 是           | 全部参数               | 更新实际消耗             |
| `checkQuota()`         | 检查限流 (1) | 否           | 全部参数               | 检查某个请求是否会被限流 |
| `getQuotaStatus()`     | 查询状态 (2) | 否           | key, window, maxTokens | 查询当前配额状态         |
| `getRemainingTokens()` | 查询状态 (2) | 否           | key, window, maxTokens | 获取剩余配额             |

### 5.5 查询模式优势

1. **不产生无用数据**：查询模式不会插入任何记录
2. **精确查询**：可以查询任意 token 数量是否会被限流
3. **性能优化**：减少 Redis 写操作
4. **代码清晰**：查询和消费分离，逻辑更清晰
5. **参数简化**：查询状态模式只需要3个参数，更简洁

### 5.6 查询模式参数说明

**重要**：不同查询模式的参数需求不同：

#### 5.6.1 检查限流模式（mode=1）

需要传入全部参数，因为需要检查某个请求是否会被限流：

| 参数    | 是否需要 | 说明                                        |
| ------- | -------- | ------------------------------------------- |
| KEYS[1] | ✅ 是    | 限流 key，用于查询 Redis 中的记录           |
| ARGV[1] | ✅ 是    | 当前时间戳，用于计算时间窗口范围            |
| ARGV[2] | ✅ 是    | 时间窗口大小，用于查询窗口内的记录          |
| ARGV[3] | ✅ 是    | 最大 token 数，用于计算剩余配额和使用百分比 |
| ARGV[4] | ✅ 是    | 本次请求消耗的 token 数，用于检查是否会超限 |
| ARGV[5] | ✅ 是    | 阈值百分比，用于判断是否达到阈值            |
| ARGV[6] | ✅ 是    | 操作模式，用于区分查询和消费                |

**为什么检查限流需要全部参数？**

1. **计算时间窗口**：需要 `now` 和 `window` 来查询窗口内的记录
2. **计算使用率**：需要 `maxTokens` 来计算使用百分比
3. **检查限流**：需要 `requestTokens` 和 `thresholdPercent` 来判断是否限流
4. **返回完整信息**：需要所有参数才能返回剩余配额、使用率、重置时间等完整信息

#### 5.6.2 查询状态模式（mode=2）

只需要3个参数，用于查询当前配额状态：

| 参数    | 是否需要 | 说明                                        |
| ------- | -------- | ------------------------------------------- |
| KEYS[1] | ✅ 是    | 限流 key，用于查询 Redis 中的记录           |
| ARGV[1] | ✅ 是    | 当前时间戳，用于计算时间窗口范围            |
| ARGV[2] | ✅ 是    | 时间窗口大小，用于查询窗口内的记录          |
| ARGV[3] | ✅ 是    | 最大 token 数，用于计算剩余配额和使用百分比 |
| ARGV[4] | ❌ 否    | 不需要，只是查询状态                        |
| ARGV[5] | ❌ 否    | 不需要，只是查询状态                        |
| ARGV[6] | ✅ 是    | 操作模式，设置为2                           |

**为什么查询状态只需要3个参数？**

1. **查询配额状态**：只需要 `key`、`window`、`maxTokens` 来计算当前使用情况
2. **不检查限流**：不需要 `requestTokens` 和 `thresholdPercent`
3. **返回基本信息**：只返回剩余配额、使用率、重置时间等基本信息

#### 5.6.3 两种查询模式对比

| 特性         | 检查限流模式 (mode=1)    | 查询状态模式 (mode=2) |
| ------------ | ------------------------ | --------------------- |
| 需要参数     | 7个（全部）              | 4个（简化）           |
| 是否检查限流 | ✅ 是                    | ❌ 否                 |
| 返回信息     | 完整（含限流原因）       | 基础（配额状态）      |
| 使用场景     | 检查某个请求是否会被限流 | 查询当前配额状态      |
| 性能         | 稍慢（需要更多计算）     | 更快（只需查询）      |

### 5.7 Lua 脚本更新逻辑

更新后的 Lua 脚本支持负数 tokens，用于调整配额：

```lua
-- 如果是更新操作（负数），直接更新并返回
if requestTokens < 0 then
    -- 添加负数记录（表示减少）
    redis.call('ZADD', key, now, requestTokens)
    redis.call('EXPIRE', key, math.ceil(window / 1000) + 1)

    local newUsedTokens = usedTokens + requestTokens
    local usagePercent = math.floor(newUsedTokens / maxTokens * 100)
    return {1, maxTokens - newUsedTokens, now + window, usagePercent, 0}
end
```

### 5.8 Java 代码更新

#### QuotaLimiter 接口增加更新和查询方法

```java
public interface QuotaLimiter {
    // 消费模式
    LimitResult tryConsume(String key, long tokens);
    LimitResult tryConsume(String key, long tokens, long windowMs, long maxTokens);
    LimitResult tryConsume(String key, long tokens, long windowMs, long maxTokens, int thresholdPercent);

    // 更新模式
    LimitResult updateConsumption(String key, long tokens);
    LimitResult updateConsumption(String key, long tokens, long windowMs, long maxTokens, int thresholdPercent);

    // 检查限流模式（需要全部参数）
    LimitResult checkQuota(String key, long tokens);
    LimitResult checkQuota(String key, long tokens, long windowMs, long maxTokens, int thresholdPercent);

    // 查询状态模式（只需要3个参数）
    LimitResult getQuotaStatus(String key, long windowMs, long maxTokens);

    long getRemainingTokens(String key);
    void reset(String key);
}
```

#### QuotaService 增加更新和查询方法

```java
public LimitResult updateTokenConsumption(Long userId, long tokens) {
    String key = buildQuotaKey(userId);
    QuotaConfig.QuotaRule rule = quotaConfig.getRules().getOrDefault("default", new QuotaConfig.QuotaRule());
    return quotaLimiter.updateConsumption(key, tokens, rule.getWindowMs(), rule.getMaxTokens(), rule.getThresholdPercent());
}

public LimitResult updateTokenConsumption(String userType, Long userId, long tokens) {
    String key = buildQuotaKey(userType, userId);
    QuotaConfig.QuotaRule rule = quotaConfig.getRules().getOrDefault(userType, new QuotaConfig.QuotaRule());
    return quotaLimiter.updateConsumption(key, tokens, rule.getWindowMs(), rule.getMaxTokens(), rule.getThresholdPercent());
}

public LimitResult checkQuota(Long userId, long tokens) {
    String key = buildQuotaKey(userId);
    QuotaConfig.QuotaRule rule = quotaConfig.getRules().getOrDefault("default", new QuotaConfig.QuotaRule());
    return quotaLimiter.checkQuota(key, tokens, rule.getWindowMs(), rule.getMaxTokens(), rule.getThresholdPercent());
}

public LimitResult checkQuota(String userType, Long userId, long tokens) {
    String key = buildQuotaKey(userType, userId);
    QuotaConfig.QuotaRule rule = quotaConfig.getRules().getOrDefault(userType, new QuotaConfig.QuotaRule());
    return quotaLimiter.checkQuota(key, tokens, rule.getWindowMs(), rule.getMaxTokens(), rule.getThresholdPercent());
}

public LimitResult getQuotaStatus(Long userId) {
    String key = buildQuotaKey(userId);
    QuotaConfig.QuotaRule rule = quotaConfig.getRules().getOrDefault("default", new QuotaConfig.QuotaRule());
    return quotaLimiter.getQuotaStatus(key, rule.getWindowMs(), rule.getMaxTokens());
}

public LimitResult getQuotaStatus(String userType, Long userId) {
    String key = buildQuotaKey(userType, userId);
    QuotaConfig.QuotaRule rule = quotaConfig.getRules().getOrDefault(userType, new QuotaConfig.QuotaRule());
    return quotaLimiter.getQuotaStatus(key, rule.getWindowMs(), rule.getMaxTokens());
}
```

### 5.9 使用示例

#### 查询配额状态（简化版，只需要3个参数）

```java
@GetMapping("/quota/status")
public Result<QuotaInfo> getQuotaStatus(@RequestParam Long userId) {
    String key = buildQuotaKey(userId);
    QuotaConfig.QuotaRule rule = quotaConfig.getRules().getOrDefault("default", new QuotaConfig.QuotaRule());

    // 只需要 key, window, maxTokens
    LimitResult result = quotaService.getQuotaStatus(userId);

    QuotaInfo quotaInfo = new QuotaInfo();
    quotaInfo.setUserId(userId);
    quotaInfo.setRemainingTokens(result.getRemainingTokens());
    quotaInfo.setUsagePercent(result.getUsagePercent());
    quotaInfo.setResetTime(result.getResetTime());

    return Result.success(quotaInfo);
}
```

#### 检查请求是否会被限流（需要全部参数）

```java
@GetMapping("/quota/check")
public Result<QuotaInfo> checkQuota(@RequestParam Long userId, @RequestParam long tokens) {
    LimitResult result = quotaService.checkQuota(userId, tokens);

    QuotaInfo quotaInfo = new QuotaInfo();
    quotaInfo.setUserId(userId);
    quotaInfo.setAllowed(result.isAllowed());
    quotaInfo.setRemainingTokens(result.getRemainingTokens());
    quotaInfo.setUsagePercent(result.getUsagePercent());
    quotaInfo.setResetTime(result.getResetTime());
    quotaInfo.setLimitReason(result.getLimitReason());

    return Result.success(quotaInfo);
}
```

#### 完整的请求流程

```java
@PostMapping("/message")
public Result<String> sendMessage(@RequestBody ChatRequest request) {
    Long userId = getUserIdFromToken();

    long estimatedTokens = estimateTokens(request.getMessage());

    LimitResult limitResult = quotaService.checkTokenQuota(userId, estimatedTokens);

    if (!limitResult.isAllowed()) {
        long resetSeconds = (limitResult.getResetTime() - System.currentTimeMillis()) / 1000;

        if (limitResult.getLimitReason() == LimitResult.LimitReason.THRESHOLD_REACHED) {
            throw new BusinessException(ErrorCode.QUOTA_THRESHOLD_REACHED,
                String.format("Token配额使用已达%d%%，请%d秒后重试",
                    limitResult.getUsagePercent(), resetSeconds));
        } else {
            throw new BusinessException(ErrorCode.QUOTA_EXCEEDED,
                String.format("Token配额已用完，请%d秒后重试", resetSeconds));
        }
    }

    String response = null;
    try {
        response = chatService.sendMessage(request);

        long actualTokens = calculateActualTokens(request.getMessage(), response);
        long tokenDiff = actualTokens - estimatedTokens;

        if (tokenDiff != 0) {
            quotaService.updateTokenConsumption(userId, tokenDiff);
        }

        return Result.success(response);
    } catch (Exception e) {
        quotaService.updateTokenConsumption(userId, -estimatedTokens);
        throw e;
    }
}

private long estimateTokens(String message) {
    return (long) (message.length() * 1.5);
}

private long calculateActualTokens(String message, String response) {
    return (long) ((message.length() + response.length()) * 1.5);
}
```

### 5.6 使用 AOP 简化代码

#### QuotaUpdate 注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuotaUpdate {
    String userType() default "default";
}
```

#### QuotaUpdateAspect 切面

```java
@Aspect
@Component
@RequiredArgsConstructor
public class QuotaUpdateAspect {

    private final QuotaService quotaService;

    @Around("@annotation(quotaUpdate)")
    public Object handleQuotaUpdate(ProceedingJoinPoint joinPoint, QuotaUpdate quotaUpdate) throws Throwable {
        Long userId = getUserIdFromContext();
        String userType = quotaUpdate.userType();

        long estimatedTokens = estimateTokens(joinPoint.getArgs());

        LimitResult limitResult = quotaService.checkTokenQuota(userType, userId, estimatedTokens);

        if (!limitResult.isAllowed()) {
            throw new BusinessException(ErrorCode.QUOTA_EXCEEDED, "配额不足");
        }

        Object result = null;
        try {
            result = joinPoint.proceed();

            long actualTokens = calculateActualTokens(joinPoint.getArgs(), result);
            long tokenDiff = actualTokens - estimatedTokens;

            if (tokenDiff != 0) {
                quotaService.updateTokenConsumption(userType, userId, tokenDiff);
            }

            return result;
        } catch (Exception e) {
            quotaService.updateTokenConsumption(userType, userId, -estimatedTokens);
            throw e;
        }
    }

    private Long getUserIdFromContext() {
        return 1L;
    }

    private long estimateTokens(Object[] args) {
        return 100L;
    }

    private long calculateActualTokens(Object[] args, Object result) {
        return 150L;
    }
}
```

#### 使用注解简化

```java
@PostMapping("/message")
@QuotaUpdate(userType = "default")
public Result<String> sendMessage(@RequestBody ChatRequest request) {
    String response = chatService.sendMessage(request);
    return Result.success(response);
}
```

### 5.7 异常处理

当请求失败时，需要退还已预扣减的 tokens：

```java
try {
    response = chatService.sendMessage(request);
    long actualTokens = calculateActualTokens(request.getMessage(), response);
    long tokenDiff = actualTokens - estimatedTokens;

    if (tokenDiff != 0) {
        quotaService.updateTokenConsumption(userId, tokenDiff);
    }

    return Result.success(response);
} catch (Exception e) {
    quotaService.updateTokenConsumption(userId, -estimatedTokens);
    throw e;
}
```

### 5.8 更新机制优势

1. **精确计费**：根据实际 tokens 消耗计费，避免多扣或少扣
2. **异常恢复**：请求失败时自动退还预扣减的 tokens
3. **透明化**：用户可以清楚看到实际 tokens 消耗
4. **灵活调整**：支持正负数更新，灵活调整配额

## 六、Java 实现设计

### 6.1 核心接口

#### QuotaLimiter 接口

```java
public interface QuotaLimiter {
    LimitResult tryConsume(String key, long tokens);
    LimitResult tryConsume(String key, long tokens, long windowMs, long maxTokens);
    LimitResult tryConsume(String key, long tokens, long windowMs, long maxTokens, int thresholdPercent);

    LimitResult updateConsumption(String key, long tokens);
    LimitResult updateConsumption(String key, long tokens, long windowMs, long maxTokens, int thresholdPercent);

    long getRemainingTokens(String key);
    void reset(String key);
}
```

#### LimitResult 返回对象

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitResult {
    private boolean allowed;
    private long remainingTokens;
    private long resetTime;
    private long windowStart;
    private long windowEnd;
    private int usagePercent;
    private LimitReason limitReason;

    public enum LimitReason {
        NORMAL(0, "正常"),
        THRESHOLD_REACHED(1, "达到阈值百分比"),
        LIMIT_EXCEEDED(2, "超过配额上限");

        private final int code;
        private final String desc;

        LimitReason(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static LimitReason fromCode(int code) {
            return Arrays.stream(values())
                .filter(r -> r.code == code)
                .findFirst()
                .orElse(NORMAL);
        }
    }
}
```

### 6.2 Redis 滑动窗口限流器实现

```java
@Component
@RequiredArgsConstructor
public class RedisSlidingWindowLimiter implements QuotaLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SLIDING_WINDOW_LUA =
        "local key = KEYS[1]\n" +
        "local now = tonumber(ARGV[1])\n" +
        "local window = tonumber(ARGV[2])\n" +
        "local maxTokens = tonumber(ARGV[3])\n" +
        "local requestTokens = tonumber(ARGV[4])\n" +
        "local thresholdPercent = tonumber(ARGV[5])\n" +
        "local thresholdTokens = math.floor(maxTokens * thresholdPercent / 100)\n" +
        "local items = redis.call('ZRANGEBYSCORE', key, now - window, '+inf', 'WITHSCORES')\n" +
        "local usedTokens = 0\n" +
        "for i = 1, #items, 2 do\n" +
        "    usedTokens = usedTokens + tonumber(items[i])\n" +
        "end\n" +
        "if requestTokens < 0 then\n" +
        "    redis.call('ZADD', key, now, requestTokens)\n" +
        "    redis.call('EXPIRE', key, math.ceil(window / 1000) + 1)\n" +
        "    local newUsedTokens = usedTokens + requestTokens\n" +
        "    local usagePercent = math.floor(newUsedTokens / maxTokens * 100)\n" +
        "    return {1, maxTokens - newUsedTokens, now + window, usagePercent, 0}\n" +
        "end\n" +
        "if usedTokens >= thresholdTokens then\n" +
        "    local oldestItem = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')\n" +
        "    local resetTime = now\n" +
        "    if #oldestItem > 0 then\n" +
        "        resetTime = tonumber(oldestItem[2]) + window\n" +
        "    end\n" +
        "    local usagePercent = math.floor(usedTokens / maxTokens * 100)\n" +
        "    return {0, maxTokens - usedTokens, resetTime, usagePercent, 1}\n" +
        "end\n" +
        "if usedTokens + requestTokens > maxTokens then\n" +
        "    local oldestItem = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')\n" +
        "    local resetTime = now\n" +
        "    if #oldestItem > 0 then\n" +
        "        resetTime = tonumber(oldestItem[2]) + window\n" +
        "    end\n" +
        "    local usagePercent = math.floor(usedTokens / maxTokens * 100)\n" +
        "    return {0, maxTokens - usedTokens, resetTime, usagePercent, 2}\n" +
        "end\n" +
        "redis.call('ZADD', key, now, requestTokens)\n" +
        "redis.call('EXPIRE', key, math.ceil(window / 1000) + 1)\n" +
        "local newUsedTokens = usedTokens + requestTokens\n" +
        "local usagePercent = math.floor(newUsedTokens / maxTokens * 100)\n" +
        "return {1, maxTokens - newUsedTokens, now + window, usagePercent, 0}";

    private DefaultRedisScript<List> slidingWindowScript;

    @PostConstruct
    public void init() {
        slidingWindowScript = new DefaultRedisScript<>();
        slidingWindowScript.setScriptText(SLIDING_WINDOW_LUA);
        slidingWindowScript.setResultType(List.class);
    }

    @Override
    public LimitResult tryConsume(String key, long tokens) {
        return tryConsume(key, tokens, 60000, 10000, 90);
    }

    @Override
    public LimitResult tryConsume(String key, long tokens, long windowMs, long maxTokens) {
        return tryConsume(key, tokens, windowMs, maxTokens, 90);
    }

    @Override
    public LimitResult tryConsume(String key, long tokens, long windowMs, long maxTokens, int thresholdPercent) {
        long now = System.currentTimeMillis();
        List<Long> result = redisTemplate.execute(
            slidingWindowScript,
            Collections.singletonList(key),
            String.valueOf(now),
            String.valueOf(windowMs),
            String.valueOf(maxTokens),
            String.valueOf(tokens),
            String.valueOf(thresholdPercent)
        );

        return buildLimitResult(result, now, windowMs);
    }

    @Override
    public LimitResult updateConsumption(String key, long tokens) {
        return updateConsumption(key, tokens, 60000, 10000, 90);
    }

    @Override
    public LimitResult updateConsumption(String key, long tokens, long windowMs, long maxTokens, int thresholdPercent) {
        long now = System.currentTimeMillis();
        List<Long> result = redisTemplate.execute(
            slidingWindowScript,
            Collections.singletonList(key),
            String.valueOf(now),
            String.valueOf(windowMs),
            String.valueOf(maxTokens),
            String.valueOf(tokens),
            String.valueOf(thresholdPercent)
        );

        return buildLimitResult(result, now, windowMs);
    }

    private LimitResult buildLimitResult(List<Long> result, long now, long windowMs) {
        boolean allowed = result.get(0) == 1;
        long remaining = result.get(1);
        long resetTime = result.get(2);
        int usagePercent = result.get(3).intValue();
        LimitResult.LimitReason limitReason = LimitResult.LimitReason.fromCode(result.get(4).intValue());

        return new LimitResult(allowed, remaining, resetTime, now - windowMs, now + windowMs, usagePercent, limitReason);
    }

    @Override
    public long getRemainingTokens(String key) {
        return tryConsume(key, 0).getRemainingTokens();
    }

    @Override
    public void reset(String key) {
        redisTemplate.delete(key);
    }
}
```

        redisTemplate.delete(key);
    }

}

````

### 5.3 配额管理服务

```java
@Service
@RequiredArgsConstructor
public class QuotaService {

    private final QuotaLimiter quotaLimiter;
    private final QuotaConfig quotaConfig;

    public LimitResult checkTokenQuota(Long userId, long tokens) {
        String key = buildQuotaKey(userId);
        QuotaConfig.QuotaRule rule = quotaConfig.getRules().getOrDefault("default", new QuotaConfig.QuotaRule());
        return quotaLimiter.tryConsume(key, tokens, rule.getWindowMs(), rule.getMaxTokens(), rule.getThresholdPercent());
    }

    public LimitResult checkTokenQuota(String userType, Long userId, long tokens) {
        String key = buildQuotaKey(userType, userId);
        QuotaConfig.QuotaRule rule = quotaConfig.getRules().getOrDefault(userType, new QuotaConfig.QuotaRule());
        return quotaLimiter.tryConsume(key, tokens, rule.getWindowMs(), rule.getMaxTokens(), rule.getThresholdPercent());
    }

    private String buildQuotaKey(Long userId) {
        return "quota:user:" + userId + ":tokens";
    }

    private String buildQuotaKey(String userType, Long userId) {
        return "quota:" + userType + ":" + userId + ":tokens";
    }

    public long getRemainingTokens(Long userId) {
        return quotaLimiter.getRemainingTokens(buildQuotaKey(userId));
    }

    public void resetUserQuota(Long userId) {
        quotaLimiter.reset(buildQuotaKey(userId));
    }
}
````

### 5.4 配额配置类

```java
@Data
@Configuration
@ConfigurationProperties(prefix = "quota")
public class QuotaConfig {
    private Map<String, QuotaRule> rules = new HashMap<>();

    @Data
    public static class QuotaRule {
        private long windowMs = 60000;
        private long maxTokens = 10000;
        private int thresholdPercent = 90;
        private String description;
    }
}
```

## 六、配置说明

### 6.1 application.yml 配置

```yaml
quota:
  rules:
    default:
      window-ms: 60000 # 1分钟
      max-tokens: 10000 # 最大1万tokens
      threshold-percent: 90 # 使用达到90%就限流
      description: '默认用户配额'
    vip:
      window-ms: 60000
      max-tokens: 50000 # VIP用户5万tokens
      threshold-percent: 95 # VIP用户95%才限流
      description: 'VIP用户配额'
    free:
      window-ms: 3600000 # 1小时
      max-tokens: 1000 # 免费用户1千tokens/小时
      threshold-percent: 80 # 免费用户80%就限流
      description: '免费用户配额'
```

### 6.2 Redis 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

## 七、使用示例

### 7.1 在 Controller 中使用

```java
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final QuotaService quotaService;

    @PostMapping("/message")
    public Result<String> sendMessage(@RequestBody ChatRequest request) {
        Long userId = getUserIdFromToken();
        long estimatedTokens = estimateTokens(request.getMessage());

        LimitResult limitResult = quotaService.checkTokenQuota(userId, estimatedTokens);

        if (!limitResult.isAllowed()) {
            long resetSeconds = (limitResult.getResetTime() - System.currentTimeMillis()) / 1000;

            if (limitResult.getLimitReason() == LimitResult.LimitReason.THRESHOLD_REACHED) {
                throw new BusinessException(ErrorCode.QUOTA_THRESHOLD_REACHED,
                    String.format("Token配额使用已达%d%%，请%d秒后重试",
                        limitResult.getUsagePercent(), resetSeconds));
            } else {
                throw new BusinessException(ErrorCode.QUOTA_EXCEEDED,
                    String.format("Token配额已用完，请%d秒后重试", resetSeconds));
            }
        }

        String response = chatService.sendMessage(request);

        long actualTokens = calculateActualTokens(response);
        quotaService.checkTokenQuota(userId, actualTokens - estimatedTokens);

        return Result.success(response);
    }
}
```

### 7.2 查询剩余配额

```java
@GetMapping("/quota/remaining")
public Result<QuotaInfo> getRemainingQuota() {
    Long userId = getUserIdFromToken();
    long remainingTokens = quotaService.getRemainingTokens(userId);

    QuotaInfo quotaInfo = new QuotaInfo();
    quotaInfo.setUserId(userId);
    quotaInfo.setRemainingTokens(remainingTokens);
    quotaInfo.setUsagePercent(calculateUsagePercent(userId, remainingTokens));

    return Result.success(quotaInfo);
}
```

### 7.3 重置用户配额

```java
@PostMapping("/quota/reset/{userId}")
public Result<Void> resetUserQuota(@PathVariable Long userId) {
    quotaService.resetUserQuota(userId);
    return Result.success();
}
```

## 八、REST API

### 8.1 查询配额信息

```bash
GET /quota/remaining
```

响应示例：

```json
{
  "code": 200,
  "data": {
    "userId": 123,
    "remainingTokens": 5000,
    "usagePercent": 50,
    "resetTime": 1704067260000
  },
  "message": "查询成功"
}
```

### 8.2 重置用户配额

```bash
POST /quota/reset/{userId}
```

响应示例：

```json
{
  "code": 200,
  "message": "重置成功"
}
```

## 九、工作原理

### 9.1 限流流程

```
1. 用户发起请求
   ↓
2. 估算 Token 消耗
   ↓
3. 调用 QuotaService 检查配额
   ↓
4. 执行 Lua 脚本
   ↓
5. 检查是否达到阈值百分比
   ├─ 是 → 返回限流结果（THRESHOLD_REACHED）
   └─ 否 → 继续
   ↓
6. 检查是否超过上限
   ├─ 是 → 返回限流结果（LIMIT_EXCEEDED）
   └─ 否 → 继续
   ↓
7. 添加请求记录到 Redis
   ↓
8. 返回成功结果
```

### 9.2 Redis 数据结构

```java
// 使用 Redis ZSet 存储时间窗口内的 Token 消耗记录
// Key: quota:user:{userId}:tokens
// Value: ZSet (member: token数, score: 时间戳)

// 示例：
ZADD quota:user:123:tokens 1704067200000 500
ZADD quota:user:123:tokens 1704067210000 300
ZADD quota:user:123:tokens 1704067220000 200

// 查询窗口内的记录
ZRANGEBYSCORE quota:user:123:tokens 1704067160000 +inf WITHSCORES

// 自动过期
EXPIRE quota:user:123:tokens 61
```

### 9.3 滑动窗口示意图

```
时间轴: ──────────────────────────────────────────────────────────────→
         ↑                              ↑                              ↑
      now-window                     now                         now+window

窗口内记录: ● ● ● ● ● ● ● ● ● ●
              ↓
        计算已使用 Token
              ↓
        判断是否限流
```

## 十、性能优化

### 10.1 Lua 脚本优化

- **原子操作**：所有操作在 Lua 脚本中原子执行，避免竞态条件
- **减少网络往返**：一次 Redis 调用完成所有操作
- **自动过期**：设置合理的过期时间，自动清理过期数据

### 10.2 Redis 优化

- **使用 ZSet**：利用 ZSet 的有序特性，高效查询窗口内记录
- **合理设置过期时间**：窗口大小 + 1 秒，防止残留数据
- **连接池配置**：合理配置 Redis 连接池，提高并发性能

### 10.3 业务层优化

- **Token 预估**：提前预估 Token 消耗，避免实际消耗超限
- **批量扣减**：支持批量扣减 Token，减少 Redis 调用次数
- **缓存配额信息**：缓存用户配额信息，减少 Redis 查询

## 十一、监控和告警

### 11.1 监控指标

```java
@Scheduled(fixedRate = 60000) // 每分钟检查一次
public void monitorQuota() {
    // 监控限流触发次数
    // 监控配额使用率
    // 监控 Redis 性能
    // 监控异常情况
}
```

### 11.2 告警规则

- **限流触发频率过高**：当限流触发次数超过阈值时告警
- **配额使用率过高**：当用户配额使用率持续高于阈值时告警
- **Redis 性能异常**：当 Redis 响应时间超过阈值时告警
- **异常限流**：当正常请求被错误限流时告警

## 十二、故障排查

### 12.1 限流误触发

**问题**：正常请求被错误限流

**排查步骤**：

1. 检查 Redis 是否正常运行
2. 检查 Lua 脚本是否正确加载
3. 检查时间窗口配置是否合理
4. 检查 Token 计算是否准确

### 12.2 配额不生效

**问题**：配额限制未生效

**排查步骤**：

1. 检查配置文件是否正确加载
2. 检查限流器是否正确注入
3. 检查 Redis 连接是否正常
4. 检查限流逻辑是否正确执行

### 12.3 性能问题

**问题**：限流影响系统性能

**排查步骤**：

1. 检查 Redis 响应时间
2. 检查 Lua 脚本执行时间
3. 检查网络延迟
4. 检查并发量

## 十三、方案对比

| 特性       | 滑动窗口  | 固定窗口 | 令牌桶  | 漏桶     |
| ---------- | --------- | -------- | ------- | -------- |
| 精确度     | 高        | 低       | 中      | 中       |
| 突发流量   | 平滑      | 不平滑   | 允许    | 限制     |
| 实现复杂度 | 中        | 低       | 中      | 中       |
| 内存占用   | 中        | 低       | 低      | 低       |
| 适用场景   | Token计费 | 简单限流 | API限流 | 流量整形 |

**推荐方案**：对于 RAG 系统的 Token 消耗限流，**滑动窗口方案**是最合适的，因为：

1. 精确控制实际 Token 消耗
2. 避免突发流量导致超支
3. 支持灵活的配额策略
4. 提供使用率反馈

## 十四、优势

1. **精确控制**：精确控制时间窗口内的 Token 消耗
2. **提前限流**：支持阈值百分比，提前限流避免超支
3. **平滑处理**：滑动窗口算法平滑处理突发流量
4. **灵活配置**：支持不同用户类型的差异化配置
5. **使用率反馈**：提供详细的使用率信息
6. **高性能**：基于 Redis + Lua，性能优异
7. **原子操作**：Lua 脚本保证原子性，避免竞态条件
8. **自动清理**：自动过期机制，无需手动清理

## 十五、劣势

1. **Redis 依赖**：依赖 Redis 服务，存在单点故障风险
2. **内存占用**：需要维护时间戳列表，内存占用稍高
3. **实现复杂**：相比固定窗口，实现稍复杂
4. **Token 预估**：需要准确预估 Token 消耗，否则可能超限

## 十六、适用场景

**适合：**

- Token 计费场景（按实际使用量计费）
- 需要精确控制 Token 消耗的场景
- 突发流量较多的场景
- 需要使用率反馈的场景
- 不同用户类型需要差异化配额的场景

**不适合：**

- 简单的请求限流（建议使用固定窗口）
- 对内存占用极其敏感的场景
- Redis 不可用的场景
- 不需要精确控制 Token 消耗的场景

## 十七、未来优化方向

1. **分布式限流**：支持分布式环境下的限流
2. **动态调整**：根据系统负载动态调整限流策略
3. **智能预估**：使用 ML 模型更准确地预估 Token 消耗
4. **配额透支**：支持配额透支，但需要后续补齐
5. **配额转移**：支持用户之间转移配额
6. **配额购买**：支持用户购买额外配额
7. **配额统计**：提供详细的配额使用统计和分析

## 十八、相关文件

- [RedisConfig.java](file:///e:/编程学习/项目/yv-ai/yu-ai-agent/yu-ai-agent-master/src/main/java/com/yupi/yuaiagent/config/RedisConfig.java)
- [RedisChatMemory.java](file:///e:/编程学习/项目/yv-ai/yu-ai-agent/yu-ai-agent-master/src/main/java/com/yupi/yuaiagent/chatmemory/RedisChatMemory.java)

## 十九、总结

Token 限额方案基于 Redis 和 Lua 脚本，采用滑动窗口算法，实现了精确的 Token 消耗控制。该方案具有以下核心优势：

**技术亮点：**

- **滑动窗口算法**：精确控制时间窗口内的 Token 消耗
- **阈值百分比限流**：支持提前限流，避免用户在临界点频繁请求
- **使用率反馈**：提供详细的使用率信息，便于前端展示
- **限流原因区分**：区分"达到阈值"和"超过上限"两种情况
- **Lua 脚本原子操作**：保证限流逻辑的原子性，避免竞态条件
- **自动过期机制**：自动清理过期数据，无需手动维护

**核心优势：**

- **精确控制**：精确控制实际 Token 消耗，避免超支
- **平滑处理**：滑动窗口算法平滑处理突发流量
- **灵活配置**：支持不同用户类型的差异化配置
- **高性能**：基于 Redis + Lua，性能优异
- **易于集成**：与现有代码无缝集成

**适用场景：**

- RAG 系统的 Token 消耗限流
- 按实际使用量计费的场景
- 需要精确控制 Token 消耗的场景
- 不同用户类型需要差异化配额的场景

这是一个实用且易于维护的解决方案，通过滑动窗口算法和阈值百分比机制，实现了精确、灵活的 Token 消耗控制。
