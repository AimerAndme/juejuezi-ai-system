package com.yupi.yuaiagent.utils;

import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;


public class FileUtils {

    // ... existing code ...

    /**
     * 在指定目录下根据文件名查找文件（非递归）
     * 
     * @param directory 搜索目录路径
     * @param fileName 文件名（支持完整匹配）
     * @return 文件的完整路径，如果未找到返回 null
     */
    public static String findFileByName(String directory, String fileName) {
        try {
            Path dirPath = Paths.get(directory);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                return null;
            }

            Path targetFile = dirPath.resolve(fileName);
            if (Files.exists(targetFile) && Files.isRegularFile(targetFile)) {
                return targetFile.toAbsolutePath().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在指定目录下递归查找文件（支持子目录）
     * 
     * @param directory 搜索目录路径
     * @param fileName 文件名（支持完整匹配）
     * @return 文件的完整路径，如果未找到返回 null
     */
    public static String findFileRecursively(String directory, String fileName) {
        try {
            Path dirPath = Paths.get(directory);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                return null;
            }

            Optional<Path> foundFile = Files.walk(dirPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .findFirst();

            return foundFile.map(path -> path.toAbsolutePath().toString()).orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在指定目录下查找所有匹配的文件（支持通配符）
     * 
     * @param directory 搜索目录路径
     * @param pattern 文件名模式（支持通配符，如 "*.txt", "test*.pdf"）
     * @param recursive 是否递归搜索子目录
     * @return 匹配的文件路径列表
     */
    public static List<String> findFilesByPattern(String directory, String pattern, boolean recursive) {
        List<String> results = new ArrayList<>();
        try {
            Path dirPath = Paths.get(directory);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                return results;
            }

            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

            if (recursive) {
                Files.walk(dirPath)
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .forEach(path -> results.add(path.toAbsolutePath().toString()));
            } else {
                Files.list(dirPath)
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .forEach(path -> results.add(path.toAbsolutePath().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 检查文件是否存在于指定目录
     * 
     * @param directory 目录路径
     * @param fileName 文件名
     * @return 如果文件存在返回 true，否则返回 false
     */
    public static boolean fileExistsInDirectory(String directory, String fileName) {
        Path filePath = Paths.get(directory, fileName);
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }

    /**
     * 获取文件的绝对路径
     * 
     * @param directory 目录路径
     * @param fileName 文件名
     * @return 文件的绝对路径
     */
    public static String getAbsolutePath(String directory, String fileName) {
        return Paths.get(directory, fileName).toAbsolutePath().toString();
    }

    // ... existing code ...
}
