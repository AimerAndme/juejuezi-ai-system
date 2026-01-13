package com.yupi.yuaiagent.evaluation;

import java.util.List;

public interface EvaluationMetric {
    String getName();
    double calculate(EvaluationTestCase testCase);
    String getDescription();
}
