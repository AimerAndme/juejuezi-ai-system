package com.yupi.yuaiagent.exception;

import lombok.Getter;

@Getter
public class MemoryFormatException extends RuntimeException {

    private final String fieldName;
    private final String expectedFormat;
    private final String actualValue;

    public MemoryFormatException(String fieldName, String expectedFormat, String actualValue) {
        super(String.format("数据格式错误：字段[%s]期望格式[%s]，实际值[%s]", fieldName, expectedFormat, actualValue));
        this.fieldName = fieldName;
        this.expectedFormat = expectedFormat;
        this.actualValue = actualValue;
    }

    public MemoryFormatException(String fieldName, String expectedFormat, String actualValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.expectedFormat = expectedFormat;
        this.actualValue = actualValue;
    }
}
