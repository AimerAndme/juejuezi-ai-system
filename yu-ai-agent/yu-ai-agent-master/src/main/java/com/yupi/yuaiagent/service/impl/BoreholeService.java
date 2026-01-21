package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.Borehole;
import com.yupi.yuaiagent.domin.entity.HistogramRange;
import com.yupi.yuaiagent.domin.entity.ValueRange;
import com.yupi.yuaiagent.domin.vo.HistogramData;
import com.yupi.yuaiagent.mapper.BoreholeMapper;
import com.yupi.yuaiagent.service.IBoreholeService;
import com.yupi.yuaiagent.service.cache.CacheService;
import com.yupi.yuaiagent.service.visualization.HistogramCalculation;
import com.yupi.yuaiagent.util.CacheKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class BoreholeService implements IBoreholeService {

    private final BoreholeMapper mapper;
    private final CacheService cacheService;
    private final HistogramCalculation histogramCalculation;

    public BoreholeService(BoreholeMapper mapper, CacheService cacheService, HistogramCalculation histogramCalculation) {
        this.mapper = mapper;
        this.cacheService = cacheService;
        this.histogramCalculation = histogramCalculation;
    }

    @Override
    public int add(Borehole entity) {
        return mapper.insert(entity);
    }

    @Override
    public Borehole getById(String holeId) {
        return mapper.selectById(holeId);
    }

    @Override
    public List<Borehole> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<Borehole> getAll(int offset, int limit) {
        return mapper.selectAllWithPagination(offset, limit);
    }

    @Override
    public int getTotalCount() {
        return mapper.selectTotalCount();
    }

    @Override
    public List<Borehole> getByAreaId(String areaId) {
        return mapper.selectByAreaId(areaId);
    }

    @Override
    public List<Borehole> getByAreaId(String areaId, int offset, int limit) {
        return mapper.selectByAreaIdWithPagination(areaId, offset, limit);
    }

    @Override
    public int getTotalCountByAreaId(String areaId) {
        return mapper.selectTotalCountByAreaId(areaId);
    }

    @Override
    public int update(Borehole entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(String holeId) {
        return mapper.deleteById(holeId);
    }

    @Override
    public HistogramData getHistogramVisualizationData(String areaId, int intervalCount, Double minValue, Double maxValue) {
        ValueRange range = mapper.getValueRange(areaId);
        int count = range.getCount();
        if (minValue == null || maxValue == null) {
            minValue = range.getMinValue();
            maxValue = range.getMaxValue();
        }
        String cacheKey = CacheKeyGenerator.generateHistogramCacheKey(areaId, intervalCount, minValue, maxValue, count);
        try {
            Object cache = cacheService.getCache(cacheKey);
            if (cache != null) {
                log.info("缓存命中：{}", cacheKey);
                return (HistogramData) cache;
            }
        } catch (Exception e) {
            log.error("缓存获取异常：{}", cacheKey, e);
        }
        log.info("缓存未命中，执行数据查询：{}", cacheKey);
        List<Borehole> boreholes = mapper.selectByAreaId(areaId);
        List<Double> doubleList = boreholes.stream().map(Borehole::getTotalDepth).toList();
        List<HistogramRange> histogramRanges = histogramCalculation.handleCalculation(doubleList, count, intervalCount);
        HistogramData histogramData = new HistogramData();
        histogramData.setInterval(histogramRanges);
        cacheService.setCache(cacheKey, histogramData, 60 * 60 * 24);
        return histogramData;
    }
}
