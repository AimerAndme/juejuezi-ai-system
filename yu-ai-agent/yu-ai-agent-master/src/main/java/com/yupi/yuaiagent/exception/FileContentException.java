package com.yupi.yuaiagent.exception;

import lombok.Getter;

@Getter
public class FileContentException extends RuntimeException {

    private final String fileName;
    private final String reason;

    public FileContentException(String fileName, String reason) {
        super(String.format("文件内容异常：文件[%s]，原因[%s]", fileName, reason));
        this.fileName = fileName;
        this.reason = reason;
    }

    public FileContentException(String fileName, String reason, String message) {
        super(message);
        this.fileName = fileName;
        this.reason = reason;
    }
}
