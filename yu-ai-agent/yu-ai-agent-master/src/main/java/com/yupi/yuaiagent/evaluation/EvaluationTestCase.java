package com.yupi.yuaiagent.evaluation;

import lombok.Data;

import java.util.List;

@Data
public class EvaluationTestCase {

    private String id;

    private String question;

    private String groundTruthAnswer;

    private List<String> relevantDocuments;

    private String retrievedAnswer;

    private List<String> retrievedDocuments;

    private List<String> retrievedDocumentContents;

    private double retrievalScore;

    private double generationScore;

    private double overallScore;

    private String evaluationDetails;
}
