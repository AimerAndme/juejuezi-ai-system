package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.FileProcessNotification;
import com.yupi.yuaiagent.service.IFileNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class FileNotificationService implements IFileNotificationService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 600000L;

    @Override
    public SseEmitter subscribe(String userId, String fileMd5) {
        String key = buildKey(userId, fileMd5);

        if (emitters.containsKey(key)) {
            emitters.get(key).complete();
            emitters.remove(key);
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(key, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE 连接完成: {}", key);
            emitters.remove(key);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE 连接超时: {}", key);
            emitters.remove(key);
        });

        emitter.onError((e) -> {
            log.error("SSE 连接错误: {}", key, e);
            emitters.remove(key);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE 连接成功")
                    .build());
        } catch (IOException e) {
            log.error("发送连接成功消息失败: {}", key, e);
            emitters.remove(key);
        }

        log.info("用户 {} 订阅文件 {} 处理通知", userId, fileMd5);
        return emitter;
    }

    @Override
    public void sendNotification(FileProcessNotification notification) {
        String key = buildKey(notification.getUserId(), notification.getFileMd5());
        SseEmitter emitter = emitters.get(key);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification)
                        .build());

                log.info("发送文件处理通知: {} - {} - 进度: {}%", key, notification.getStatus(), notification.getProgress());

                if ("SUCCESS".equals(notification.getStatus()) || "FAILED".equals(notification.getStatus())) {
                    log.info("检测到完成状态，延迟关闭连接以等待前端接收消息: {}", key);
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            complete(notification.getUserId(), notification.getFileMd5());
                        } catch (InterruptedException e) {
                            log.error("延迟关闭连接被中断: {}", key, e);
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }
            } catch (IOException e) {
                log.error("发送通知失败: {}", key, e);
                emitters.remove(key);
            }
        } else {
            log.warn("未找到对应的 SSE 连接: {}", key);
        }
    }

    @Override
    public void complete(String userId, String fileMd5) {
        String key = buildKey(userId, fileMd5);
        SseEmitter emitter = emitters.get(key);

        if (emitter != null) {
            try {
                emitter.complete();
                log.debug("SSE 连接完成: {}", key);
            } catch (Exception e) {
                log.error("完成 SSE 连接失败: {}", key, e);
            } finally {
                emitters.remove(key);
            }
        }
    }

    @Override
    public void remove(String userId, String fileMd5) {
        String key = buildKey(userId, fileMd5);
        emitters.remove(key);
        log.debug("移除 SSE 连接: {}", key);
    }

    private String buildKey(String userId, String fileMd5) {
        return userId + ":" + fileMd5;
    }
}
