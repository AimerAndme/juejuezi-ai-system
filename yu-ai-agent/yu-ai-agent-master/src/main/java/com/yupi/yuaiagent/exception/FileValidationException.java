package com.yupi.yuaiagent.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class FileValidationException extends RuntimeException {

    private final String fieldName;
    private final Object fieldValue;


    public FileValidationException(String fieldName, Object fieldValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;

    }

    public FileValidationException(String fieldName, Object fieldValue) {
        super(String.format("文件验证失败：字段[%s]的值[%s]不合法", fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
