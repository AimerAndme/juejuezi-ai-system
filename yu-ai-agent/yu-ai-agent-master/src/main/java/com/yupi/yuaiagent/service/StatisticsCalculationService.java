package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.HistogramStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class StatisticsCalculationService {

    public HistogramStatistics calculateAllStatistics(List<Double> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return new HistogramStatistics();
        }

        List<Double> validData = new ArrayList<>();
        long invalidCount = 0;

        for (Double value : dataList) {
            if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
                invalidCount++;
            } else {
                validData.add(value);
            }
        }

        if (validData.isEmpty()) {
            return new HistogramStatistics();
        }

        Collections.sort(validData);

        HistogramStatistics stats = new HistogramStatistics();
        stats.setTotalCount((long) dataList.size());
        stats.setInvalidCount(invalidCount);
        stats.setMinValue(validData.get(0));
        stats.setMaxValue(validData.get(validData.size() - 1));
        stats.setMean(calculateMean(validData));
        stats.setStandardDeviation(calculateStandardDeviation(validData, stats.getMean()));
        stats.setCoefficientOfVariation(calculateCoefficientOfVariation(stats.getMean(), stats.getStandardDeviation()));
        stats.setMedian(calculateMedian(validData));
        stats.setLowerQuartile(calculateQuartile(validData, 0.25));
        stats.setUpperQuartile(calculateQuartile(validData, 0.75));
        stats.setMeanMinus2Sigma(stats.getMean() - 2 * stats.getStandardDeviation());
        stats.setMeanPlus2Sigma(stats.getMean() + 2 * stats.getStandardDeviation());
        stats.setLogMean(calculateLogMean(validData));
        stats.setLogVariance(calculateLogVariance(validData, stats.getLogMean()));
        stats.setShapeParameter(calculateShapeParameter(validData));
        stats.setScaleParameter(calculateScaleParameter(validData));

        return stats;
    }

    private double calculateMean(List<Double> data) {
        double sum = 0;
        for (Double value : data) {
            sum += value;
        }
        return sum / data.size();
    }

    private double calculateStandardDeviation(List<Double> data, double mean) {
        double sumSquaredDiff = 0;
        for (Double value : data) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / data.size());
    }

    private double calculateCoefficientOfVariation(double mean, double standardDeviation) {
        if (mean == 0) {
            return 0;
        }
        return (standardDeviation / mean) * 100;
    }

    private double calculateMedian(List<Double> sortedData) {
        int size = sortedData.size();
        if (size % 2 == 0) {
            return (sortedData.get(size / 2 - 1) + sortedData.get(size / 2)) / 2.0;
        } else {
            return sortedData.get(size / 2);
        }
    }

    private double calculateQuartile(List<Double> sortedData, double percentile) {
        int size = sortedData.size();
        double position = percentile * (size - 1);
        int lower = (int) Math.floor(position);
        int upper = (int) Math.ceil(position);

        if (lower == upper) {
            return sortedData.get(lower);
        }

        double weight = position - lower;
        return sortedData.get(lower) * (1 - weight) + sortedData.get(upper) * weight;
    }

    private double calculateLogMean(List<Double> data) {
        double sumLog = 0;
        int count = 0;
        for (Double value : data) {
            if (value > 0) {
                sumLog += Math.log(value);
                count++;
            }
        }
        return count > 0 ? sumLog / count : 0;
    }

    private double calculateLogVariance(List<Double> data, double logMean) {
        double sumSquaredDiff = 0;
        int count = 0;
        for (Double value : data) {
            if (value > 0) {
                double logValue = Math.log(value);
                double diff = logValue - logMean;
                sumSquaredDiff += diff * diff;
                count++;
            }
        }
        return count > 0 ? sumSquaredDiff / count : 0;
    }

    private double calculateShapeParameter(List<Double> data) {
        double mean = calculateMean(data);
        double stdDev = calculateStandardDeviation(data, mean);

        if (stdDev == 0) {
            return 0;
        }

        double sum = 0;
        for (Double value : data) {
            double z = (value - mean) / stdDev;
            sum += z * z * z * z;
        }

        double skewness = sum / data.size();

        double k = Math.pow(2 / skewness, 0.3333);

        return k;
    }

    private double calculateScaleParameter(List<Double> data) {
        double mean = calculateMean(data);
        double stdDev = calculateStandardDeviation(data, mean);

        double sumAbs = 0;
        for (Double value : data) {
            sumAbs += Math.abs(value - mean);
        }

        double mad = sumAbs / data.size();

        return 0.6745 * mad;
    }
}