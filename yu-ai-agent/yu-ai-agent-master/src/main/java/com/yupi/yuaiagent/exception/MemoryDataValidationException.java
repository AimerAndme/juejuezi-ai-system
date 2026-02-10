package com.yupi.yuaiagent.exception;

import lombok.Getter;

@Getter
public class MemoryDataValidationException extends RuntimeException {

    private final String fieldName;
    private final Object fieldValue;

    public MemoryDataValidationException(String fieldName, Object fieldValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public MemoryDataValidationException(String fieldName, Object fieldValue) {
        super(String.format("数据验证失败：字段[%s]的值[%s]不合法", fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
