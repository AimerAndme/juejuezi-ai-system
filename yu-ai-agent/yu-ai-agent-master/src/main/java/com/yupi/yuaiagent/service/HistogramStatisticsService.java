package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.HistogramRange;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class HistogramStatisticsService {

    /**
     * 计算直方图数据
     *
     * @param dataList      数据列表
     * @param intervalCount 区间数量
     * @return 直方图区间列表
     */
    public List<HistogramRange> calculateHistogram(List<Double> dataList, int intervalCount) {
        if (dataList == null || dataList.isEmpty()) {
            return new ArrayList<>();
        }

        if (intervalCount <= 0) {
            throw new IllegalArgumentException("区间数量必须大于0");
        }

        double minValue = Collections.min(dataList);
        double maxValue = Collections.max(dataList);

        return calculateHistogram(dataList, intervalCount, minValue, maxValue);
    }

    /**
     * 计算直方图数据（指定范围）
     *
     * @param dataList      数据列表
     * @param intervalCount 区间数量
     * @param minValue      最小值
     * @param maxValue      最大值
     * @return 直方图区间列表
     */
    public List<HistogramRange> calculateHistogram(List<Double> dataList, int intervalCount,
                                                   Double maxValue, Double minValue) {
        if (dataList == null || dataList.isEmpty()) {
            return new ArrayList<>();
        }

        if (intervalCount <= 0) {
            throw new IllegalArgumentException("区间数量必须大于0");
        }
        if (minValue == null || maxValue == null) {
            throw new IllegalArgumentException("最小值和最大值不能为空");
        }
        if (minValue >= maxValue) {
            throw new IllegalArgumentException("最小值必须小于最大值");
        }

        double intervalWidth = (maxValue - minValue) / intervalCount;

        long[] intervalCounts = new long[intervalCount];

        for (Double value : dataList) {
            if (value == null) {
                continue;
            }

            int intervalIndex = calculateIntervalIndex(value, minValue, intervalWidth, intervalCount);
            if (intervalIndex >= 0 && intervalIndex < intervalCount) {
                intervalCounts[intervalIndex]++;
            }
        }

        List<HistogramRange> intervals = new ArrayList<>();
        for (int i = 0; i < intervalCount; i++) {
            HistogramRange range = new HistogramRange();
            range.setIndex(i);
            range.setLeftEndpoint(minValue + i * intervalWidth);
            range.setRightEndpoint(minValue + (i + 1) * intervalWidth);
            range.setCount(intervalCounts[i]);
            intervals.add(range);
        }

        return intervals;
    }

    /**
     * 计算区间索引
     *
     * @param value         数据值
     * @param minValue      最小值
     * @param intervalWidth 区间宽度
     * @param intervalCount 区间数量
     * @return 区间索引
     */
    private int calculateIntervalIndex(double value, double minValue, double intervalWidth, int intervalCount) {
        if (value < minValue) {
            return 0;
        }
        if (value >= minValue + intervalCount * intervalWidth) {
            return intervalCount - 1;
        }
        int index = (int) Math.floor((value - minValue) / intervalWidth);
        return Math.min(index, intervalCount - 1);
    }

    /**
     * 计算直方图数据（自动确定区间数量）
     *
     * @param dataList 数据列表
     * @return 直方图区间列表
     */
    public List<HistogramRange> calculateHistogramAutoInterval(List<Double> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return new ArrayList<>();
        }

        int intervalCount = calculateOptimalIntervalCount(dataList.size());
        return calculateHistogram(dataList, intervalCount);
    }

    /**
     * 计算最优区间数量（基于Sturges公式）
     *
     * @param dataSize 数据量
     * @return 最优区间数量
     */
    private int calculateOptimalIntervalCount(int dataSize) {
        if (dataSize <= 0) {
            return 10;
        }

        int sturgesInterval = (int) Math.ceil(Math.log(dataSize) / Math.log(2)) + 1;

        return Math.max(5, Math.min(50, sturgesInterval));
    }

    /**
     * 计算直方图数据（指定区间宽度）
     *
     * @param dataList      数据列表
     * @param intervalWidth 区间宽度
     * @return 直方图区间列表
     */
    public List<HistogramRange> calculateHistogramByWidth(List<Double> dataList, double intervalWidth) {
        if (dataList == null || dataList.isEmpty()) {
            return new ArrayList<>();
        }

        if (intervalWidth <= 0) {
            throw new IllegalArgumentException("区间宽度必须大于0");
        }

        double minValue = Collections.min(dataList);
        double maxValue = Collections.max(dataList);

        int intervalCount = (int) Math.ceil((maxValue - minValue) / intervalWidth);

        return calculateHistogram(dataList, intervalCount, minValue, maxValue);
    }

    /**
     * 计算直方图数据（指定区间边界）
     *
     * @param dataList   数据列表
     * @param boundaries 区间边界列表（升序排列）
     * @return 直方图区间列表
     */
    public List<HistogramRange> calculateHistogramByBoundaries(List<Double> dataList, List<Double> boundaries) {
        if (dataList == null || dataList.isEmpty()) {
            return new ArrayList<>();
        }

        if (boundaries == null || boundaries.size() < 2) {
            throw new IllegalArgumentException("区间边界至少需要2个值");
        }

        int intervalCount = boundaries.size() - 1;
        long[] intervalCounts = new long[intervalCount];

        for (Double value : dataList) {
            if (value == null) {
                continue;
            }

            int intervalIndex = findIntervalIndex(value, boundaries);
            if (intervalIndex >= 0 && intervalIndex < intervalCount) {
                intervalCounts[intervalIndex]++;
            }
        }

        List<HistogramRange> intervals = new ArrayList<>();
        for (int i = 0; i < intervalCount; i++) {
            HistogramRange range = new HistogramRange();
            range.setIndex(i);
            range.setLeftEndpoint(boundaries.get(i));
            range.setRightEndpoint(boundaries.get(i + 1));
            range.setCount(intervalCounts[i]);
            intervals.add(range);
        }

        return intervals;
    }

    /**
     * 查找值所在的区间索引
     *
     * @param value      数据值
     * @param boundaries 区间边界列表
     * @return 区间索引
     */
    private int findIntervalIndex(double value, List<Double> boundaries) {
        for (int i = 0; i < boundaries.size() - 1; i++) {
            if (value >= boundaries.get(i) && value < boundaries.get(i + 1)) {
                return i;
            }
        }

        if (value >= boundaries.get(boundaries.size() - 1)) {
            return boundaries.size() - 2;
        }

        return 0;
    }
}