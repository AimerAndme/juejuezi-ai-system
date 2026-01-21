//package com.yupi.yuaiagent.service.impl;
//
//import com.yupi.yuaiagent.domin.entity.HistogramInterval;
//import com.yupi.yuaiagent.domin.entity.ValueRange;
//import com.yupi.yuaiagent.domin.vo.HistogramData;
//import com.yupi.yuaiagent.service.DataAggregationService;
//import com.yupi.yuaiagent.service.IVisualizationService;
//import com.yupi.yuaiagent.service.cache.CacheService;
//import com.yupi.yuaiagent.util.CacheKeyGenerator;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@AllArgsConstructor
//@Slf4j
//public class VisualizationService implements IVisualizationService {
//
//    private final DataAggregationService dataAggregationService;
//    private final CacheService cacheService;
//    @Override
//    public HistogramData getHistogramVisualizationData(String areaId, String tableName, String columnName, int intervalCount, Double minValue, Double maxValue) {
//        if (minValue == null || maxValue == null) {
//            ValueRange  range = dataAggregationService.getValueRange(areaId,tableName, columnName);
//            minValue = range.getMinValue();
//            maxValue = range.getMaxValue();
//        }
//        String cacheKey = CacheKeyGenerator.generateHistogramCacheKey(tableName, minValue, maxValue);
//        try {
//            //缓存命中直接返回结果
//            Object cache = cacheService.getCache(cacheKey);
//            if (cache != null) {
//                log.info("缓存命中：{}", cacheKey);
//                return (HistogramData) cache;
//            }
//        } catch (Exception e) {
//            log.error("缓存获取异常：{}", cacheKey, e);
//        }
//        log.info("缓存未命中，执行数据查询：{}", cacheKey);
//        List<HistogramInterval> histogramIntervals = dataAggregationService.calculateHistogram(areaId, tableName, columnName, intervalCount, minValue, maxValue);
//        HistogramData histogramData = new HistogramData();
//        histogramData.setInterval(histogramIntervals);
//        histogramData.setTableName(tableName);
//        histogramData.setColumnName(columnName);
//        cacheService.setCache(cacheKey, histogramData, 3600L);
//        return histogramData;
//    }
//}
