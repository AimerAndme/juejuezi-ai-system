package com.yupi.yuaiagent.client;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Component
public class VlmClient {
    private static final String systemPrompt = """
            
            **提示词：**
            
            你是一个专业的文档向量化预处理器。你的任务是将图片内容转化为一段富含语义信息的纯文本，以便后续被向量数据库索引和检索。
            
            请遵循以下原则处理图片：
            
            1.  **角色定位**：你不是在“描述”图片，而是在**“重写”**文档。假设图片的位置原本就是一段文字，你需要把这段“隐形文字”写出来。
            2.  **关键词植入**：
                -   必须包含图片中的具体名词、数据、术语。
                -   必须包含图片所展示的**结论**、**趋势**或**逻辑关系**。
                -   预测用户可能会用来搜索该图片的**查询词**，并将这些词自然地融入段落中。
            3.  **上下文融合（关键）**：
                -   请想象这段文字是嵌入在正文中的。
                -   如果图片是一张流程图，就把它写成一段说明文。
                -   如果图片是一张表，就用文字概括其核心数据和对比关系（例如：“数据显示，A指标在Q4达到了峰值，同比增长了35%”）。
                -   如果图片是架构图，就描述其层级关系和组件功能。
            4.  **去噪处理**：
                -   不要输出“这是一张图片”、“图片显示”、“图中可以看到”等元描述信息。
                -   直接输出核心内容。
            """;
    @Value("${spring.ai.dashscope.image.options.model}")
    private String modelId;
    @Value("${spring.ai.dashscope.api-key}")
    private String apikey;

    public String image2text(String imageUrl) throws NoApiKeyException, UploadFileException {
        log.info("开始图片转文字！");
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder().role("user")
                .content(Arrays.asList(
                        Collections.singletonMap("image", imageUrl),
                        Collections.singletonMap("text", systemPrompt))).build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                // 各地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
                .apiKey(apikey)
                .model(modelId)
                .messages(Collections.singletonList(userMessage))
                .build();
        MultiModalConversationResult result = null;
        try {
            result = conv.call(param);
        } catch (Exception e) {
            log.error("图片转文字出错！");
            throw new RuntimeException("图片转文字失败！");
        }

        return (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
    }
}
