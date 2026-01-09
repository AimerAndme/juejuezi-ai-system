package com.yupi.yuaiagent.util;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.yupi.yuaiagent.client.VlmClient;
import com.yupi.yuaiagent.domin.entity.FileExtractedImages;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import com.yupi.yuaiagent.service.IFileExtractedImagesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class PDFContentExtractor {

    private final IFileExtractedImagesService fileExtractedImagesService;
    private final VlmClient vlmClient;

    /**
     * 检测和提取页面上的表格内容
     *
     * @param document  PDF文档
     * @param pageIndex 页面索引
     * @return 表格内容列表
     */
    private static List<String> detectAndExtractTables(PDDocument document, int pageIndex) throws IOException {
        List<String> tables = new ArrayList<>();

        // 这是一个简化的表格检测实现
        // 实际项目中可能需要更复杂的表格检测算法
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);

        // 获取文本位置信息，用于检测表格
        CustomPDFTextStripper customStripper = new CustomPDFTextStripper();
        customStripper.setStartPage(pageIndex + 1);
        customStripper.setEndPage(pageIndex + 1);

        try {
            customStripper.getText(document);
        } catch (IOException e) {
            // 如果无法获取文本位置信息，跳过表格检测
            return tables;
        }

        // 简单的表格检测逻辑：查找具有相同X坐标对齐的文本行
        List<TextPosition> positions = customStripper.getTextPositions();

        // 按Y坐标分组文本位置
        Map<Float, List<TextPosition>> lines = new HashMap<>();
        for (TextPosition pos : positions) {
            float y = Math.round(pos.getY() * 100.0f) / 100.0f; // 四舍五入到小数点后两位
            lines.computeIfAbsent(y, k -> new ArrayList<>()).add(pos);
        }

        // 检查每行是否有多个文本元素，这可能是表格的特征
        List<List<TextPosition>> potentialTableLines = new ArrayList<>();
        for (Map.Entry<Float, List<TextPosition>> entry : lines.entrySet()) {
            List<TextPosition> line = entry.getValue();
            // 如果一行中有多个文本元素，可能是表格的一行
            if (line.size() > 1) {
                // 检查这些文本元素是否在水平方向上分布（类似表格列）
                Collections.sort(line, Comparator.comparing(TextPosition::getX));

                // 检查文本之间的间距，判断是否为表格
                boolean isPotentialTableRow = true;
                for (int j = 1; j < line.size(); j++) {
                    float distance = line.get(j).getX() - line.get(j - 1).getX();
                    // 如果文本之间的距离适中，可能构成表格
                    if (distance < 20 || distance > 500) {
                        isPotentialTableRow = false;
                        break;
                    }
                }

                if (isPotentialTableRow) {
                    potentialTableLines.add(line);
                }
            }
        }

        // 如果有多行具有相似的文本分布模式，则认为是表格
        if (potentialTableLines.size() >= 2) {
            StringBuilder tableBuilder = new StringBuilder();
            tableBuilder.append("TABLE CONTENT:\n");

            for (List<TextPosition> line : potentialTableLines) {
                for (int j = 0; j < line.size(); j++) {
                    TextPosition pos = line.get(j);
                    tableBuilder.append(pos.getUnicode().trim());
                    if (j < line.size() - 1) {
                        tableBuilder.append(" | "); // 表格分隔符
                    }
                }
                tableBuilder.append("\n");
            }

            tables.add(tableBuilder.toString());
        }

        return tables;
    }

    /**
     * 提取PDF的文本和图片
     *
     * @param pdfPath        PDF文件路径
     * @param imageOutputDir 图片保存的目录路径
     * @param baseUrl        图片的基础URL (例如: "http://yourserver.com/images/")
     * @return 包含文本内容和图片URL列表的 ExtractionResult
     * @throws IOException
     */
    public ExtractionResult extractTextAndImages(String pdfPath, String imageOutputDir, String baseUrl) throws IOException {
        File pdfFile = new File(pdfPath);
        String textContent;
        List<String> imageUrls = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            int pageCount = document.getNumberOfPages();

            // 1. 提取文本
            PDFTextStripper stripper = new PDFTextStripper();
            // 如果需要按页处理，可以循环设置 setStartPage 和 setEndPage
            textContent = stripper.getText(document);

            // 2. 提取图片
            File outputDir = new File(imageOutputDir);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            int imageCounter = 0;
            for (int i = 0; i < pageCount; i++) {
                PDPage page = document.getPage(i);
                PDResources resources = page.getResources();

                if (resources != null && resources.getXObjectNames() != null) {
                    for (Object xObjectKey : resources.getXObjectNames()) {
                        PDXObject xObject = resources.getXObject((org.apache.pdfbox.cos.COSName) xObjectKey);

                        if (xObject instanceof PDImageXObject) {
                            PDImageXObject image = (PDImageXObject) xObject;

                            String suffix = image.getSuffix(); // 获取图片格式 (jpg, png, etc.)
                            if (suffix == null) {
                                suffix = "png"; // 默认格式
                            }
                            String imageFileName = "extracted_image_page_" + (i + 1) + "_" + (imageCounter++) + "." + suffix;
                            File imageFile = new File(outputDir, imageFileName);

                            // 读取图片数据
                            BufferedImage awtImage = image.getImage();
                            if (awtImage != null) {
                                // 保存图片到本地
                                ImageIO.write(awtImage, suffix.toUpperCase(), imageFile);
                                // 生成图片URL
                                String imageUrl = baseUrl + imageFileName;
                                imageUrls.add(imageUrl);
                                System.out.println("Extracted image to: " + imageFile.getAbsolutePath() + ", URL: " + imageUrl);
                            } else {
                                System.out.println("Could not extract image data for object: " + xObjectKey);
                            }
                        }
                        // 可以根据需要处理其他类型的XObject (如PDFormXObject)
                    }
                }
            }
        }

        return new ExtractionResult(textContent, imageUrls);
    }

    /**
     * 提取PDF的文本、图片和表格，并在文本中保留它们的位置标识
     *
     * @param pdfPath        PDF文件路径
     * @param imageOutputDir 图片保存的目录路径
     * @param baseUrl        图片的基础URL (例如: "http://yourserver.com/images/")
     * @return 包含混合内容、图片URL列表和表格内容列表的 MixedContentResult
     * @throws IOException
     */
    public MixedContentResult extractMixedContent(String fileMd5, String pdfPath, String imageOutputDir, String baseUrl) throws IOException {
        File pdfFile = new File(pdfPath);
        StringBuilder mixedContent = new StringBuilder();
        List<String> imageUrls = new ArrayList<>();
        List<String> tableContents = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            int pageCount = document.getNumberOfPages();

            File outputDir = new File(imageOutputDir);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            int imageCounter = 0;
            int tableCounter = 0;

            for (int i = 0; i < pageCount; i++) {
                PDPage page = document.getPage(i);

                // 提取页面文本
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String pageText = stripper.getText(document);

                // 添加页面文本到混合内容
                mixedContent.append(pageText);

                // 获取页面资源并查找图片
                PDResources resources = page.getResources();
                if (resources != null && resources.getXObjectNames() != null) {
                    for (Object xObjectKey : resources.getXObjectNames()) {
                        PDXObject xObject = resources.getXObject((org.apache.pdfbox.cos.COSName) xObjectKey);

                        if (xObject instanceof PDImageXObject) {
                            PDImageXObject image = (PDImageXObject) xObject;

                            String suffix = image.getSuffix();
                            if (suffix == null) {
                                suffix = "png";
                            }
                            String imageFileName = "extracted_image_page_" + (i + 1) + "_" + (imageCounter++) + "." + suffix;
                            File imageFile = new File(outputDir, imageFileName);

                            // 读取图片数据
                            BufferedImage awtImage = image.getImage();
                            if (awtImage != null) {
                                //建表：保存对应的文件id，图片id，大模型识别内容
                                // 保存图片到本地
                                ImageIO.write(awtImage, suffix.toUpperCase(), imageFile);
                                // 生成图片URL
                                String imageUrl = baseUrl + imageFileName;
                                imageUrls.add(imageUrl);

                                // 在文本中插入图片占位符，保持位置信息
                                mixedContent.append("\n[IMAGE: ").append(imageUrl).append("]\n");
                                log.info("Image saved: {}", imageFile.getAbsolutePath());
                            } else {
                                log.warn("Failed to save image: {}", imageFile.getAbsolutePath());
                            }
                        }
                    }
                }

                // 尝试检测和提取表格
                List<String> pageTables = detectAndExtractTables(document, i);
                for (String tableContent : pageTables) {
                    String tablePlaceholder = "[TABLE_" + (tableCounter++) + ": " + tableContent.substring(0, Math.min(50, tableContent.length())) + "...]";
                    tableContents.add(tableContent);
                    mixedContent.append("\n").append(tablePlaceholder).append("\n");
                }
            }
        }

        return new MixedContentResult(mixedContent.toString(), imageUrls, tableContents);
    }

    /**
     * 提取PDF的文本、图片和表格，使用vlm大模型进行图片的处理
     *
     * @param pdfPath        PDF文件路径
     * @param imageOutputDir 图片保存的目录路径
     * @param baseUrl        图片的基础URL (例如: "http://yourserver.com/images/")
     * @return 包含混合内容、图片URL列表和表格内容列表的 MixedContentResult
     * @throws IOException
     */
    public String extractMixedContentWithVlm(FileUpload fileUpload, String pdfPath, String imageOutputDir, String baseUrl) throws IOException, NoApiKeyException, UploadFileException {
        String userId = fileUpload.getUserId();
        File pdfFile = new File(pdfPath);
        StringBuilder mixedContent = new StringBuilder();
        List<String> imageUrls = new ArrayList<>();
        List<String> tableContents = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            int pageCount = document.getNumberOfPages();

            File outputDir = new File(imageOutputDir);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            int imageCounter = 0;
            int tableCounter = 0;

            for (int i = 0; i < pageCount; i++) {
                PDPage page = document.getPage(i);

                // 提取页面文本
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String pageText = "";
                try {
                    pageText = stripper.getText(document);
                } catch (IOException e) {
                    log.warn("提取第 {} 页文本时出错，可能是字体问题: {}", i + 1, e.getMessage());
                    // 继续处理下一页
                    continue;
                }
                // 添加页面文本到混合内容
                mixedContent.append(pageText);
                // 获取页面资源并查找图片
                PDResources resources = page.getResources();
                if (resources != null && resources.getXObjectNames() != null) {
                    for (Object xObjectKey : resources.getXObjectNames()) {
                        PDXObject xObject = resources.getXObject((org.apache.pdfbox.cos.COSName) xObjectKey);

                        if (xObject instanceof PDImageXObject) {
                            PDImageXObject image = (PDImageXObject) xObject;

                            String suffix = image.getSuffix();
                            if (suffix == null) {
                                suffix = "png";
                            }
                            //图片名称
                            String imageFileName = "extracted_image_page_" + (i + 1) + "_" + (imageCounter++) + "." + suffix;
                            File imageFile = new File(outputDir, imageFileName);

                            // 读取图片数据
                            BufferedImage awtImage = null;
                            try {
                                awtImage = image.getImage();
                            } catch (IOException e) {
                                log.warn("无法提取图片数据，可能是由于ICC颜色空间问题: {}", e.getMessage());
                                continue; // 跳过这个图片对象，继续处理其他图片
                            }
                            if (awtImage != null) {
                                // 保存图片到本地
                                try {
                                    ImageIO.write(awtImage, suffix.toUpperCase(), imageFile);
                                    log.debug("成功保存图片到: {}", imageFile.getAbsolutePath());
                                } catch (IOException e) {
                                    log.error("保存图片失败: {}", imageFile.getAbsolutePath(), e);
                                    continue; // 跳过此图片的后续处理
                                }

                                // 生成图片URL
                                String imageUrl = baseUrl + "/" + imageFileName;
                                imageUrls.add(imageUrl);

                                //建表：保存对应的文件id，图片id，大模型识别内容
                                //1、视觉模型提取文字
                                log.info("开始图片转文字: {}", imageUrl);
                                String imageText = image2Text(imageUrl);
                                //2、数据库入库
                                try {
                                    FileExtractedImages fileExtractedImages = new FileExtractedImages();
                                    fileExtractedImages
                                            .setFileMd5(fileUpload.getFileMd5())
                                            .setUserId(userId)
                                            .setImagePath(imageFileName)
                                            .setPageNumber(i + 1)
                                            .setVlModelName("vlm")
                                            .setVlModelVersion("1.0")
                                            .setVlResultText(imageText);
                                    fileExtractedImagesService.addFileExtractedImages(fileExtractedImages);
                                    log.info("图片转文字成功入库: {}", imageFileName);
                                } catch (Exception e) {
                                    log.error("图片信息入库失败: {}", imageFileName, e);
                                    // 根据项目规范，落库失败需要抛出异常以触发重试
                                    throw new RuntimeException("图片信息入库失败: " + imageFileName, e);
                                }
                                //3、保存 图片在文本中插入图片占位符，保持位置信息
                                mixedContent.append("\n[当前位置图片替换为图片描述: ").append(imageText).append("]\n");
                                System.out.println("Extracted image to: " + imageFile.getAbsolutePath() + ", URL: " + imageUrl);
                            } else {
                                System.out.println("Could not extract image data for object: " + xObjectKey);
                            }
                        }
                    }
                }
                // 尝试检测和提取表格
                List<String> pageTables = detectAndExtractTables(document, i);
                for (String tableContent : pageTables) {
                    String tablePlaceholder = "[TABLE_" + (tableCounter++) + ": " + tableContent.substring(0, Math.min(50, tableContent.length())) + "...]";
                    tableContents.add(tableContent);
                    mixedContent.append("\n").append(tablePlaceholder).append("\n");
                }
            }
        } catch (IOException | NoApiKeyException | UploadFileException e) {
            // 只捕获IO异常，其他异常如数据库异常应该向上传播
            log.error("处理PDF文档时发生IO异常: ", e);
            throw e;
        }
        return mixedContent.toString();
        // return new MixedContentResult(mixedContent.toString(), imageUrls, tableContents);
    }

    @Retryable(
            value = {
                    // 服务端错误，如500、502、503等，这些可能是临时性错误
                    org.springframework.web.client.HttpServerErrorException.class,
                    // 连接超时等网络异常
                    java.net.SocketTimeoutException.class,
                    // 其他运行时异常，但要小心，避免重试不应该重试的错误
                    RuntimeException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
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
            // 检查是否是客户端错误（4xx），这些通常不应该重试
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                // 对于客户端错误，直接抛出而不是重试
                throw new org.springframework.retry.RetryException("客户端错误，不重试: " + e.getMessage());
            }
            throw e; // 重新抛出异常以触发重试机制
        }
        return imageText;
    }

    /**
     * 自定义PDF文本提取器，用于检测表格区域
     */
    static class CustomPDFTextStripper extends PDFTextStripper {

        private List<TextPosition> textPositions = new ArrayList<>();

        public CustomPDFTextStripper() throws IOException {
            super();
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            this.textPositions.addAll(textPositions);
            super.writeString(text, textPositions);
        }

        public List<TextPosition> getTextPositions() {
            return textPositions;
        }
    }

    /**
     * 用于存储提取结果的简单类
     */
    public static class ExtractionResult {

        public final String textContent;
        public final List<String> imageUrls; // 这里用字符串表示URL

        public ExtractionResult(String textContent, List<String> imageUrls) {
            this.textContent = textContent;
            this.imageUrls = imageUrls;
        }
    }

    /**
     * PDF图文表混合提取结果
     */
    public class MixedContentResult {

        public final String mixedContent;  // 包含图片和表格占位符的文本内容
        public final List<String> imageUrls;  // 图片URL列表
        public final List<String> tableContents;  // 表格内容列表

        public MixedContentResult(String mixedContent, List<String> imageUrls, List<String> tableContents) {
            this.mixedContent = mixedContent;
            this.imageUrls = imageUrls;
            this.tableContents = tableContents;
        }
    }
}
