package com.yupi.yuaiagent.client;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
// 嵌入向量生成客户端
@Component
public class EmbeddingClient {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingClient.class);
    //    private final WebClient webClient;
//    private final ObjectMapper objectMapper;
    @Value("${embedding.api.model}")
    private String modelId;
    @Value("${embedding.api.batch-size:100}")
    private int batchSize;
    @Value("${embedding.api.dimension:2048}")
    private int dimension;
    @Value("${embedding.api.key}")
    private String apikey;

//    public EmbeddingClient(WebClient embeddingWebClient, ObjectMapper objectMapper) {
//        this.webClient = embeddingWebClient;
//        this.objectMapper = objectMapper;
//    }

//    /**
//     * 调用通义千问 API 生成向量
//     *
//     * @param texts 输入文本列表
//     * @return 对应的向量列表
//     */
//    public List<float[]> embed(List<String> texts) {
//        try {
//            logger.info("开始生成向量，文本数量: {}", texts.size());
//
//            List<float[]> all = new ArrayList<>(texts.size());
//            for (int start = 0; start < texts.size(); start += batchSize) {
//                int end = Math.min(start + batchSize, texts.size());
//                List<String> sub = texts.subList(start, end);
//                logger.debug("调用向量 API, 批次: {}-{} (size={})", start, end - 1, sub.size());
//                String response = callApiOnce(sub);
//                all.addAll(parseVectors(response));
//            }
//            logger.info("成功生成向量，总数量: {}", all.size());
//            return all;
//        } catch (Exception e) {
//            logger.error("调用向量化 API 失败: {}", e.getMessage(), e);
//            throw new RuntimeException("向量生成失败", e);
//        }
//    }
//
//    private String callApiOnce(List<String> batch) {
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("model", modelId);
//        requestBody.put("input", batch);
//        requestBody.put("dimension", dimension);  // 直接在根级别设置dimension
//        requestBody.put("encoding_format", "float");  // 添加编码格式
//
//        return webClient.post()
//                .uri("/embeddings")
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(String.class)
//                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
//                        .filter(e -> e instanceof WebClientResponseException))
//                .block(Duration.ofSeconds(30));
//    }
//
//    private List<float[]> parseVectors(String response) throws Exception {
//        JsonNode jsonNode = objectMapper.readTree(response);
//        JsonNode data = jsonNode.get("data");  // 兼容模式下使用data字段
//        if (data == null || !data.isArray()) {
//            throw new RuntimeException("API 响应格式错误: data 字段不存在或不是数组");
//        }
//
//        List<float[]> vectors = new ArrayList<>();
//        for (JsonNode item : data) {
//            JsonNode embedding = item.get("embedding");
//            if (embedding != null && embedding.isArray()) {
//                float[] vector = new float[embedding.size()];
//                for (int i = 0; i < embedding.size(); i++) {
//                    vector[i] = (float) embedding.get(i).asDouble();
//                }
//                vectors.add(vector);
//            }
//        }
//        return vectors;
//    }

    public double[] text2embed(String text) {
        List<Double> result = new ArrayList<>();
        try {
            // TOdo构建请求参数(无法修改向量维度！)
            TextEmbeddingParam param = TextEmbeddingParam
                    .builder()
                    .model(modelId)
                    .apiKey(apikey)
                    // 输入文本
                    .texts(Collections.singleton(text))
                    .build();

            // 创建模型实例并调用
            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult textEmbeddingResult = textEmbedding.call(param);
            if (textEmbeddingResult.getOutput().getEmbeddings().get(0).getEmbedding().isEmpty()) {
                log.error("向量化失败！结果为空");
                return null;
            }
            // 输出结果
            log.info("文本：{}向量化完成！", text);
            result = textEmbeddingResult.getOutput().getEmbeddings().get(0).getEmbedding();
            return result.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (NoApiKeyException e) {
            // 捕获并处理API Key未设置的异常
            System.err.println("调用 API 时发生异常: " + e.getMessage());
            System.err.println("请检查您的 API Key 是否已正确配置。");
            e.printStackTrace();
        } catch (Exception e) {
            log.error("文本向量化出错！");
        }
        return null;
    }
}
