package com.yupi.yuaiagent.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class FileParseException extends RuntimeException {

    private final String fieldName;
    private final Object fieldValue;
    private final List<Map<Integer, String>> errorTextList;
    private final List<Map<Integer, String>> errorImageList;



    public FileParseException(String fieldName, Object fieldValue, String message, List<Map<Integer, String>> errorTextList, List<Map<Integer, String>> errorImageList) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.errorTextList = errorTextList;
        this.errorImageList = errorImageList;
    }

    public FileParseException(String fieldName, Object fieldValue, List<Map<Integer, String>> errorTextList, List<Map<Integer, String>> errorImageList) {
        super(String.format("文件验证失败：字段[%s]的值[%s]不合法", fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.errorTextList = errorTextList;
        this.errorImageList = errorImageList;
    }
}
