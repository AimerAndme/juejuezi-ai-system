package com.yupi.yuaiagent.service;// 创建一个测试文件：src/test/java/com/yupi/yuaiagent/client/VlmClientTest.java

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.yupi.yuaiagent.client.VlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class VlmClientTest {

    @Autowired
    private VlmClient vlmClient;

    @Test
    public void testImageToText() throws NoApiKeyException, UploadFileException {
        // 替换为实际存在的图片路径进行测试
        String result = vlmClient.image2text("E:\\编程学习\\项目\\yv-ai\\upload\\images\\IMG_1406.JPG");
        assertNotNull(result);
        System.out.println("图片转文字结果: " + result);
    }
}