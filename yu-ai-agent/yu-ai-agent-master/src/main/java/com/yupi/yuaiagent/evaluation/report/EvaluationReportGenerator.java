package com.yupi.yuaiagent.evaluation.report;

import com.yupi.yuaiagent.evaluation.EvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class EvaluationReportGenerator {
    
    public String generateHtmlReport(EvaluationResult result) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='zh-CN'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("    <title>RAG评估报告</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
        html.append("        .container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        h1 { color: #333; border-bottom: 3px solid #4CAF50; padding-bottom: 10px; }\n");
        html.append("        h2 { color: #555; margin-top: 30px; }\n");
        html.append("        .info { background-color: #e7f3fe; padding: 15px; border-left: 4px solid #2196F3; margin: 20px 0; }\n");
        html.append("        .metric-section { margin: 20px 0; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
        html.append("        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("        th { background-color: #4CAF50; color: white; }\n");
        html.append("        tr:hover { background-color: #f5f5f5; }\n");
        html.append("        .score-high { color: #4CAF50; font-weight: bold; }\n");
        html.append("        .score-medium { color: #FF9800; font-weight: bold; }\n");
        html.append("        .score-low { color: #f44336; font-weight: bold; }\n");
        html.append("        .progress-bar { width: 100%; background-color: #ddd; border-radius: 5px; margin: 5px 0; }\n");
        html.append("        .progress-fill { height: 20px; border-radius: 5px; transition: width 0.3s; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class='container'>\n");
        html.append("        <h1>📊 RAG系统评估报告</h1>\n");
        
        html.append("        <div class='info'>\n");
        html.append("            <p><strong>评估ID:</strong> ").append(result.getEvaluationId()).append("</p>\n");
        html.append("            <p><strong>评估时间:</strong> ").append(formatTimestamp(result.getTimestamp())).append("</p>\n");
        html.append("            <p><strong>测试用例数:</strong> ").append(result.getTotalTestCases()).append("</p>\n");
        html.append("        </div>\n");
        
        html.append("        <h2>📈 评估指标概览</h2>\n");
        html.append("        <table>\n");
        html.append("            <tr>\n");
        html.append("                <th>指标名称</th>\n");
        html.append("                <th>得分</th>\n");
        html.append("                <th>进度</th>\n");
        html.append("                <th>评级</th>\n");
        html.append("            </tr>\n");
        
        for (Map.Entry<String, Double> entry : result.getMetricAverages().entrySet()) {
            String metricName = entry.getKey();
            double score = entry.getValue();
            String scoreClass = getScoreClass(score);
            String grade = getGrade(score);
            
            html.append("            <tr>\n");
            html.append("                <td>").append(metricName).append("</td>\n");
            html.append("                <td class='").append(scoreClass).append("'>").append(String.format("%.4f", score)).append("</td>\n");
            html.append("                <td>\n");
            html.append("                    <div class='progress-bar'>\n");
            html.append("                        <div class='progress-fill' style='width: ").append(score * 100).append("%; background-color: ").append(getColor(score)).append(";'></div>\n");
            html.append("                    </div>\n");
            html.append("                </td>\n");
            html.append("                <td>").append(grade).append("</td>\n");
            html.append("            </tr>\n");
        }
        
        html.append("        </table>\n");
        
        html.append("        <h2>📝 详细分析</h2>\n");
        html.append("        <div class='metric-section'>\n");
        html.append("            <h3>检索质量指标</h3>\n");
        html.append("            <p>这些指标评估检索系统的性能：</p>\n");
        html.append("            <ul>\n");
        html.append("                <li><strong>Precision@K:</strong> 前K个结果中相关文档的比例</li>\n");
        html.append("                <li><strong>Recall@K:</strong> 前K个结果覆盖了多少相关文档</li>\n");
        html.append("                <li><strong>MRR:</strong> 第一个相关文档的排名倒数</li>\n");
        html.append("                <li><strong>NDCG@K:</strong> 考虑排序质量的归一化指标</li>\n");
        html.append("            </ul>\n");
        html.append("        </div>\n");
        
        html.append("        <div class='metric-section'>\n");
        html.append("            <h3>生成质量指标</h3>\n");
        html.append("            <p>这些指标评估生成答案的质量：</p>\n");
        html.append("            <ul>\n");
        html.append("                <li><strong>Faithfulness:</strong> 答案是否忠实于检索到的文档</li>\n");
        html.append("                <li><strong>Answer Relevance:</strong> 答案是否回答了用户问题</li>\n");
        html.append("                <li><strong>Context Precision:</strong> 检索到的文档是否包含答案</li>\n");
        html.append("                <li><strong>Context Recall:</strong> 检索到的文档是否覆盖了所有必要信息</li>\n");
        html.append("            </ul>\n");
        html.append("        </div>\n");
        
        html.append("        <h2>💡 改进建议</h2>\n");
        html.append("        <div class='metric-section'>\n");
        html.append("            <p>基于评估结果，建议关注以下方面：</p>\n");
        html.append("            <ul>\n");
        html.append("                <li>如果Precision@K较低，考虑优化检索算法或调整向量维度</li>\n");
        html.append("                <li>如果Recall@K较低，考虑增加检索结果数量或优化文档切分</li>\n");
        html.append("                <li>如果Faithfulness较低，检查提示词设计，确保模型基于上下文回答</li>\n");
        html.append("                <li>如果Answer Relevance较低，优化检索查询或调整生成模型参数</li>\n");
        html.append("            </ul>\n");
        html.append("        </div>\n");
        
        html.append("        <p style='text-align: center; color: #666; margin-top: 40px;'>").append(formatTimestamp(result.getTimestamp())).append("</p>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    public String generateMarkdownReport(EvaluationResult result) {
        StringBuilder md = new StringBuilder();
        
        md.append("# 📊 RAG系统评估报告\n\n");
        md.append("## 基本信息\n\n");
        md.append("- **评估ID:** ").append(result.getEvaluationId()).append("\n");
        md.append("- **评估时间:** ").append(formatTimestamp(result.getTimestamp())).append("\n");
        md.append("- **测试用例数:** ").append(result.getTotalTestCases()).append("\n\n");
        
        md.append("## 📈 评估指标\n\n");
        md.append("| 指标名称 | 得分 | 评级 |\n");
        md.append("|---------|------|------|\n");
        
        for (Map.Entry<String, Double> entry : result.getMetricAverages().entrySet()) {
            String metricName = entry.getKey();
            double score = entry.getValue();
            String grade = getGrade(score);
            md.append(String.format("| %s | %.4f | %s |\n", metricName, score, grade));
        }
        
        md.append("\n## 📝 指标说明\n\n");
        md.append("### 检索质量指标\n\n");
        md.append("- **Precision@K:** 前K个结果中相关文档的比例\n");
        md.append("- **Recall@K:** 前K个结果覆盖了多少相关文档\n");
        md.append("- **MRR:** 第一个相关文档的排名倒数\n");
        md.append("- **NDCG@K:** 考虑排序质量的归一化指标\n\n");
        
        md.append("### 生成质量指标\n\n");
        md.append("- **Faithfulness:** 答案是否忠实于检索到的文档\n");
        md.append("- **Answer Relevance:** 答案是否回答了用户问题\n");
        md.append("- **Context Precision:** 检索到的文档是否包含答案\n");
        md.append("- **Context Recall:** 检索到的文档是否覆盖了所有必要信息\n\n");
        
        md.append("## 💡 改进建议\n\n");
        md.append("基于评估结果，建议关注以下方面：\n\n");
        md.append("- 如果Precision@K较低，考虑优化检索算法或调整向量维度\n");
        md.append("- 如果Recall@K较低，考虑增加检索结果数量或优化文档切分\n");
        md.append("- 如果Faithfulness较低，检查提示词设计，确保模型基于上下文回答\n");
        md.append("- 如果Answer Relevance较低，优化检索查询或调整生成模型参数\n");
        
        return md.toString();
    }
    
    public void saveHtmlReport(EvaluationResult result, String filePath) throws IOException {
        String html = generateHtmlReport(result);
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(html);
        }
        log.info("HTML报告已保存到: {}", filePath);
    }
    
    public void saveMarkdownReport(EvaluationResult result, String filePath) throws IOException {
        String markdown = generateMarkdownReport(result);
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(markdown);
        }
        log.info("Markdown报告已保存到: {}", filePath);
    }
    
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
    
    private String getScoreClass(double score) {
        if (score >= 0.8) return "score-high";
        if (score >= 0.6) return "score-medium";
        return "score-low";
    }
    
    private String getGrade(double score) {
        if (score >= 0.9) return "优秀 ⭐⭐⭐⭐⭐";
        if (score >= 0.8) return "良好 ⭐⭐⭐⭐";
        if (score >= 0.7) return "中等 ⭐⭐⭐";
        if (score >= 0.6) return "及格 ⭐⭐";
        return "需改进 ⭐";
    }
    
    private String getColor(double score) {
        if (score >= 0.8) return "#4CAF50";
        if (score >= 0.6) return "#FF9800";
        return "#f44336";
    }
}
