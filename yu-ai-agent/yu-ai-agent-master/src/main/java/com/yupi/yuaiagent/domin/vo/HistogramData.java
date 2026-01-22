package com.yupi.yuaiagent.domin.vo;

import com.yupi.yuaiagent.domin.entity.HistogramRange;
import lombok.Data;

import java.util.List;

@Data
public class HistogramData {

    /**
     * 区间数据
     */
    private List<HistogramRange> interval;

    /**
     * 总数量
     */
    private Long totalCount;

    /**
     * 无效数量
     */
    private Long invalidCount;

    /**
     * 平均值
     */
    private Double mean;

    /**
     * 标准差
     */
    private Double standardDeviation;

    /**
     * 变异系数
     */
    private Double coefficientOfVariation;

    /**
     * 最大值
     */
    private Double maxValue;

    /**
     * 上四分位数
     */
    private Double upperQuartile;

    /**
     * 下四分位数
     */
    private Double lowerQuartile;

    /**
     * 中位数
     */
    private Double median;

    /**
     * 最小值
     */
    private Double minValue;

    /**
     * 均值减去2倍标准差
     */
    private Double meanMinus2Sigma;

    /**
     * 均值加上2倍标准差
     */
    private Double meanPlus2Sigma;

    /**
     * 对数均值
     */
    private Double logMean;

    /**
     * 对数方差
     */
    private Double logVariance;

    /**
     * 形状参数
     */
    private Double shapeParameter;

    /**
     * 尺度参数
     */
    private Double scaleParameter;
}
