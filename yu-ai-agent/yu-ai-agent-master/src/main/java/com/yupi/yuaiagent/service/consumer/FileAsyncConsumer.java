package com.yupi.yuaiagent.service.consumer;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.rabbitmq.client.Channel;
import com.yupi.yuaiagent.config.RabbitMQConfig;
import com.yupi.yuaiagent.domin.constant.FileConstant;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import com.yupi.yuaiagent.exception.BusinessException;
import com.yupi.yuaiagent.exception.ErrorCode;
import com.yupi.yuaiagent.mapper.FileUploadMapper;
import com.yupi.yuaiagent.service.FileValidationService;
import com.yupi.yuaiagent.service.VectorizationService;
import com.yupi.yuaiagent.service.parse.PDFContentExtractor;
import com.yupi.yuaiagent.service.parse.ParseService;
import com.yupi.yuaiagent.utils.FileUtils;
import com.yupi.yuaiagent.utils.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@AllArgsConstructor
public class FileAsyncConsumer {

    private static final String nodupKey = "mq:nodup:";
    private static final int expireTime = 10000;
    private final FileUploadMapper fileUploadMapper;
    private final VectorizationService vectorizationService;
    private final ParseService parseService;
    private final PDFContentExtractor pdfContentExtractor;
    private final FileValidationService fileValidationService;
    private final TaskExecutor businessTaskExecutor;
    private final RedisUtils redisUtils;

    @RabbitListener(
            queues = RabbitMQConfig.FILE_ASYNC_QUEUE,
            ackMode = "MANUAL",
            concurrency = "3-5" // MQ 拉取线程池大小
    )
    public void consumeFile(
            Map<String, String> fileInfo,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag
    ) {
        String fileMd5 = fileInfo.get("fileMd5");
        String userId = fileInfo.get("userId");
        String key = nodupKey + fileMd5 + ":" + userId;
        try {
            boolean lock = redisUtils.tryLock(key, expireTime);
            if (!lock) {
                log.info("文件正在处理中，请稍等...");
                channel.basicAck(deliveryTag, false);
                return;
            }
            // MQ 拉取线程：快速验证消息和文件信息
            fileValidationService.validateFileInfo(fileInfo);
            FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
            fileValidationService.validateFileUpload(fileUpload);
            // 提交给业务线程池处理，MQ 线程立即返回
            CompletableFuture.runAsync(() -> {
                try {
                    // 业务线程：处理耗时操作
                    processFile(fileUpload, fileMd5, channel, deliveryTag);
                } catch (Exception e) {
                    log.error("文件处理失败，消息进入死信队列：{}", e.getMessage(), e);
                    // 处理失败，发送 NACK
                    try {
                        channel.basicNack(deliveryTag, false, false);
                    } catch (IOException ex) {
                        log.error("basicNack 失败", ex);
                    }
                }
            }, businessTaskExecutor);
            log.debug("文件处理任务已提交到业务线程池，MQ 线程继续拉取下一条消息");
        } catch (Exception e) {
            // MQ 线程中的验证异常，直接 NACK
            log.error("消息验证失败，消息进入死信队列：{}", e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("basicNack 失败", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * 业务线程处理耗时操作
     */
    private void processFile(FileUpload fileUpload, String fileMd5, Channel channel, Long deliveryTag) throws Exception {
        log.info("开始处理文件：{}", fileUpload.getFileName());

        try (InputStream fileStream = downloadFileByFileMd5(fileUpload)) {
            // 文件下载
            byte[] fileContentBytes = StreamUtils.copyToByteArray(fileStream);
            fileValidationService.validateFileContent(fileUpload.getFileName(), fileContentBytes);

            // 文件解析和向量化
            try (InputStream independentFileStream = new ByteArrayInputStream(fileContentBytes)) {
                // 数据库入库
                parseService.parseAndSave(fileMd5, independentFileStream,
                        fileUpload.getUserId(), fileUpload.getOrgTag(), fileUpload.getIsPublic());

                // 向量化处理
                vectorizationService.vectorize(fileMd5, fileUpload.getUserId(),
                        fileUpload.getOrgTag(), fileUpload.getIsPublic());

                log.info("文件向量化已结束：{}", fileUpload.getFileName());

                // 处理完成，发送 ACK
                channel.basicAck(deliveryTag, false);
                log.debug("文件处理完成，已发送 ACK：{}", fileMd5);
            } catch (IOException | TikaException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 读取文件为输入流
     *
     * @param fileUpload
     * @return
     */
    private InputStream downloadFileByFileMd5(FileUpload fileUpload) throws IOException, NoApiKeyException, UploadFileException {
        String fileName = fileUpload.getFileMd5() + "_" + fileUpload.getFileName();
        String filePath = FileUtils.findFileByName(FileConstant.FILE_UPLOAD_SAVE_DIR_, fileName);
        String imagesPath = FileConstant.IMAGES_UPLOAD_SAVE_DIR_;

        fileValidationService.validateFilePath(filePath);
        fileValidationService.validateFileExists(filePath);
        fileValidationService.validateFileExtension(fileName);

        if (fileName.endsWith(".pdf")) {
            String s = pdfContentExtractor.extractMixedContentWithVlmCur(fileUpload, filePath, imagesPath, imagesPath);
            log.info("文件：{}，已成功处理", filePath);
            return new ByteArrayInputStream(s.getBytes());
        }

        try {
            InputStream inputStream = new FileInputStream(filePath);
            log.info("文件：{}，已成功读取", filePath);
            return inputStream;
        } catch (IOException e) {
            log.error("读取文件时发生错误: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_PARSE_ERROR);
        }
    }

    /**
     * 业务线程池配置
     */
    @Configuration
    static class TaskExecutorConfig {
        @Bean(name = "businessTaskExecutor")
        public TaskExecutor businessTaskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(5); // 核心线程数
            executor.setMaxPoolSize(10); // 最大线程数
            executor.setQueueCapacity(50); // 队列容量
            executor.setKeepAliveSeconds(60); // 线程空闲时间
            executor.setThreadNamePrefix("business-task-executor-"); // 线程名称前缀
            executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略
            executor.initialize();
            return executor;
        }
    }
}
