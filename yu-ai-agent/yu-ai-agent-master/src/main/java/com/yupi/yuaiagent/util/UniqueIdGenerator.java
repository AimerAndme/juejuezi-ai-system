package com.yupi.yuaiagent.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 唯一ID生成工具类：适配Agent记忆模块（memoryId、taskId、messageId等）
 * 核心规则：业务标识 + 时间戳（毫秒） + 随机串/UUID → 保证全局唯一+业务可读
 */
public class UniqueIdGenerator {

    // 时间戳偏移量（2025-01-01 00:00:00 的毫秒数），减少ID长度
    private static final long TIMESTAMP_OFFSET = 1735689600000L;
    // 随机数位数（默认6位，可调整）
    private static final int RANDOM_LENGTH = 6;
    // 最大随机数（6位：0~999999）
    private static final int MAX_RANDOM = (int) Math.pow(10, RANDOM_LENGTH) - 1;
    // 特殊字符替换（避免ID中包含非法字符）
    private static final String ILLEGAL_CHAR_REPLACER = "_";

    /**
     * 生成记忆模块专用memoryId（推荐核心方法）
     * 格式：userId_sessionId_时间戳偏移_随机串 → 如：u123_s456_123456_789012
     *
     * @param sessionId 会话ID（不可为空）
     * @return 全局唯一的memoryId（长度≈30-40位）
     */
    public static String generateMemoryId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("userId和sessionId不可为空");
        }
        // 1. 清洗非法字符（避免特殊字符影响存储）
        String cleanSessionId = cleanIllegalChars(sessionId);
        // 2. 时间戳偏移（减少ID长度，基于2025年基准）
        long timestamp = System.currentTimeMillis() - TIMESTAMP_OFFSET;
        // 3. 随机串（6位，避免同一毫秒生成重复ID）
        String randomStr = generateRandomStr(RANDOM_LENGTH);
        // 4. 组合生成ID
        return String.format("%s_%d_%s", cleanSessionId, timestamp, randomStr);
    }

    /**
     * 生成带前缀的唯一ID（通用场景）
     * 格式：前缀_时间戳偏移_UUID简化版 → 如：task_123456_abc123def456
     *
     * @param prefix 前缀（如"task"、"message"、"entity"）
     * @return 带前缀的唯一ID
     */
    public static String generatePrefixedId(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("前缀不可为空");
        }
        String cleanPrefix = cleanIllegalChars(prefix);
        long timestamp = System.currentTimeMillis() - TIMESTAMP_OFFSET;
        // UUID简化版（去除"-"，16位）
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return String.format("%s_%d_%s", cleanPrefix, timestamp, uuid);
    }

    /**
     * 生成纯随机唯一ID（无业务标识场景）
     * 格式：UUID简化版（32位）→ 如：550e8400e29b41d4a716446655440000
     *
     * @return 32位纯随机唯一ID
     */
    public static String generateRandomUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成自定义长度的随机串（数字）
     *
     * @param length 随机串长度（1-10位）
     * @return 数字随机串
     */
    public static String generateRandomStr(int length) {
        if (length < 1 || length > 10) {
            throw new IllegalArgumentException("随机串长度需在1-10位之间");
        }
        // 使用ThreadLocalRandom保证线程安全和高性能
        int randomNum = ThreadLocalRandom.current().nextInt(0, (int) Math.pow(10, length));
        // 补零到指定长度（如6位：123 → 000123）
        return String.format("%0" + length + "d", randomNum);
    }

    /**
     * 清洗ID中的非法字符（避免影响数据库存储/索引）
     * 替换规则：非字母、数字、下划线的字符 → 替换为下划线
     */
    private static String cleanIllegalChars(String str) {
        if (str == null) {
            return "";
        }
        // 正则：保留字母、数字、下划线，其他替换为下划线
        return str.replaceAll("[^a-zA-Z0-9_]", ILLEGAL_CHAR_REPLACER);
    }

    /**
     * 生成截断长度的唯一ID（谨慎使用，可能影响唯一性）
     *
     * @param originalId 原始唯一ID
     * @param maxLength  最大长度（最小16位，建议不小于20位）
     * @return 截断后的ID
     */
    public static String truncateId(String originalId, int maxLength) {
        if (originalId == null || originalId.isEmpty()) {
            throw new IllegalArgumentException("原始ID不可为空");
        }
        if (maxLength < 16) {
            throw new IllegalArgumentException("最大长度不可小于16位（否则可能影响唯一性）");
        }
        return originalId.length() <= maxLength ? originalId : originalId.substring(0, maxLength);
    }

//    // ------------------------------ 测试示例 ------------------------------
//    public static void main(String[] args) {
//        // 1. 生成记忆模块memoryId（核心使用场景）
//        String memoryId = UniqueIdGenerator.generateMemoryId("user_123", "session_456");
//        System.out.println("记忆模块memoryId：" + memoryId);
//        // 输出示例：user_123_session_456_123456_789012
//
//        // 2. 生成任务ID（带前缀）
//        String taskId = UniqueIdGenerator.generatePrefixedId("task");
//        System.out.println("任务ID：" + taskId);
//        // 输出示例：task_123456_abc123def456
//
//        // 3. 生成纯随机UUID
//        String randomUuid = UniqueIdGenerator.generateRandomUuid();
//        System.out.println("纯随机UUID：" + randomUuid);
//        // 输出示例：550e8400e29b41d4a716446655440000
//
//        // 4. 截断ID（如需限制长度）
//        String truncatedId = UniqueIdGenerator.truncateId(memoryId, 30);
//        System.out.println("截断后ID：" + truncatedId);
//        // 输出示例：user_123_session_456_123456_789012（若长度未超则不变）
//    }
}