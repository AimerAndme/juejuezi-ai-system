# RAG评估系统使用指南

## 📖 概述

本项目实现了一个完整的RAG（Retrieval-Augmented Generation）系统评估框架，支持多种评估指标和报告生成。

## 🎯 核心功能

### 1. 评估指标

#### 检索质量指标
- **Precision@K**: 前K个检索结果中相关文档的比例
- **Recall@K**: 前K个检索结果覆盖了多少相关文档
- **MRR (Mean Reciprocal Rank)**: 第一个相关文档的排名倒数
- **NDCG@K (Normalized Discounted Cumulative Gain)**: 考虑排序质量的归一化指标

#### 生成质量指标
- **Faithfulness**: 生成内容是否基于检索到的文档（忠实度）
- **Answer Relevance**: 答案是否回答了用户问题
- **Context Precision**: 检索到的文档是否包含答案
- **Context Recall**: 检索到的文档是否覆盖了所有必要信息

### 2. LLM-as-a-Judge评估
使用大语言模型作为评估器，提供更准确的语义评估：
- 忠实度评估
- 相关性评估
- 上下文精度评估

### 3. 报告生成
- HTML格式的可视化报告（带进度条和评级）
- Markdown格式的文本报告

## 🚀 快速开始

### 1. 创建示例数据集

```bash
# 通过API创建示例数据集
curl -X POST "http://localhost:8080/api/evaluation/create-sample-dataset?outputPath=./sample_dataset.json"
```

### 2. 运行评估

```bash
# 基础评估（仅使用传统指标）
curl -X POST "http://localhost:8080/api/evaluation/run-from-dataset?datasetPath=./sample_dataset.json"

# 使用LLM-as-a-Judge评估
curl -X POST "http://localhost:8080/api/evaluation/run-full?datasetPath=./sample_dataset.json&outputDir=./evaluation_reports"
```

### 3. 查看报告

评估完成后，会在指定目录生成HTML和Markdown报告：
- `rag_evaluation_[eval_id].html` - 可视化HTML报告
- `rag_evaluation_[eval_id].md` - Markdown文本报告

## 📝 数据集格式

评估数据集使用JSON格式，结构如下：

```json
{
  "testCases": [
    {
      "id": "test_001",
      "question": "什么是RAG?",
      "groundTruthAnswer": "RAG是一种结合检索和生成的AI技术...",
      "relevantDocuments": ["doc_rag_intro.pdf", "doc_rag_architecture.pdf"],
      "retrievedAnswer": "RAG（Retrieval-Augmented Generation）是...",
      "retrievedDocuments": ["doc_rag_intro.pdf", "doc_rag_usage.pdf"]
    }
  ]
}
```

### 字段说明
- `id`: 测试用例唯一标识
- `question`: 用户问题
- `groundTruthAnswer`: 标准答案（可选，用于对比）
- `relevantDocuments`: 相关文档列表（用于评估检索质量）
- `retrievedAnswer`: RAG系统生成的答案（用于评估生成质量）
- `retrievedDocuments`: RAG系统检索到的文档列表（用于评估检索质量）

## 🔧 编程方式使用

### 示例1：基础评估

```java
@Autowired
private RAGEvaluationService evaluationService;

// 创建测试用例
List<EvaluationTestCase> testCases = new ArrayList<>();
EvaluationTestCase testCase = new EvaluationTestCase();
testCase.setId("test_001");
testCase.setQuestion("什么是RAG?");
testCase.setRelevantDocuments(List.of("doc1.pdf", "doc2.pdf"));
testCase.setRetrievedDocuments(List.of("doc1.pdf", "doc3.pdf"));
testCase.setRetrievedAnswer("RAG是一种检索增强生成技术...");
testCases.add(testCase);

// 运行评估
EvaluationResult result = evaluationService.evaluateTestCases(testCases);

// 生成报告
evaluationService.generateAndSaveReports(result, "./reports");
```

### 示例2：使用LLM-as-a-Judge

```java
// 使用LLM进行更准确的评估
EvaluationResult result = evaluationService.evaluateWithLLMJudge(testCases);
```

### 示例3：从文件加载并评估

```java
// 从JSON文件加载数据集
EvaluationResult result = evaluationService.evaluateFromDataset("./dataset.json");

// 运行完整评估流程（包含报告生成）
EvaluationResult result = evaluationService.runFullEvaluation(
    "./dataset.json", 
    "./evaluation_reports"
);
```

## 📊 评估结果解读

### 指标评分标准

| 分数范围 | 评级 | 说明 |
|---------|------|------|
| 0.9 - 1.0 | 优秀 ⭐⭐⭐⭐⭐ | 表现极佳，无需优化 |
| 0.8 - 0.9 | 良好 ⭐⭐⭐⭐ | 表现良好，可微调优化 |
| 0.7 - 0.8 | 中等 ⭐⭐⭐ | 表现一般，需要改进 |
| 0.6 - 0.7 | 及格 ⭐⭐ | 勉强达标，急需优化 |
| 0.0 - 0.6 | 需改进 ⭐ | 表现不佳，必须优化 |

### 改进建议

#### Precision@K较低
- 优化检索算法
- 调整向量维度
- 改进文档切分策略
- 优化查询扩展

#### Recall@K较低
- 增加检索结果数量
- 优化文档索引
- 改进查询重写
- 使用混合检索

#### Faithfulness较低
- 检查提示词设计
- 确保模型基于上下文回答
- 添加约束条件
- 使用更强的模型

#### Answer Relevance较低
- 优化检索查询
- 调整生成模型参数
- 改进上下文相关性
- 使用更好的检索策略

## 🏗️ 架构设计

### 核心组件

1. **EvaluationMetric**: 评估指标接口
2. **RAGEvaluator**: 评估器，协调所有指标计算
3. **EvaluationDatasetManager**: 数据集管理器
4. **LLMJudgeEvaluator**: LLM评估器
5. **EvaluationReportGenerator**: 报告生成器
6. **RAGEvaluationService**: 评估服务
7. **RAGEvaluationController**: REST API控制器

### 扩展自定义指标

```java
// 实现自定义评估指标
public class CustomMetric implements EvaluationMetric {
    
    @Override
    public String getName() {
        return "CustomMetric";
    }
    
    @Override
    public double calculate(EvaluationTestCase testCase) {
        // 实现你的评估逻辑
        return 0.0;
    }
    
    @Override
    public String getDescription() {
        return "自定义指标描述";
    }
}

// 注册自定义指标
@Autowired
private RAGEvaluator ragEvaluator;

ragEvaluator.addRetrievalMetric(new CustomMetric());
```

## 🔌 API端点

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/evaluation/run` | POST | 运行基础评估 |
| `/api/evaluation/run-with-llm` | POST | 运行LLM评估 |
| `/api/evaluation/run-from-dataset` | POST | 从数据集运行评估 |
| `/api/evaluation/run-full` | POST | 运行完整评估流程 |
| `/api/evaluation/create-sample-dataset` | POST | 创建示例数据集 |
| `/api/evaluation/metrics` | GET | 获取可用指标列表 |

## 📚 参考资料

- [RAGAS](https://github.com/explodinggradients/ragas) - RAG评估框架
- [DeepEval](https://github.com/confident-ai/deepeval) - LLM评估工具
- [TruLens](https://github.com/truera/trulens) - LLM应用评估

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个评估系统！
