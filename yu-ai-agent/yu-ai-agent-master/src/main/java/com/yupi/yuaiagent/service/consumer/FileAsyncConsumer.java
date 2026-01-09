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
import com.yupi.yuaiagent.service.ParseService;
import com.yupi.yuaiagent.service.VectorizationService;
import com.yupi.yuaiagent.util.PDFContentExtractor;
import com.yupi.yuaiagent.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class FileAsyncConsumer {

    private final FileUploadMapper fileUploadMapper;
    private final VectorizationService vectorizationService;
    private final ParseService parseService;
    private final PDFContentExtractor pdfContentExtractor;

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
        //文件信息校验
        FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
        if (Objects.isNull(fileUpload) || !fileUpload.getIsPublic()) {
            log.error("当前文件无法被向量化!");
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        try (InputStream fileStream = downloadFileByFileMd5(fileUpload)) {
            if (fileStream == null) {
                throw new IOException("流为空");
            }
            byte[] fileContentBytes = StreamUtils.copyToByteArray(fileStream);
            try (InputStream independentFileStream = new ByteArrayInputStream(fileContentBytes)) {
                //文件分片入库
                parseService.parseAndSave(fileMd5, independentFileStream,
                        fileUpload.getUserId(), fileUpload.getOrgTag(), fileUpload.getIsPublic());
                //向量化
                vectorizationService.vectorize(fileMd5, fileUpload.getUserId(),
                        fileUpload.getOrgTag(), fileUpload.getIsPublic());
                log.info("文件向量化已结束");
                channel.basicNack(deliveryTag, false, false);
            }
        } catch (Exception e) {
            log.error("文件在消费端向量化操作失败！", e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("basicNack 失败", ex);
                throw new RuntimeException(ex);
            }
            throw new RuntimeException("Error processing task", e);
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
        String filePath = FileUtils.findFileByName(FileConstant.FILE_UPLOAD_SAVE_DIR_, fileName); // 替换为你的文件路径
        String imagesPath = FileConstant.IMAGES_UPLOAD_SAVE_DIR_;
        if (filePath == null) {
            log.error("文件不存在");
            throw new RuntimeException("文件不存在");
        }
        //判断文件类型
        if (fileName.endsWith(".pdf")) {
            //如果是PDF文件，进行pdf的处理,
            String s = pdfContentExtractor.extractMixedContentWithVlm(fileUpload, filePath, imagesPath, imagesPath);
            log.info("文件：{}，已成功处理，", filePath);
            return new ByteArrayInputStream(s.getBytes());
        }
        // 使用 try-with-resources 确保 InputStream 被正确关闭
        try {
            // 现在你可以使用 inputStream 了
            // 例如，读取字节:
            InputStream inputStream = new FileInputStream(filePath);
            log.info("文件：{}，已成功读取，", filePath);
            // 或者读取到字节数组:
            // byte[] buffer = new byte[1024];
            // int bytesRead;
            // while ((bytesRead = inputStream.read(buffer)) != -1) {
            //     // 处理 buffer 中的 bytesRead 个字节
            // }
            return inputStream;
        } catch (IOException e) {
            System.err.println("读取文件时发生错误: " + e.getMessage());
            throw new BusinessException(ErrorCode.FILE_PARSE_ERROR);
        }
    }
}
