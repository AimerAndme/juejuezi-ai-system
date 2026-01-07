package com.yupi.yuaiagent.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFContentExtractor {

    /**
     * 提取PDF的文本和图片
     *
     * @param pdfPath        PDF文件路径
     * @param imageOutputDir 图片保存的目录路径
     * @param baseUrl        图片的基础URL (例如: "http://yourserver.com/images/")
     * @return 包含文本内容和图片URL列表的 ExtractionResult
     * @throws IOException
     */
    public static ExtractionResult extractTextAndImages(String pdfPath, String imageOutputDir, String baseUrl) throws IOException {
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

    public static void main(String[] args) {
        String pdfPath = "E:\\编程学习\\项目\\yv-ai\\rag文档\\代码随想录知识星球精华Java篇.pdf"; // 替换为你的PDF文件路径
        String imageOutputDir = "E:\\编程学习\\项目\\yv-ai\\upload\\images"; // 替换为你想保存图片的目录
        String baseUrl = "http://localhost:8080/images/"; // 替换为你应用能访问图片的基础URL

        try {
            ExtractionResult result = extractTextAndImages(pdfPath, imageOutputDir, baseUrl);

            System.out.println("--- Extracted Text Content ---");
            System.out.println(result.textContent);
            System.out.println("\n--- Extracted Image URLs ---");
            for (String url : result.imageUrls) {
                System.out.println(url);
            }

            // --- 在这里进行内容整合 ---
            // 例如，将图片URL插入到文本的特定位置（这需要更复杂的逻辑来确定位置）
            // 或者，将文本和图片URL分别存储，用于后续在前端（如HTML）中组合显示。
            String combinedContent = result.textContent + "\n\nExtracted Images:\n" + String.join("\n", result.imageUrls);
            System.out.println("\n--- Combined Content (Example) ---");
            System.out.println(combinedContent);

        } catch (IOException e) {
            e.printStackTrace();
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
}