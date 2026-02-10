package com.yupi.yuaiagent.exception;

import lombok.Getter;

@Getter
public class FileFormatException extends RuntimeException {

    private final String fileName;
    private final String fileExtension;
    private final String reason;

    public FileFormatException(String fileName, String fileExtension, String reason) {
        super(String.format("文件格式错误：文件[%s]，扩展名[%s]，原因[%s]", fileName, fileExtension, reason));
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.reason = reason;
    }

    public FileFormatException(String fileName, String fileExtension, String reason, String message) {
        super(message);
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.reason = reason;
    }
}
