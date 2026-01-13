package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.evaluation.EvaluationResult;
import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import com.yupi.yuaiagent.evaluation.service.RAGEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/evaluation")
public class RAGEvaluationController {

    @Autowired

    private RAGEvaluationService evaluationService;

    @PostMapping("/run")
    public EvaluationResult runEvaluation(@RequestBody List<EvaluationTestCase> testCases) {
        log.info("收到RAG评估请求，测试用例数: {}", testCases.size());

        return evaluationService.evaluateTestCases(testCases);
    }

    @PostMapping("/run-with-llm")
    public EvaluationResult runEvaluationWithLLMJudge(@RequestBody List<EvaluationTestCase> testCases) {

        log.info("收到LLM-as-a-Judge评估请求，测试用例数: {}", testCases.size());
        return evaluationService.evaluateWithLLMJudge(testCases);
    }

    @PostMapping("/run-from-dataset")
    public EvaluationResult runEvaluationFromDataset(@RequestParam String datasetPath) {
        try {
            log.info("从数据集运行评估: {}", datasetPath);
            return evaluationService.evaluateFromDataset(datasetPath);
        } catch (IOException e) {

            log.error("加载数据集失败", e);
            throw new RuntimeException("加载数据集失败: " + e.getMessage());

        }
    }

    @PostMapping("/run-full")
    public EvaluationResult runFullEvaluation(@RequestParam String datasetPath,
            @RequestParam(defaultValue = "./evaluation_reports") String outputDir) {
        try {
            log.info("运行完整评估流程");

            return evaluationService.runFullEvaluation(datasetPath, outputDir);
        } catch (IOException e) {
            log.error("运行完整评估失败", e);
            throw new RuntimeException("运行完整评估失败: " + e.getMessage());
        }
    }

    @PostMapping("/run-full-data")
    public EvaluationResult runFullEvaluationData(@RequestBody List<EvaluationTestCase> testCases,
            @RequestParam(defaultValue = "./evaluation_reports") String outputDir) {
        log.info("运行完整评估流程（直接数据）: testCases={}", testCases.size());
        try {
            return evaluationService.runFullEvaluationData(testCases, outputDir);
        } catch (IOException e) {
            log.error("运行完整评估失败", e);
            throw new RuntimeException("运行完整评估失败: " + e.getMessage());
        }
    }

    @PostMapping("/evaluate-with-rag-pipeline")
    public EvaluationResult evaluateWithRAGPipeline(@RequestParam String datasetPath,
            @RequestParam(defaultValue = "5") int topK) {
        try {

            log.info("使用RAG流程进行端到端评估: datasetPath={}, topK={}", datasetPath, topK);
            return evaluationService.evaluateWithRAGPipeline(datasetPath, topK);
        } catch (IOException e) {
            log.error("RAG流程评估失败", e);
            throw new RuntimeException("RAG流程评估失败: " + e.getMessage());
        }
    }

    @PostMapping("/evaluate-with-rag-pipeline-data")
    public EvaluationResult evaluateWithRAGPipelineData(@RequestBody List<EvaluationTestCase> testCases,
            @RequestParam(defaultValue = "5") int topK) {
        log.info("使用RAG流程进行端到端评估（直接数据）: testCases={}, topK={}", testCases.size(), topK);
        return evaluationService.evaluateWithRAGPipelineData(testCases, topK);
    }

    @PostMapping("/evaluate-retrieval-only")
    public EvaluationResult evaluateRetrievalOnly(@RequestParam String datasetPath,
            @RequestParam(defaultValue = "5") int topK) {
        try {
            log.info("仅评估检索质量: datasetPath={}, topK={}", datasetPath, topK);
            return evaluationService.evaluateRetrievalOnly(datasetPath, topK);
        } catch (IOException e) {
            log.error("检索质量评估失败", e);
            throw new RuntimeException("检索质量评估失败: " + e.getMessage());
        }
    }

    @PostMapping("/evaluate-retrieval-only-data")
    public EvaluationResult evaluateRetrievalOnlyData(@RequestBody List<EvaluationTestCase> testCases,
            @RequestParam(defaultValue = "5") int topK) {
        log.info("仅评估检索质量（直接数据）: testCases={}, topK={}", testCases.size(), topK);
        return evaluationService.evaluateRetrievalOnlyData(testCases, topK);
    }

    @PostMapping("/create-sample-dataset")
    public String createSampleDataset(@RequestParam(defaultValue = "./sample_dataset.json") String outputPath) {
        try {
            log.info("创建示例数据集");
            evaluationService.createAndSaveSampleDataset(outputPath);
            return "示例数据集已创建: " + outputPath;
        } catch (IOException e) {
            log.error("创建示例数据集失败", e);
            throw new RuntimeException("创建示例数据集失败: " + e.getMessage());
        }
    }

    @GetMapping("/metrics")
    public String getAvailableMetrics() {
        StringBuilder sb = new StringBuilder();
        sb.append("可用的评估指标:\n\n");

        sb.append("检索质量指标:\n");
        sb.append("- Precision@K: 前K个结果中相关文档的比例\n");
        sb.append("- Recall@K: 前K个结果覆盖了多少相关文档\n");
        sb.append("- MRR: 第一个相关文档的排名倒数\n");
        sb.append("- NDCG@K: 考虑排序质量的归一化指标\n\n");

        sb.append("生成质量指标:\n");
        sb.append("- Faithfulness: 答案是否忠实于检索到的文档\n");
        sb.append("- Answer Relevance: 答案是否回答了用户问题\n");
        sb.append("- Context Precision: 检索到的文档是否包含答案\n");
        sb.append("- Context Recall: 检索到的文档是否覆盖了所有必要信息\n");

        return sb.toString();
    }
}
