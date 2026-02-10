package com.yupi.yuaiagent.domin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileProcessNotification implements Serializable {

    private String fileMd5;

    private String fileName;

    private String userId;

    private String status;

    private Integer progress;

    private String message;

    private LocalDateTime timestamp;

    private String orgTag;

    private Boolean isPublic;
    private List<Map<Integer, String>> errorTextList;
    private List<Map<Integer, String>> errorImageList;

    public static FileProcessNotification create(String fileMd5, String fileName, String userId, String status, Integer progress, String message) {
        FileProcessNotification notification = new FileProcessNotification();
        notification.setFileMd5(fileMd5);
        notification.setFileName(fileName);
        notification.setUserId(userId);
        notification.setStatus(status);
        notification.setProgress(progress);
        notification.setMessage(message);
        notification.setTimestamp(LocalDateTime.now());
        return notification;
    }
}
