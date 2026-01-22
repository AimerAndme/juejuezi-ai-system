package com.yupi.yuaiagent.service.consumer;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.rabbitmq.client.Channel;
import com.yupi.yuaiagent.config.RabbitMQConfig;
import com.yupi.yuaiagent.domin.constant.FileConstant;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import com.yupi.yuaiagent.exception.*;
import com.yupi.yuaiagent.mapper.FileUploadMapper;
import com.yupi.yuaiagent.service.FileValidationService;
import com.yupi.yuaiagent.service.VectorizationService;
import com.yupi.yuaiagent.service.parse.PDFContentExtractor;
import com.yupi.yuaiagent.service.parse.ParseService;
import com.yupi.yuaiagent.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class FileAsyncConsumer {

    private final FileUploadMapper fileUploadMapper;
    private final VectorizationService vectorizationService;
    private final ParseService parseService;
    private final PDFContentExtractor pdfContentExtractor;
    private final FileValidationService fileValidationService;

    @RabbitListener(
            queues = RabbitMQConfig.FILE_ASYNC_QUEUE,
            ackMode = "MANUAL"
    )
    public void consumeFile(
            Map<String, String> fileInfo,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag
    ) {
        String fileMd5 = fileInfo.get("fileMd5");
        String userId = fileInfo.get("userId");

        try {
            fileValidationService.validateFileInfo(fileInfo);

            FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
            fileValidationService.validateFileUpload(fileUpload);

            try (InputStream fileStream = downloadFileByFileMd5(fileUpload)) {

                byte[] fileContentBytes = StreamUtils.copyToByteArray(fileStream);
                fileValidationService.validateFileContent(fileUpload.getFileName(), fileContentBytes);

                try (InputStream independentFileStream = new ByteArrayInputStream(fileContentBytes)) {
                    parseService.parseAndSave(fileMd5, independentFileStream,
                            fileUpload.getUserId(), fileUpload.getOrgTag(), fileUpload.getIsPublic());
                    vectorizationService.vectorize(fileMd5, fileUpload.getUserId(),
                            fileUpload.getOrgTag(), fileUpload.getIsPublic());
                    log.info("文件向量化已结束");
                    channel.basicAck(deliveryTag, false);
                } catch (IOException | TikaException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (FileValidationException | FileNotExistException | FileFormatException | FileContentException |
                 IOException | NoApiKeyException | UploadFileException e) {
            log.error("业务异常，消息进入死信队列：{}", e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("basicNack 失败", ex);
                throw new RuntimeException(ex);
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
            String s = pdfContentExtractor.extractMixedContentWithVlm(fileUpload, filePath, imagesPath, imagesPath);
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
}
