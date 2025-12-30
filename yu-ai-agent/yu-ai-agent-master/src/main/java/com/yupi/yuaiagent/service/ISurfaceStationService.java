package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.SurfaceStation;
import java.util.List;

public interface ISurfaceStationService {

    int add(SurfaceStation entity);

    SurfaceStation getById(String stationId);

    List<SurfaceStation> getAll();

    List<SurfaceStation> getByAreaId(String areaId);

    int update(SurfaceStation entity);

    int delete(String stationId);
}
