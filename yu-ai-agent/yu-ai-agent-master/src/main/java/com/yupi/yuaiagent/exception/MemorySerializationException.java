package com.yupi.yuaiagent.exception;

import lombok.Getter;

@Getter
public class MemorySerializationException extends RuntimeException {

    private final String operation;
    private final String data;

    public MemorySerializationException(String operation, String data, String message) {
        super(message);
        this.operation = operation;
        this.data = data;
    }

    public MemorySerializationException(String operation, String data, Throwable cause) {
        super(String.format("序列化操作失败：操作[%s]，数据[%s]", operation, data), cause);
        this.operation = operation;
        this.data = data;
    }

    public MemorySerializationException(String operation, String data, String message, Throwable cause) {
        super(message, cause);
        this.operation = operation;
        this.data = data;
    }
}
