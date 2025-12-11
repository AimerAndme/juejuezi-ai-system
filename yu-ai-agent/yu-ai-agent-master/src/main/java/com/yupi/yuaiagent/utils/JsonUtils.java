package com.yupi.yuaiagent.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 通用 JSON 工具类（基于 Jackson 实现）
 * 支持对象与 JSON 字符串互转、集合/Map 与 JSON 互转，适配 PG jsonb 字段场景
 */
public final class JsonUtils {

    // 私有化构造方法，禁止实例化
    private JsonUtils() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }

    /**
     * 全局 ObjectMapper 实例（单例，线程安全）
     * 配置：忽略 null 值、格式化输出、处理 LocalDateTime、忽略未知字段（避免反序列化报错）
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 注册 JDK8 时间模块（处理 LocalDateTime/LocalDate 等）
        JavaTimeModule timeModule = new JavaTimeModule();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dtf));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dtf));
        OBJECT_MAPPER.registerModule(timeModule);

        // 序列化配置：忽略 null 值、格式化输出 JSON（便于调试）
        OBJECT_MAPPER.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);

        // 反序列化配置：忽略 JSON 中存在但实体类没有的字段（避免反序列化报错）
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ===================== 核心方法：对象转 JSON 字符串 =====================
    /**
     * 对象转 JSON 字符串（默认配置：忽略 null、格式化输出）
     * @param obj 任意 Java 对象（POJO/Map/List 等）
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转 JSON 失败", e);
        }
    }

    /**
     * 对象转 JSON 字符串（自定义是否格式化、是否忽略 null）
     * @param obj 任意 Java 对象
     * @param pretty 是否格式化输出（true=格式化，false=压缩）
     * @param ignoreNull 是否忽略 null 值（true=忽略，false=保留）
     * @return JSON 字符串
     */
    public static String toJson(Object obj, boolean pretty, boolean ignoreNull) {
        try {
            // 临时 ObjectMapper（避免修改全局配置）
            ObjectMapper tempMapper = new ObjectMapper();
            tempMapper.registerModule(new JavaTimeModule());
            // 配置 null 值处理
            tempMapper.setSerializationInclusion(ignoreNull ? 
                    com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL : 
                    com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS);
            // 配置格式化
            if (pretty) {
                tempMapper.enable(SerializationFeature.INDENT_OUTPUT);
            } else {
                tempMapper.disable(SerializationFeature.INDENT_OUTPUT);
            }
            return tempMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转 JSON 失败", e);
        }
    }

    // ===================== 核心方法：JSON 字符串转对象 =====================
    /**
     * JSON 字符串转指定类型对象
     * @param json JSON 字符串
     * @param clazz 目标对象类型（如 User.class）
     * @param <T> 泛型
     * @return 目标类型对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 转对象失败", e);
        }
    }

    /**
     * JSON 字符串转复杂类型（如 List<User>、Map<String, Object> 等）
     * @param json JSON 字符串
     * @param typeReference 类型引用（如 new TypeReference<List<User>>() {}）
     * @param <T> 泛型
     * @return 复杂类型对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 转复杂类型失败", e);
        }
    }

    // ===================== 快捷方法：JSON 转集合/Map =====================
    /**
     * JSON 字符串转 List 集合（如 List<User>）
     * @param json JSON 字符串
     * @param elementClazz List 中元素的类型
     * @param <T> 泛型
     * @return List 集合
     */
    public static <T> List<T> jsonToList(String json, Class<T> elementClazz) {
        return fromJson(json, new TypeReference<List<T>>() {});
    }

    /**
     * JSON 字符串转 Map（默认 Map<String, Object>）
     * @param json JSON 字符串
     * @return Map 集合
     */
    public static Map<String, Object> jsonToMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * JSON 字符串转指定类型的 Map（如 Map<String, User>）
     * @param json JSON 字符串
     * @param valueClazz Map 值的类型
     * @param <V> 泛型
     * @return 指定类型的 Map
     */
    public static <V> Map<String, V> jsonToMap(String json, Class<V> valueClazz) {
        return fromJson(json, new TypeReference<Map<String, V>>() {});
    }
}