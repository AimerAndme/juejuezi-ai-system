package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.HistogramInterval;
import com.yupi.yuaiagent.domin.entity.ScatterPoint;
import com.yupi.yuaiagent.domin.entity.ValueRange;
import com.yupi.yuaiagent.mapper.VisualizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataAggregationService {

    @Autowired
    private VisualizationMapper visualizationMapper;

    /**
     * 验证表名和列名的合法性
     */
    private void validateTableNameAndColumnName(String tableName, String columnName) {
        // 简单验证：只允许字母、数字和下划线
        if (!tableName.matches("^[a-zA-Z0-9_]+$") || !columnName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("表名或列名包含非法字符");
        }
        // 可以根据实际需求添加更多验证，例如检查表名是否存在于白名单中
    }

    /**
     * 计算直方图数据
     */
    public List<HistogramInterval> calculateHistogram(String areaId, String tableName, String columnName, int intervalCount, Double minValue, Double maxValue) {
        // 1. 验证参数合法性
        validateTableNameAndColumnName(tableName, columnName);

        // 2. 计算数据范围（如果未指定）
        if (minValue == null || maxValue == null) {
            ValueRange range = getValueRange(areaId, tableName, columnName);
            minValue = range.getMinValue();
            maxValue = range.getMaxValue();
        }

        // 3. 计算区间宽度
        double intervalWidth = (maxValue - minValue) / intervalCount;

        // 4. 执行查询
        List<Map<String, Object>> results = visualizationMapper.calculateHistogram(
                areaId, tableName, columnName, intervalCount, minValue, maxValue, intervalWidth
        );

        // 5. 转换结果
        List<HistogramInterval> intervals = new ArrayList<>();
        for (Map<String, Object> result : results) {
            HistogramInterval interval = new HistogramInterval();
            interval.setIndex(((Number) result.get("interval_index")).intValue());
            interval.setCount(((Number) result.get("count")).longValue());
            interval.setMinValue(((Number) result.get("min_value")).doubleValue());
            interval.setMaxValue(((Number) result.get("max_value")).doubleValue());
            intervals.add(interval);
        }

        // 6. 填充缺失区间
        fillMissingIntervals(intervals, intervalCount, minValue, intervalWidth);

        return intervals;
    }

    /**
     * 获取散点图数据（带采样）
     */
    public List<ScatterPoint> getScatterData(String areaId, String tableName, String xColumnName, String yColumnName, Double sampleRate) {
        // 1. 验证参数合法性
        validateTableNameAndColumnName(tableName, xColumnName);
        validateTableNameAndColumnName(tableName, yColumnName);

        // 2. 计算采样率
        double rate = sampleRate != null ? sampleRate : calculateOptimalSampleRate(tableName);

        // 3. 执行查询
        List<Map<String, Object>> results = visualizationMapper.getScatterData(
                areaId, tableName, xColumnName, yColumnName, rate
        );

        // 4. 转换结果
        List<ScatterPoint> points = new ArrayList<>();
        for (Map<String, Object> result : results) {
            ScatterPoint point = new ScatterPoint();
            point.setX(((Number) result.get("x")).doubleValue());
            point.setY(((Number) result.get("y")).doubleValue());
            points.add(point);
        }

        return points;
    }

    /**
     * 获取值范围
     */
    public ValueRange getValueRange(String areaId, String tableName, String columnName) {
        // 验证参数合法性
        validateTableNameAndColumnName(tableName, columnName);
        return visualizationMapper.getValueRange(areaId, tableName, columnName);
    }

    /**
     * 计算最佳采样率
     */
    private double calculateOptimalSampleRate(String tableName) {
        // 这里可以根据表的实际大小动态计算采样率
        // 简单实现：默认采样10%
        return 0.1;
    }

    /**
     * 填充缺失区间
     */
    private void fillMissingIntervals(List<HistogramInterval> intervals, int intervalCount, double minValue, double intervalWidth) {
        // 构建索引映射
        Map<Integer, HistogramInterval> intervalMap = new HashMap<>();
        for (HistogramInterval interval : intervals) {
            intervalMap.put(interval.getIndex(), interval);
        }

        // 填充缺失区间
        List<HistogramInterval> filledIntervals = new ArrayList<>();
        for (int i = 0; i < intervalCount; i++) {
            if (intervalMap.containsKey(i)) {
                filledIntervals.add(intervalMap.get(i));
            } else {
                HistogramInterval interval = new HistogramInterval();
                interval.setIndex(i);
                interval.setCount(0);
                interval.setMinValue(minValue + i * intervalWidth);
                interval.setMaxValue(minValue + (i + 1) * intervalWidth);
                filledIntervals.add(interval);
            }
        }

        intervals.clear();
        intervals.addAll(filledIntervals);
    }
}
