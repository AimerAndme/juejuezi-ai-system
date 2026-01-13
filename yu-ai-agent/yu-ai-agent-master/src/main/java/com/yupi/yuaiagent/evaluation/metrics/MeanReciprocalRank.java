package com.yupi.yuaiagent.evaluation.metrics;

import com.yupi.yuaiagent.evaluation.EvaluationMetric;
import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MeanReciprocalRank implements EvaluationMetric {

    @Override
    public String getName() {
        return "MRR";
    }

    @Override
    public double calculate(EvaluationTestCase testCase) {
        List<String> retrievedDocs = testCase.getRetrievedDocuments();
        List<String> relevantDocs = testCase.getRelevantDocuments();
        List<String> retrievedContents = testCase.getRetrievedDocumentContents();

        if (retrievedDocs == null || retrievedDocs.isEmpty()) {
            return 0.0;
        }

        if (relevantDocs == null || relevantDocs.isEmpty()) {
            return 0.0;
        }

        for (int i = 0; i < retrievedDocs.size(); i++) {
            String retrievedContent = retrievedContents != null && i < retrievedContents.size()
                    ? retrievedContents.get(i)
                    : retrievedDocs.get(i);

            for (String relevantDoc : relevantDocs) {
                if (isContentMatch(retrievedContent, relevantDoc)) {
                    return 1.0 / (i + 1);
                }
            }
        }

        return 0.0;
    }

    private boolean isContentMatch(String content1, String content2) {
        if (content1 == null || content2 == null) {
            return false;
        }

        String normalized1 = content1.trim().toLowerCase();
        String normalized2 = content2.trim().toLowerCase();

        if (normalized1.equals(normalized2)) {
            return true;
        }

        if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) {
            return true;
        }

        return false;
    }

    @Override
    public String getDescription() {
        return "第一个相关文档的排名倒数（基于内容匹配）";
    }
}
