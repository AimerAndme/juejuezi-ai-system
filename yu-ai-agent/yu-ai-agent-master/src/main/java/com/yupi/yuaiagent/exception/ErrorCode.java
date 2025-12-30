package com.yupi.yuaiagent.exception;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    REQUEST_TIMEOUT(408, "请求超时"),
    // 业务错误 4xx
    BUSINESS_ERROR(4000, "业务处理失败"),
    USER_NOT_FOUND(4001, "用户不存在"),
    USER_ALREADY_EXISTS(4002, "用户已存在"),
    PASSWORD_ERROR(4003, "密码错误"),
    TOKEN_INVALID(4004, "Token无效"),
    TOKEN_EXPIRED(4005, "Token已过期"),
    // 文件相关错误 4xx
    FILE_NOT_FOUND(4100, "文件不存在"),
    FILE_UPLOAD_ERROR(4101, "文件上传失败"),
    FILE_SIZE_EXCEEDED(4102, "文件大小超过限制"),
    FILE_TYPE_ERROR(4103, "文件类型不支持"),
    FILE_PARSE_ERROR(4104, "文件解析失败"),
    // 数据库错误 4xx
    DATABASE_ERROR(4200, "数据库操作失败"),
    DATA_NOT_FOUND(4201, "数据不存在"),
    DATA_ALREADY_EXISTS(4202, "数据已存在"),
    // 服务端错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    // AI相关错误 5xx
    AI_SERVICE_ERROR(5100, "AI服务调用失败"),
    AI_RESPONSE_ERROR(5101, "AI响应解析失败"),
    EMBEDDING_ERROR(5102, "向量化处理失败"),
    // 第三方服务错误 5xx
    ELASTICSEARCH_ERROR(5200, "Elasticsearch服务异常"),
    REDIS_ERROR(5201, "Redis服务异常"),
    RABBITMQ_ERROR(5202, "RabbitMQ服务异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
