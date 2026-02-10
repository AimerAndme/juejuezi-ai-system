package com.yupi.yuaiagent.service.consumer;

import com.yupi.yuaiagent.config.RabbitMQConfig;
import com.yupi.yuaiagent.domin.entity.FileProcessNotification;
import com.yupi.yuaiagent.service.IFileNotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class FileNotificationConsumer {

    private final IFileNotificationService fileNotificationService;

    @RabbitListener(
            queues = RabbitMQConfig.FILE_NOTIFICATION_QUEUE,
            ackMode = "AUTO",
            concurrency = "2-3"
    )
    public void handleNotification(FileProcessNotification notification) {
        try {
            log.info("收到文件处理通知: {} - {} - {}", 
                    notification.getFileMd5(), 
                    notification.getFileName(), 
                    notification.getStatus());
            fileNotificationService.sendNotification(notification);
            log.debug("文件处理通知已发送: {} - {}", 
                    notification.getFileMd5(), 
                    notification.getStatus());
        } catch (Exception e) {
            log.error("处理文件通知失败: {} - {}", 
                    notification.getFileMd5(), 
                    e.getMessage(), e);
        }
    }
}
