package com.yupi.yuaiagent.util;

import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;

@Component
public class FileUtils {

    /**
     * 计算文件SHA-256 hash（前32位模拟MD5）
     */
    public static String calculateMD5(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        return bytesToHex(md.digest()).substring(0, 32); // 取前32位
    }

    /**
     * 计算字节数组SHA-256 hash（前32位模拟MD5）
     */
    public static String calculateMD5(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        return bytesToHex(md.digest()).substring(0, 32); // 取前32位
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 合并分片文件
     */
    public static void mergeChunks(List<File> chunkFiles, File targetFile) throws IOException {
        // 不再排序，依赖调用方已经按chunkIndex排序
        System.out.println("[合并分片] 接收到的分片顺序: "
                + String.join(", ", chunkFiles.stream().map(File::getName).toArray(String[]::new)));

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            for (int i = 0; i < chunkFiles.size(); i++) {
                File chunkFile = chunkFiles.get(i);
                System.out.println("[合并分片] 正在合并第" + i + "个分片: " + chunkFile.getName() + ", 大小: " + chunkFile.length());
                try (FileInputStream fis = new FileInputStream(chunkFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
        System.out.println("[合并分片] 合并完成, 最终文件大小: " + targetFile.length());
    }

    /**
     * 创建目录（如果不存在）
     */
    public static void ensureDirectory(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    /**
     * 删除文件或目录
     */
    public static void deleteFileOrDirectory(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFileOrDirectory(f);
                }
            }
        }
        file.delete();
    }

    /**
     * 验证文件类型
     */
    public static boolean isAllowedFileType(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".docx")
                || lowerCaseName.endsWith(".md")
                || lowerCaseName.endsWith(".pdf")
                || lowerCaseName.endsWith(".txt");
    }
}
