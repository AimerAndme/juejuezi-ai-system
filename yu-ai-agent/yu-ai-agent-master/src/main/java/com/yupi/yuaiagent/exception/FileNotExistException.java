package com.yupi.yuaiagent.exception;

import lombok.Getter;

@Getter
public class FileNotExistException extends RuntimeException {

    private final String filePath;

    public FileNotExistException(String filePath) {
        super(String.format("文件不存在：[%s]", filePath));
        this.filePath = filePath;
    }

    public FileNotExistException(String filePath, String message) {
        super(message);
        this.filePath = filePath;
    }
}
