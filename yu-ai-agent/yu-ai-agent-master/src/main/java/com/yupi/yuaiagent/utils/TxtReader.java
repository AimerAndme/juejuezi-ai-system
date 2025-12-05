package com.yupi.yuaiagent.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TxtReader {
    public static String readTxtToString(String path) throws Exception {
        // 处理 classpath 资源（JAR包内）
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            // 用ClassLoader加载classpath资源（兼容打包后）
            try (InputStream is = TxtReader.class.getClassLoader().getResourceAsStream(resourcePath);
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                if (is == null) {
                    throw new FileNotFoundException("classpath资源不存在：" + resourcePath);
                }

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n"); // 保留换行符
                }
                return sb.toString();
            }
        }
        // 处理本地文件路径
        else {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        }
    }
}
