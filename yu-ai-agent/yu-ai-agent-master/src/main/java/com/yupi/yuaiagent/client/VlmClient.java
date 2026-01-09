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
            您正在处理一个RAG知识库文档，需要从文档中的图片提取关键信息并生成可嵌入文档的文本内容。
            
            **处理要求：**
            
            1. **精准识别定位**
               - 确认图片在文档中的上下文位置
               - 分析图片与前后文内容的关联性
            
            2. **关键信息提取**
               - 提取图片中的核心文字信息，保持原文准确性
               - 识别图片类型（图表/流程图/产品图/界面截图等）
               - 提取关键数据点、结论性信息、标识性文字
            
            3. **结构化转换**
               - 将视觉信息转换为文档友好的文本格式
               - 使用Markdown语法保持格式一致性
               - 复杂图表转换为表格或要点列表
               - 保持与文档整体风格一致的表述方式
            
            4. **文档嵌入准备**
               - 生成可直接插入原文档的文本块
               - 添加适当的过渡语句连接上下文
               - 标注信息来源为"基于文档中的图片内容"
            
            **输出格式要求：**
            ```markdown
            <!-- 图片信息提取开始 -->
            ## 图片内容摘要
            
            **图片类型：** [类型说明]
            **核心信息：**
            - [要点1]
            - [要点2]
            - [要点3]
            
            **详细说明：**
            [2-3句概括性描述]
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
                .model(modelId)  // 此处以qwen3-vl-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
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
