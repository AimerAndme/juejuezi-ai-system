package com.yupi.yuaiagent.domin.entity;

import lombok.Data;

@Data
public class HistogramStatistics {
    private Long totalCount;

    private Long invalidCount;

    private Double mean;

    private Double standardDeviation;

    private Double coefficientOfVariation;

    private Double maxValue;

    private Double upperQuartile;

    private Double lowerQuartile;

    private Double median;

    private Double minValue;

    private Double meanMinus2Sigma;

    private Double meanPlus2Sigma;

    private Double logMean;

    private Double logVariance;

    private Double shapeParameter;

    private Double scaleParameter;
}