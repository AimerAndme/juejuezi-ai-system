package com.yupi.yuaiagent.service.parse;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import com.yupi.yuaiagent.client.VlmClient;
import com.yupi.yuaiagent.domin.constant.FileConstant;
import com.yupi.yuaiagent.domin.entity.FileExtractedImages;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import com.yupi.yuaiagent.service.IFileExtractedImagesService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@AllArgsConstructor
public class PDFContentExtractor {

    private final IFileExtractedImagesService fileExtractedImagesService;
    private final VlmClient vlmClient;

    @Resource(name = "pdfPageExecutor")
    private ThreadPoolTaskExecutor pdfPageExecutor;

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

        return null;
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
    @ExecutionTimeMonitor
    public String extractMixedContentWithVlm(FileUpload fileUpload, String pdfPath, String imageOutputDir, String baseUrl) throws IOException, NoApiKeyException, UploadFileException {
        long startTime = System.currentTimeMillis();
        String userId = fileUpload.getUserId();
        String fileMd5 = fileUpload.getFileMd5();
        String fileName = fileUpload.getFileName();
        File pdfFile = new File(pdfPath);

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            int pageCount = document.getNumberOfPages();

            File outputDir = new File(imageOutputDir);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            List<CompletableFuture<PageProcessResult>> futures = new ArrayList<>();

            for (int i = 0; i < pageCount; i++) {
                final int pageIndex = i;
                CompletableFuture<PageProcessResult> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return processPage(document, pageIndex, fileMd5, userId, outputDir, baseUrl);
                    } catch (Exception e) {
                        log.error("处理第 {} 页失败: {}", pageIndex + 1, e.getMessage(), e);
                        PageProcessResult errorResult = new PageProcessResult();
                        errorResult.setPageIndex(pageIndex);
                        errorResult.setPageContent("");
                        errorResult.setImageUrls(new ArrayList<>());
                        errorResult.setTableContents(new ArrayList<>());
                        return errorResult;
                    }
                }, pdfPageExecutor);
                futures.add(future);
            }

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            try {
                allFutures.get();
            } catch (Exception e) {
                log.error("等待页面处理完成时发生异常: {}", e.getMessage(), e);
            }

            StringBuilder mixedContent = new StringBuilder();
            List<String> allImageUrls = new ArrayList<>();
            List<String> allTableContents = new ArrayList<>();

            for (CompletableFuture<PageProcessResult> future : futures) {
                try {
                    PageProcessResult result = future.get();
                    mixedContent.append(result.getPageContent());
                    allImageUrls.addAll(result.getImageUrls());
                    allTableContents.addAll(result.getTableContents());
                } catch (Exception e) {
                    log.error("获取页面处理结果失败: {}", e.getMessage(), e);
                }
            }

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.info("PDF处理完成 - 文件: {}, 总页数: {}, 提取图片数: {}, 提取表格数: {}, 耗时: {}ms", fileName, pageCount, allImageUrls.size(), allTableContents.size(), executionTime);
            return mixedContent.toString();
        } catch (IOException e) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.error("PDF处理失败 - 文件: {}, 耗时: {}ms, 异常: {}", fileName, executionTime, e.getMessage());
            throw e;
        }
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
    @ExecutionTimeMonitor
    public MixedContentResult extractMixedContentWithVlmCur(FileUpload fileUpload, String pdfPath, String imageOutputDir, String baseUrl) throws IOException, NoApiKeyException, UploadFileException {
        long startTime = System.currentTimeMillis();
        String userId = fileUpload.getUserId();
        String fileMd5 = fileUpload.getFileMd5();
        String fileName = fileUpload.getFileName();
        File pdfFile = new File(pdfPath);
        List<PDFPageText> textList = new ArrayList<>();
        List<PDFPageImage> imageList = new ArrayList<>();
        List<PDFPageExtractResult> pdfExtractResultList = new ArrayList<>();
        MixedContentResult mixedContentResult = new MixedContentResult();
        List<Map<Integer, String>> errorTextList = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            int pageCount = document.getNumberOfPages();
            File outputDir = new File(imageOutputDir);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            //完成文本和图片的提取（图片临时存储）
            for (int i = 0; i < pageCount; i++) {
                PDFPageExtractResult pdfPageExtractResult = new PDFPageExtractResult();
                //文本提取
                try {
                    PDFPageText pageText = extractText(document, i);
                    textList.add(pageText);
                    pdfPageExtractResult.setText(pageText.getText());
                } catch (Exception e) {
                    errorTextList.add(Map.of(i, e.getMessage()));
                }
                //如果存在图片
                PDFPageImage pdfPageImage = extactImage(document, i, fileMd5, outputDir, baseUrl);
                if (!pdfPageImage.getImageUrlList().isEmpty()) {
                    imageList.add(pdfPageImage);
                    pdfPageExtractResult.setImageUrlList(pdfPageImage.getImageUrlList());
                }
                pdfExtractResultList.add(pdfPageExtractResult);
            }
        } catch (IOException e) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.error("PDF处理失败 - 文件: {}, 耗时: {}ms, 异常: {}", fileName, executionTime, e.getMessage());
            throw e;
        }
        //多线程提取图片信息
        List<Map<Integer, String>> errorImageList = new ArrayList<>();
        List<FileExtractedImages> fileExtractedImagesList = new ArrayList<>();
        List<CompletableFuture<PDFPageExtractResult>> pdfExtractFutureList = new ArrayList<>();
        for (PDFPageExtractResult pdfPageExtractResult : pdfExtractResultList) {
            if (pdfPageExtractResult.getImageTextList() == null || pdfPageExtractResult.getImageTextList().isEmpty()) {
                continue;
            }
            CompletableFuture<PDFPageExtractResult> future = CompletableFuture.supplyAsync(() -> {
                List<String> imageText = new ArrayList<>();
                for (String imageUrl : pdfPageExtractResult.getImageUrlList()) {
                    String image2Text = "";
                    FileExtractedImages fileExtractedImages = new FileExtractedImages();
                    try {
                        image2Text = image2Text(imageUrl);
                        fileExtractedImages.setImagePath(imageUrl)
                                .setPageNumber(pdfPageExtractResult.getPageIndex())
                                .setVlModelName("test")
                                .setVlModelVersion("test")
                                .setVlResultText(image2Text)
                                .setVlProcessingStatus("success");
                    } catch (NoApiKeyException | UploadFileException e) {
                        //业务逻辑异常
                        log.error("图片转文字失败: {}", imageUrl, e);
                        errorImageList.add(Map.of(pdfPageExtractResult.getPageIndex(), "业务异常"));
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        fileExtractedImages.setImagePath(imageUrl)
                                .setPageNumber(pdfPageExtractResult.getPageIndex())
                                .setVlModelName("test")
                                .setVlModelVersion("test")
                                .setVlResultText(image2Text)
                                .setVlProcessingStatus("fail");
                        log.error("图片转文字失败: {}", imageUrl, e);
                        errorImageList.add(
                                Map.of(pdfPageExtractResult.getPageIndex(), "系统异常")
                        );
                        throw new RuntimeException(e);
                    }
                    fileExtractedImagesList.add(fileExtractedImages);
                    imageText.add(image2Text);
                }
                pdfPageExtractResult.setImageTextList(imageText);
                return pdfPageExtractResult;
            }, pdfPageExecutor);
            pdfExtractFutureList.add(future);
        }
        //等待处理完成
        log.info("等待图片信息处理完成");
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(pdfExtractFutureList.toArray(new CompletableFuture[0]));
        try {
            allFutures.get();
        } catch (Exception e) {
            log.error("等待页面处理完成时发生异常: {}", e.getMessage(), e);
        }
        //处理图片信息，拼接内容并返回
        StringBuilder mixedContent = new StringBuilder();
        log.info("开始拼接内容");
        for (PDFPageExtractResult pdfPageExtractResult : pdfExtractResultList) {
            mixedContent.append(pdfPageExtractResult.getText());
            if (pdfPageExtractResult.getImageTextList() == null) {
                continue;
            }
            if (!pdfPageExtractResult.getImageTextList().isEmpty()) {
                for (String s : pdfPageExtractResult.getImageTextList()) {
                    mixedContent.append(s);
                }
            }
        }
        mixedContentResult.setMixedContent(mixedContent.toString());
        mixedContentResult.setErrorImagePages(errorImageList);
        mixedContentResult.setErrorTextPages(errorTextList);
        mixedContentResult.setFileExtractedImages(fileExtractedImagesList);
        return mixedContentResult;
    }

    private PDFPageImage extactImage(PDDocument document, int pageIndex, String fileMd5, File outputDir, String baseUrl) throws IOException {
        PDPage page = document.getPage(pageIndex);
        PDResources resources = page.getResources();
        int imageCounter = 0;
        ArrayList<String> imageUrls = new ArrayList<>();
        if (resources != null && resources.getXObjectNames() != null) {
            for (Object xObjectKey : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject((org.apache.pdfbox.cos.COSName) xObjectKey);
                if (xObject instanceof PDImageXObject) {
                    PDImageXObject image = (PDImageXObject) xObject;
                    String suffix = image.getSuffix();
                    if (suffix == null) {
                        suffix = "png";
                    }
                    String imageFileName = fileMd5 + FileConstant.IMAGES_UPLOADS_PREFIX + (pageIndex + 1) + "_" + (imageCounter++) + "." + suffix;
                    File imageFile = new File(outputDir, imageFileName);
                    BufferedImage awtImage = null;
                    try {
                        awtImage = image.getImage();
                    } catch (IOException e) {
                        log.warn("无法提取图片数据，可能是由于ICC颜色空间问题: {}", e.getMessage());
                        continue;
                    }
                    if (awtImage != null) {
                        try {
                            ImageIO.write(awtImage, suffix.toUpperCase(), imageFile);
                            log.debug("成功保存图片到: {}", imageFile.getAbsolutePath());
                        } catch (IOException e) {
                            log.error("保存图片失败: {}", imageFile.getAbsolutePath(), e);
                            continue;
                        }
                        String imageUrl = baseUrl + "/" + imageFileName;
                        imageUrls.add(imageUrl);
                    } else {
                        log.warn("无法获取图片数据，请检查图片格式是否正确");
                    }
                }
            }
        }
        return new PDFPageImage(pageIndex, imageUrls, new ArrayList<>());
    }

    //解析文本
    private PDFPageText extractText(PDDocument document, int pageIndex) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);
        String pageText = "";
        StringBuilder tableContents = new StringBuilder();
        try {
            String text = stripper.getText(document);
            if (text != null) {
                pageText = text;
            } else {
                log.warn("第 {} 页文本内容为空", pageIndex + 1);
            }
        } catch (Exception e) {
            log.warn("提取第 {} 页文本时出错，可能是字体或PDF格式问题: {}", pageIndex + 1, e.getMessage());
        }
        // 尝试检测和提取表格
        List<String> pageTables = detectAndExtractTables(document, pageIndex);
        for (String tableContent : pageTables) {
            if (tableContent == null) {
                tableContent = "";
                log.warn("第 {} 页表格内容为空", pageIndex + 1);
            }
            String tablePlaceholder = "[TABLE_" + ": " + tableContent.trim() + "..]";
            tableContents.append("\n").append(tablePlaceholder).append("\n");
        }
        PDFPageText pdfPageText = new PDFPageText();
        pdfPageText.setText(pageText + tableContents.toString());
        pdfPageText.setPageIndex(pageIndex);
        return pdfPageText;
    }

    private PageProcessResult processPage(PDDocument document, int pageIndex, String fileMd5, String userId, File outputDir, String baseUrl) throws IOException, NoApiKeyException, UploadFileException {
        PageProcessResult result = new PageProcessResult();
        result.setPageIndex(pageIndex);
        result.setImageUrls(new ArrayList<>());
        result.setTableContents(new ArrayList<>());


        StringBuilder pageContent = new StringBuilder();

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);
        String pageText = "";
        try {
            pageText = stripper.getText(document);
        } catch (Exception e) {
            log.warn("提取第 {} 页文本时出错，可能是字体或PDF格式问题: {}", pageIndex + 1, e.getMessage());
        }
        pageContent.append(pageText);
        //图片提取
        PDPage page = document.getPage(pageIndex);
        PDResources resources = page.getResources();
        int imageCounter = 0;
        if (resources != null && resources.getXObjectNames() != null) {
            for (Object xObjectKey : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject((org.apache.pdfbox.cos.COSName) xObjectKey);

                if (xObject instanceof PDImageXObject) {
                    PDImageXObject image = (PDImageXObject) xObject;

                    String suffix = image.getSuffix();
                    if (suffix == null) {
                        suffix = "png";
                    }
                    String imageFileName = fileMd5 + FileConstant.IMAGES_UPLOADS_PREFIX + (pageIndex + 1) + "_" + (imageCounter++) + "." + suffix;
                    File imageFile = new File(outputDir, imageFileName);

                    BufferedImage awtImage = null;
                    try {
                        awtImage = image.getImage();
                    } catch (IOException e) {
                        log.warn("无法提取图片数据，可能是由于ICC颜色空间问题: {}", e.getMessage());
                        continue;
                    }
                    if (awtImage != null) {
                        try {
                            ImageIO.write(awtImage, suffix.toUpperCase(), imageFile);
                            log.debug("成功保存图片到: {}", imageFile.getAbsolutePath());
                        } catch (IOException e) {
                            log.error("保存图片失败: {}", imageFile.getAbsolutePath(), e);
                            continue;
                        }

                        String imageUrl = baseUrl + "/" + imageFileName;
                        result.getImageUrls().add(imageUrl);

                        log.info("开始图片转文字: {}", imageUrl);
                        String imageText = image2Text(imageUrl);

                        try {
                            FileExtractedImages fileExtractedImages = new FileExtractedImages();
                            fileExtractedImages
                                    .setFileMd5(fileMd5)
                                    .setUserId(userId)
                                    .setImagePath(imageUrl)
                                    .setPageNumber(pageIndex + 1)
                                    .setVlModelName("vlm")
                                    .setVlModelVersion("1.0")
                                    .setVlResultText(imageText);
                            fileExtractedImagesService.addFileExtractedImages(fileExtractedImages);
                            log.info("图片转文字成功入库: {}", imageFileName);
                        } catch (Exception e) {
                            log.error("图片信息入库失败: {}", imageFileName, e);
                            throw new RuntimeException("图片信息入库失败: " + imageFileName, e);
                        }

                        pageContent.append("\n[当前位置图片替换为图片描述: ").append(imageText).append("]\n");
                    } else {
                        log.warn("无法获取图片数据，请检查图片格式是否正确");
                    }
                }
            }
        }

        List<String> pageTables = detectAndExtractTables(document, pageIndex);
        int tableCounter = 0;
        for (String tableContent : pageTables) {
            String tablePlaceholder = "[TABLE_" + (tableCounter++) + ": " + tableContent.substring(0, Math.min(50, tableContent.length())) + "...]";
            result.getTableContents().add(tableContent);
            pageContent.append("\n").append(tablePlaceholder).append("\n");
        }

        result.setPageContent(pageContent.toString());
        return result;
    }

    @ExecutionTimeMonitor
    public String extractMixedContentWithVlmSerial(FileUpload fileUpload, String pdfPath, String imageOutputDir, String baseUrl) throws IOException, NoApiKeyException, UploadFileException {
        long startTime = System.currentTimeMillis();
        String userId = fileUpload.getUserId();
        String fileMd5 = fileUpload.getFileMd5();
        String fileName = fileUpload.getFileName();
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

                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String pageText = "";
                try {
                    pageText = stripper.getText(document);
                } catch (Exception e) {
                    log.warn("提取第 {} 页文本时出错，可能是字体或PDF格式问题: {}", i + 1, e.getMessage());
                    continue;
                }
                mixedContent.append(pageText);

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
                            String imageFileName = fileMd5 + FileConstant.IMAGES_UPLOADS_PREFIX + (i + 1) + "_" + (imageCounter++) + "." + suffix;
                            File imageFile = new File(outputDir, imageFileName);

                            BufferedImage awtImage = null;
                            try {
                                awtImage = image.getImage();
                            } catch (Exception e) {
                                log.warn("无法提取图片数据，可能是由于ICC颜色空间问题: {}", e.getMessage());
                                continue;
                            }
                            if (awtImage != null) {
                                try {
                                    ImageIO.write(awtImage, suffix.toUpperCase(), imageFile);
                                    log.debug("成功保存图片到: {}", imageFile.getAbsolutePath());
                                } catch (IOException e) {
                                    log.error("保存图片失败: {}", imageFile.getAbsolutePath(), e);
                                    continue;
                                }

                                String imageUrl = baseUrl + "/" + imageFileName;
                                imageUrls.add(imageUrl);

                                log.info("开始图片转文字: {}", imageUrl);
                                String imageText = image2Text(imageUrl);

                                try {
                                    FileExtractedImages fileExtractedImages = new FileExtractedImages();
                                    fileExtractedImages
                                            .setFileMd5(fileMd5)
                                            .setUserId(userId)
                                            .setImagePath(imageUrl)
                                            .setPageNumber(i + 1)
                                            .setVlModelName("vlm")
                                            .setVlModelVersion("1.0")
                                            .setVlResultText(imageText);
                                    fileExtractedImagesService.addFileExtractedImages(fileExtractedImages);
                                    log.info("图片转文字成功入库: {}", imageFileName);
                                } catch (Exception e) {
                                    log.error("图片信息入库失败: {}", imageFileName, e);
                                    throw new RuntimeException("图片信息入库失败: " + imageFileName, e);
                                }

                                mixedContent.append("\n[当前位置图片替换为图片描述: ").append(imageText).append("]\n");
                            } else {
                                log.warn("无法获取图片数据，请检查图片格式是否正确");
                            }
                        }
                    }
                }

                List<String> pageTables = detectAndExtractTables(document, i);
                for (String tableContent : pageTables) {
                    String tablePlaceholder = "[TABLE_" + (tableCounter++) + ": " + tableContent.substring(0, Math.min(50, tableContent.length())) + "...]";
                    tableContents.add(tableContent);
                    mixedContent.append("\n").append(tablePlaceholder).append("\n");
                }
            }
        } catch (IOException | NoApiKeyException | UploadFileException e) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.error("PDF处理失败 - 文件: {}, 耗时: {}ms, 异常: {}", fileName, executionTime, e.getMessage());
            throw e;
        }
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        log.info("PDF处理完成 - 文件: {}, 总页数: {}, 提取图片数: {}, 提取表格数: {}, 耗时: {}ms", fileName, mixedContent.length() > 0 ? "已处理" : "未处理", imageUrls.size(), tableContents.size(), executionTime);
        return mixedContent.toString();
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
        String imageText;
        try {
            imageText = vlmClient.image2text(imageUrl);
            log.info("图片转文字成功: {}", imageUrl);
            if (imageText == null) {
                log.info("图片转文字,响应为空内容: {}", imageUrl);
                return "";
            }
        } catch (Exception e) {
            log.error("调用 vlmClient 发生未知错误: {}", imageUrl, e);

            // --- 关键点：如果是客户端错误（4xx），不希望重试 ---
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                // 将客户端错误包装为业务异常抛出
                // Spring Retry 默认只处理 RuntimeException，如果 NoApiKeyException 是受检异常，必须这样处理
                throw new UploadFileException("客户端错误，不进行重试: " + e.getMessage());
            }
            if (e instanceof HttpServerErrorException) {
                log.info("图片转文字,响应为空内容: {}", imageUrl);
                throw e;
            }
            // 抛出原异常，触发 @Retryable 机制
            throw e;
        }
        return imageText;
    }

    // --- 必须添加的恢复方法 ---
    @Recover
    public String recover(Exception e, String imageUrl) throws Exception {
        log.error("重试机制已耗尽，最终失败: {}", imageUrl, e);

        // 如果是业务逻辑异常（如 API Key 错误），通常不需要重试，这里直接处理
        if (e instanceof NoApiKeyException || e instanceof UploadFileException) {
            throw e; // 重新抛出业务异常
        }
        log.error("重试机制已耗尽，最终失败: {}", imageUrl, e);
        // 对于重试耗尽的系统错误，返回默认值或抛出新的运行时异常
        return "";
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

    @Data
    static class PageProcessResult {
        private int pageIndex;
        private String pageContent;
        private List<String> imageUrls;
        private List<String> tableContents;
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

//    /**
//     * PDF图文表混合提取结果
//     */
//    public class MixedContentResult {
//
//        public final String mixedContent;  // 包含图片和表格占位符的文本内容
//        public final List<String> imageUrls;  // 图片URL列表
//        public final List<String> tableContents;  // 表格内容列表
//
//        public MixedContentResult(String mixedContent, List<String> imageUrls, List<String> tableContents) {
//            this.mixedContent = mixedContent;
//            this.imageUrls = imageUrls;
//            this.tableContents = tableContents;
//        }
//    }
}
