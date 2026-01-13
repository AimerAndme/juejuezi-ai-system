package com.yupi.yuaiagent.evaluation.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class EvaluationDatasetManager {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<EvaluationTestCase> loadDatasetFromJson(String filePath) throws IOException {
        log.info("从JSON文件加载评估数据集: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("数据集文件不存在: " + filePath);
        }

        EvaluationDataset dataset = objectMapper.readValue(file, EvaluationDataset.class);
        log.info("成功加载 {} 个测试用例", dataset.getTestCases().size());

        return dataset.getTestCases();
    }

    public void saveDatasetToJson(List<EvaluationTestCase> testCases, String filePath) throws IOException {
        log.info("保存评估数据集到JSON文件: {}", filePath);

        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            log.info("创建目录: {}", parentDir.getAbsolutePath());
        }

        EvaluationDataset dataset = new EvaluationDataset();
        dataset.setTestCases(testCases);

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, dataset);
        log.info("成功保存 {} 个测试用例", testCases.size());
    }

    public List<EvaluationTestCase> createSampleDataset() {
        log.info("创建示例评估数据集");

        List<EvaluationTestCase> testCases = new ArrayList<>();

        EvaluationTestCase case1 = new EvaluationTestCase();
        case1.setId("test_001");
        case1.setQuestion("什么是RAG?");
        case1.setGroundTruthAnswer("RAG（Retrieval-Augmented Generation）是一种结合检索和生成的AI技术，通过从知识库中检索相关信息来增强生成模型的回答质量。");
        case1.setRelevantDocuments(List.of("doc_rag_intro.pdf", "doc_rag_architecture.pdf"));
        case1.setRetrievedDocuments(List.of("doc_rag_intro.pdf", "doc_rag_architecture.pdf", "doc_rag_benefits.pdf"));
        case1.setRetrievedAnswer("RAG（Retrieval-Augmented Generation）是一种结合检索和生成的AI技术。它通过从知识库中检索相关信息，然后将这些信息作为上下文提供给生成模型，从而增强生成模型的回答质量和准确性。");
        testCases.add(case1);

        EvaluationTestCase case2 = new EvaluationTestCase();
        case2.setId("test_002");
        case2.setQuestion("Spring AI的主要功能是什么?");
        case2.setGroundTruthAnswer("Spring AI是一个为Spring生态系统设计的AI框架，提供了与大语言模型集成的简化API，支持聊天、嵌入、RAG等功能。");
        case2.setRelevantDocuments(List.of("doc_spring_ai_overview.pdf", "doc_spring_ai_features.pdf"));
        case2.setRetrievedDocuments(List.of("doc_spring_ai_overview.pdf", "doc_spring_ai_features.pdf", "doc_spring_ai_quickstart.pdf"));
        case2.setRetrievedAnswer("Spring AI是一个为Spring生态系统设计的AI框架，主要功能包括：提供与大语言模型集成的简化API、支持聊天对话、文本嵌入、RAG检索增强生成等功能，让开发者能够轻松在Spring应用中集成AI能力。");
        testCases.add(case2);

        EvaluationTestCase case3 = new EvaluationTestCase();
        case3.setId("test_003");
        case3.setQuestion("如何优化向量检索的性能?");
        case3.setGroundTruthAnswer("优化向量检索性能的方法包括：1）使用高效的索引算法如HNSW；2）调整向量维度；3）优化相似度计算；4）使用缓存策略；5）分片和并行检索。");
        case3.setRelevantDocuments(List.of("doc_vector_optimization.pdf", "doc_hnsw_index.pdf", "doc_cache_strategy.pdf"));
        case3.setRetrievedDocuments(List.of("doc_vector_optimization.pdf", "doc_hnsw_index.pdf", "doc_cache_strategy.pdf", "doc_parallel_search.pdf"));
        case3.setRetrievedAnswer("优化向量检索性能主要有以下几种方法：1）使用高效的索引算法，如HNSW（Hierarchical Navigable Small World）；2）合理调整向量维度，在精度和性能之间找到平衡；3）优化相似度计算算法；4）使用缓存策略减少重复计算；5）采用分片和并行检索技术提高吞吐量。");
        testCases.add(case3);

        log.info("创建了 {} 个示例测试用例", testCases.size());

        return testCases;
    }

    public EvaluationTestCase createTestCase(String id, String question, String groundTruth,
            List<String> relevantDocs) {
        EvaluationTestCase testCase = new EvaluationTestCase();
        testCase.setId(id);
        testCase.setQuestion(question);
        testCase.setGroundTruthAnswer(groundTruth);
        testCase.setRelevantDocuments(relevantDocs);
        return testCase;
    }

    public static class EvaluationDataset {

        private List<EvaluationTestCase> testCases;

        public List<EvaluationTestCase> getTestCases() {
            return testCases;
        }

        public void setTestCases(List<EvaluationTestCase> testCases) {
            this.testCases = testCases;
        }
    }
}
