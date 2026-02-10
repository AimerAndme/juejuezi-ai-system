package com.yupi.yuaiagent.service.parse;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class WordContentExtractorManualTest {

    @Autowired
    private WordContentExtractor wordContentExtractor;

    @Test
    void testExtractWordContentSimple_Manual() throws Exception {
        String wordPath = "E:\\编程学习\\项目\\yv-ai\\upload\\test.docx";
        
        File file = new File(wordPath);
        if (!file.exists()) {
            System.out.println("测试文件不存在，请修改wordPath为实际文件路径");
            return;
        }

        String content = wordContentExtractor.extractWordContentSimple(wordPath);
        
        System.out.println("========== Word文档内容 ==========");
        System.out.println(content);
        System.out.println("========== 内容长度: " + content.length() + " 字符 ==========");
    }

    @Test
    void testExtractWordContentWithVlm_Manual() throws Exception {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName("test.docx");
        fileUpload.setFileMd5("test123");
        fileUpload.setUserId("test_user");

        String wordPath = "E:\\编程学习\\项目\\yv-ai\\upload\\test.docx";
        String imageOutputDir = "E:\\编程学习\\项目\\yv-ai\\upload\\images";
        String baseUrl = "http://localhost:8080/images";
        
        File file = new File(wordPath);
        if (!file.exists()) {
            System.out.println("测试文件不存在，请修改wordPath为实际文件路径");
            return;
        }

        String content = wordContentExtractor.extractWordContentWithVlm(
            fileUpload,
            wordPath,
            imageOutputDir,
            baseUrl
        );
        
        System.out.println("========== Word文档内容（含图片识别） ==========");
        System.out.println(content);
        System.out.println("========== 内容长度: " + content.length() + " 字符 ==========");
    }

    @Test
    void testExtractWordContentWithVlm_Performance() throws Exception {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName("performance_test.docx");
        fileUpload.setFileMd5("perf123");
        fileUpload.setUserId("test_user");

        String wordPath = "E:\\编程学习\\项目\\yv-ai\\upload\\performance_test.docx";
        String imageOutputDir = "E:\\编程学习\\项目\\yv-ai\\upload\\images";
        String baseUrl = "http://localhost:8080/images";
        
        File file = new File(wordPath);
        if (!file.exists()) {
            System.out.println("测试文件不存在，请修改wordPath为实际文件路径");
            return;
        }

        long startTime = System.currentTimeMillis();
        
        String content = wordContentExtractor.extractWordContentWithVlm(
            fileUpload,
            wordPath,
            imageOutputDir,
            baseUrl
        );
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        System.out.println("========== 性能测试结果 ==========");
        System.out.println("文件路径: " + wordPath);
        System.out.println("内容长度: " + content.length() + " 字符");
        System.out.println("处理耗时: " + executionTime + " ms");
        System.out.println("处理耗时: " + (executionTime / 1000.0) + " 秒");
        System.out.println("================================");
    }

    @Test
    void testExtractMultipleWordFiles() throws Exception {
        String[] testFiles = {
            "E:\\编程学习\\项目\\yv-ai\\upload\\test1.docx",
            "E:\\编程学习\\项目\\yv-ai\\upload\\test2.docx",
            "E:\\编程学习\\项目\\yv-ai\\upload\\test3.docx"
        };
        
        String imageOutputDir = "E:\\编程学习\\项目\\yv-ai\\upload\\images";
        String baseUrl = "http://localhost:8080/images";

        for (int i = 0; i < testFiles.length; i++) {
            String wordPath = testFiles[i];
            File file = new File(wordPath);
            
            if (!file.exists()) {
                System.out.println("文件 " + wordPath + " 不存在，跳过");
                continue;
            }

            FileUpload fileUpload = new FileUpload();
            fileUpload.setFileName("test" + (i + 1) + ".docx");
            fileUpload.setFileMd5("test" + (i + 1));
            fileUpload.setUserId("test_user");

            System.out.println("\n========== 处理文件 " + (i + 1) + " ==========");
            System.out.println("文件路径: " + wordPath);

            long startTime = System.currentTimeMillis();
            
            String content = wordContentExtractor.extractWordContentWithVlm(
                fileUpload,
                wordPath,
                imageOutputDir,
                baseUrl
            );
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            System.out.println("内容长度: " + content.length() + " 字符");
            System.out.println("处理耗时: " + executionTime + " ms");
            System.out.println("================================");
        }
    }

    @Test
    void testExtractWordContent_ErrorHandling() throws Exception {
        String invalidPath = "E:\\编程学习\\项目\\yv-ai\\upload\\non_existent.docx";
        
        try {
            String content = wordContentExtractor.extractWordContentSimple(invalidPath);
            System.out.println("内容: " + content);
        } catch (Exception e) {
            System.out.println("========== 错误处理测试 ==========");
            System.out.println("捕获到预期异常: " + e.getClass().getSimpleName());
            System.out.println("异常信息: " + e.getMessage());
            System.out.println("================================");
        }
    }

    @Test
    void testExtractWordContent_LargeFile() throws Exception {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName("large_test.docx");
        fileUpload.setFileMd5("large123");
        fileUpload.setUserId("test_user");

        String wordPath = "E:\\编程学习\\项目\\yv-ai\\upload\\large_test.docx";
        String imageOutputDir = "E:\\编程学习\\项目\\yv-ai\\upload\\images";
        String baseUrl = "http://localhost:8080/images";
        
        File file = new File(wordPath);
        if (!file.exists()) {
            System.out.println("大文件测试文件不存在，请修改wordPath为实际文件路径");
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        long startTime = System.currentTimeMillis();
        
        String content = wordContentExtractor.extractWordContentWithVlm(
            fileUpload,
            wordPath,
            imageOutputDir,
            baseUrl
        );
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        System.out.println("========== 大文件测试结果 ==========");
        System.out.println("文件路径: " + wordPath);
        System.out.println("文件大小: " + (file.length() / 1024.0 / 1024.0) + " MB");
        System.out.println("内容长度: " + content.length() + " 字符");
        System.out.println("处理耗时: " + executionTime + " ms (" + (executionTime / 1000.0) + " 秒)");
        System.out.println("内存使用: " + (memoryUsed / 1024.0 / 1024.0) + " MB");
        System.out.println("================================");
    }
}
