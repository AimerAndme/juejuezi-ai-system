package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.Borehole;
import com.yupi.yuaiagent.domin.vo.HistogramData;

import java.util.List;

public interface IBoreholeService {

    int add(Borehole entity);

    Borehole getById(String holeId);

    List<Borehole> getAll();

    List<Borehole> getAll(int offset, int limit);

    int getTotalCount();

    List<Borehole> getByAreaId(String areaId);

    List<Borehole> getByAreaId(String areaId, int offset, int limit);

    int getTotalCountByAreaId(String areaId);

    int update(Borehole entity);

    int delete(String holeId);

    HistogramData getHistogramVisualizationData(String areaId, int intervalCount, Double minValue, Double maxValue);
}
