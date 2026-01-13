package com.yupi.yuaiagent.evaluation.service;

import com.yupi.yuaiagent.evaluation.EvaluationMetric;
import com.yupi.yuaiagent.evaluation.EvaluationResult;
import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import com.yupi.yuaiagent.evaluation.RAGEvaluator;
import com.yupi.yuaiagent.evaluation.dataset.EvaluationDatasetManager;
import com.yupi.yuaiagent.evaluation.llm.LLMJudgeEvaluator;
import com.yupi.yuaiagent.evaluation.report.EvaluationReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RAGEvaluationService {

    @Autowired
    private RAGEvaluator ragEvaluator;

    @Autowired
    private EvaluationDatasetManager datasetManager;

    @Autowired
    private LLMJudgeEvaluator llmJudgeEvaluator;

    @Autowired
    private EvaluationReportGenerator reportGenerator;

    @Autowired
    private RAGPipelineService ragPipelineService;

    public EvaluationResult evaluateFromDataset(String datasetPath) throws IOException {
        log.info("开始从数据集进行RAG评估: {}", datasetPath);

        List<EvaluationTestCase> testCases = datasetManager.loadDatasetFromJson(datasetPath);

        return evaluateTestCases(testCases);
    }

    public EvaluationResult evaluateTestCases(List<EvaluationTestCase> testCases) {
        log.info("开始评估 {} 个测试用例", testCases.size());

        EvaluationResult result = ragEvaluator.evaluate(testCases);

        log.info("RAG评估完成: {}", result.getSummary());

        return result;
    }

    public EvaluationResult evaluateWithLLMJudge(List<EvaluationTestCase> testCases) {
        log.info("开始使用LLM-as-a-Judge进行评估");

        for (EvaluationTestCase testCase : testCases) {
            if (testCase.getRetrievedAnswer() != null && testCase.getRetrievedDocuments() != null) {
                llmJudgeEvaluator.evaluateTestCase(testCase);
            }
        }

        return ragEvaluator.evaluate(testCases);
    }

    public void generateAndSaveReports(EvaluationResult result, String outputDir) throws IOException {
        log.info("生成评估报告到目录: {}", outputDir);

        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        String baseName = "rag_evaluation_" + result.getEvaluationId();

        String htmlPath = outputPath.resolve(baseName + ".html").toString();
        reportGenerator.saveHtmlReport(result, htmlPath);

        String mdPath = outputPath.resolve(baseName + ".md").toString();
        reportGenerator.saveMarkdownReport(result, mdPath);

        log.info("评估报告已生成:");
        log.info("  - HTML: {}", htmlPath);
        log.info("  - Markdown: {}", mdPath);
    }

    public void createAndSaveSampleDataset(String outputPath) throws IOException {
        log.info("创建示例数据集: {}", outputPath);

        List<EvaluationTestCase> testCases = datasetManager.createSampleDataset();
        datasetManager.saveDatasetToJson(testCases, outputPath);

        log.info("示例数据集已保存到: {}", outputPath);
    }

    public EvaluationResult runFullEvaluation(String datasetPath, String outputDir) throws IOException {
        log.info("运行完整RAG评估流程");

        List<EvaluationTestCase> testCases = datasetManager.loadDatasetFromJson(datasetPath);

        EvaluationResult result = evaluateWithLLMJudge(testCases);

        generateAndSaveReports(result, outputDir);

        return result;
    }

    public EvaluationResult runFullEvaluationData(List<EvaluationTestCase> testCases, String outputDir) throws IOException {
        log.info("运行完整RAG评估流程（直接数据）");

        EvaluationResult result = evaluateWithLLMJudge(testCases);

        generateAndSaveReports(result, outputDir);

        return result;
    }

    public EvaluationResult evaluateWithRAGPipeline(String datasetPath, int topK) throws IOException {
        log.info("使用RAG流程进行端到端评估: datasetPath={}, topK={}", datasetPath, topK);

        List<EvaluationTestCase> testCases = datasetManager.loadDatasetFromJson(datasetPath);

        log.info("执行RAG流程（检索+生成）...");
        List<EvaluationTestCase> enrichedTestCases = ragPipelineService.runRAGPipeline(testCases, topK);

        log.info("执行评估...");
        EvaluationResult result = ragEvaluator.evaluate(enrichedTestCases);

        return result;
    }

    public EvaluationResult evaluateWithRAGPipelineData(List<EvaluationTestCase> testCases, int topK) {
        log.info("使用RAG流程进行端到端评估（直接数据）: testCases={}, topK={}", testCases.size(), topK);

        log.info("执行RAG流程（检索+生成）...");
        List<EvaluationTestCase> enrichedTestCases = ragPipelineService.runRAGPipeline(testCases, topK);

        log.info("执行评估...");
        EvaluationResult result = ragEvaluator.evaluate(enrichedTestCases);

        return result;
    }

    public EvaluationResult evaluateRetrievalOnly(String datasetPath, int topK) throws IOException {
        log.info("仅评估检索质量: datasetPath={}, topK={}", datasetPath, topK);

        List<EvaluationTestCase> testCases = datasetManager.loadDatasetFromJson(datasetPath);

        log.info("执行检索...");
        List<EvaluationTestCase> enrichedTestCases = ragPipelineService.runRetrievalOnly(testCases, topK);

        log.info("执行检索指标评估...");
        EvaluationResult result = new EvaluationResult();
        result.setEvaluationId("eval_retrieval_" + System.currentTimeMillis());
        result.setTimestamp(System.currentTimeMillis());
        result.setTotalTestCases(enrichedTestCases.size());

        for (EvaluationMetric metric : ragEvaluator.getRetrievalMetrics()) {
            double totalScore = 0.0;
            int validCases = 0;

            for (EvaluationTestCase testCase : enrichedTestCases) {
                if (testCase.getRetrievedDocuments() != null
                        && testCase.getRelevantDocuments() != null) {
                    double score = metric.calculate(testCase);
                    totalScore += score;
                    validCases++;
                }
            }

            if (validCases > 0) {
                double averageScore = totalScore / validCases;
                result.addMetricScore(metric.getName(), averageScore);
                result.calculateAverage(metric.getName(), totalScore, validCases);

                log.info("{}: {:.4f}", metric.getName(), averageScore);
            }
        }

        result.setSummary(String.format("检索质量评估完成\n测试用例数: %d\nTop-K: %d",
                result.getTotalTestCases(), topK));

        return result;
    }

    public EvaluationResult evaluateRetrievalOnlyData(List<EvaluationTestCase> testCases, int topK) {
        log.info("仅评估检索质量（直接数据）: testCases={}, topK={}", testCases.size(), topK);

        log.info("执行检索...");
        List<EvaluationTestCase> enrichedTestCases = ragPipelineService.runRetrievalOnly(testCases, topK);

        log.info("自动标注相关文档...");
        autoAnnotateRelevantDocuments(enrichedTestCases);

        log.info("执行检索指标评估...");
        EvaluationResult result = new EvaluationResult();
        result.setEvaluationId("eval_retrieval_" + System.currentTimeMillis());
        result.setTimestamp(System.currentTimeMillis());
        result.setTotalTestCases(enrichedTestCases.size());

        for (EvaluationMetric metric : ragEvaluator.getRetrievalMetrics()) {
            double totalScore = 0.0;
            int validCases = 0;

            for (EvaluationTestCase testCase : enrichedTestCases) {
                if (testCase.getRetrievedDocuments() != null
                        && testCase.getRelevantDocuments() != null) {
                    double score = metric.calculate(testCase);
                    totalScore += score;
                    validCases++;
                }
            }

            if (validCases > 0) {
                double averageScore = totalScore / validCases;
                result.addMetricScore(metric.getName(), averageScore);
                result.calculateAverage(metric.getName(), totalScore, validCases);

                log.info("{}: {:.4f}", metric.getName(), averageScore);
            }
        }

        result.setSummary(String.format("检索质量评估完成\n测试用例数: %d\nTop-K: %d",
                result.getTotalTestCases(), topK));

        return result;
    }

    private void autoAnnotateRelevantDocuments(List<EvaluationTestCase> testCases) {
        for (EvaluationTestCase testCase : testCases) {
            if (testCase.getRetrievedDocuments() == null || testCase.getRetrievedDocuments().isEmpty()) {
                testCase.setRelevantDocuments(new ArrayList<>());
                continue;
            }

            List<String> retrievedDocIds = testCase.getRetrievedDocuments();
            List<String> retrievedDocContents = testCase.getRetrievedDocumentContents();
            List<String> relevantDocContents = new ArrayList<>();

            if (retrievedDocContents != null && !retrievedDocContents.isEmpty()) {
                for (int i = 0; i < Math.min(3, retrievedDocContents.size()); i++) {
                    relevantDocContents.add(retrievedDocContents.get(i));
                }
            }

            testCase.setRelevantDocuments(relevantDocContents);
            log.debug("自动标注相关文档: question={}, relevantCount={}", testCase.getQuestion(), relevantDocContents.size());
        }
    }
}
