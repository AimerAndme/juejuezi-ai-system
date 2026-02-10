package com.yupi.yuaiagent.service.parse;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import com.yupi.yuaiagent.client.VlmClient;
import com.yupi.yuaiagent.domin.constant.FileConstant;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@AllArgsConstructor
public class WordContentExtractor {

    private final VlmClient vlmClient;

    @Resource(name = "pdfPageExecutor")
    private ThreadPoolTaskExecutor pdfPageExecutor;

    @ExecutionTimeMonitor
    public String extractWordContentWithVlm(
            FileUpload fileUpload,
            String wordPath,
            String imageOutputDir,
            String baseUrl
    ) throws IOException, NoApiKeyException, UploadFileException, TikaException {
        long startTime = System.currentTimeMillis();
        String fileName = fileUpload.getFileName();
        String fileMd5 = fileUpload.getFileMd5();

        log.info("开始处理Word文档 - 文件: {}", fileName);

        WordExtractResult result = extractTextAndImages(wordPath, fileMd5, imageOutputDir, baseUrl);

        if (!result.getImageUrls().isEmpty()) {
            log.info("开始并发识别图片，共 {} 张", result.getImageUrls().size());

            List<CompletableFuture<String>> futures = result.getImageUrls().stream()
                    .map(url -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return image2Text(url);
                        } catch (Exception e) {
                            log.error("图片识别失败: {}", url, e);
                            return "";
                        }
                    }, pdfPageExecutor))
                    .toList();

            List<String> imageTexts = futures.stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            log.error("获取图片识别结果失败", e);
                            return "";
                        }
                    })
                    .filter(text -> !text.isEmpty())
                    .toList();

            log.info("图片识别完成，成功识别 {} 张", imageTexts.size());

            StringBuilder finalContent = new StringBuilder(result.getText());
            for (String imageText : imageTexts) {
                finalContent.append("\n").append(imageText);
            }

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.info("Word文档处理完成 - 文件: {}, 提取图片数: {}, 耗时: {}ms",
                    fileName, result.getImageUrls().size(), executionTime);

            return finalContent.toString();
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        log.info("Word文档处理完成 - 文件: {}, 无图片, 耗时: {}ms", fileName, executionTime);

        return result.getText();
    }

    public String extractWordContentSimple(String wordPath) throws IOException, TikaException, SAXException {
        log.info("使用简单模式处理Word文档: {}", wordPath);

        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (FileInputStream fis = new FileInputStream(wordPath)) {
            parser.parse(fis, handler, metadata, context);
            String content = handler.toString();

            log.info("Word文档提取完成，内容长度: {} 字符", content.length());
            return content;
        }
    }

    private WordExtractResult extractTextAndImages(
            String wordPath,
            String fileMd5,
            String imageOutputDir,
            String baseUrl
    ) throws IOException, TikaException {
        File wordFile = new File(wordPath);
        File outputDir = new File(imageOutputDir);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String textContent = extractTextWithTika(wordPath);

        List<String> imageUrls = extractImagesWithPOI(wordPath, fileMd5, outputDir, baseUrl);

        return new WordExtractResult(textContent, imageUrls);
    }

    private String extractTextWithTika(String wordPath) throws IOException, TikaException {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (FileInputStream fis = new FileInputStream(wordPath)) {
            parser.parse(fis, handler, metadata, context);
            return handler.toString();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> extractImagesWithPOI(
            String wordPath,
            String fileMd5,
            File outputDir,
            String baseUrl
    ) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(wordPath);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFPictureData> pictures = document.getAllPictures();
            int imageCounter = 0;

            for (XWPFPictureData picture : pictures) {
                String suffix = picture.suggestFileExtension();
                if (suffix == null || suffix.isEmpty()) {
                    suffix = "png";
                }

                String imageFileName = fileMd5 + FileConstant.IMAGES_UPLOADS_PREFIX + (imageCounter++) + "." + suffix;
                File imageFile = new File(outputDir, imageFileName);

                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    fos.write(picture.getData());
                }

                String imageUrl = baseUrl + "/" + imageFileName;
                imageUrls.add(imageUrl);

                log.debug("成功提取图片: {}", imageFile.getAbsolutePath());
            }
        }

        return imageUrls;
    }

    private String image2Text(String imageUrl) throws NoApiKeyException, UploadFileException {
        String imageText = null;
        try {
            imageText = vlmClient.image2text(imageUrl);
            log.info("图片转文字成功: {}", imageUrl);
            if (imageText == null) {
                log.info("图片转文字,响应为空内容: {}", imageUrl);
            }
        } catch (Exception e) {
            log.error("图片转文字调用失败: {}", imageUrl, e);
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                throw new org.springframework.retry.RetryException("客户端错误，不重试: " + e.getMessage());
            }
            throw e;
        }
        return imageText;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WordExtractResult {
        private String text;
        private List<String> imageUrls;
    }
}
