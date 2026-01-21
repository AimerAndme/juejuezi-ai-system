package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.vo.HistogramData;


public interface IVisualizationService {
    HistogramData getHistogramVisualizationData(String areaId, String tablename, String columnName, int intervalCount, Double minValue, Double maxValue);
}
