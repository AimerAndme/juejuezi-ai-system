package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.FileProcessNotification;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IFileNotificationService {

    SseEmitter subscribe(String userId, String fileMd5);

    void sendNotification(FileProcessNotification notification);

    void complete(String userId, String fileMd5);

    void remove(String userId, String fileMd5);
}
