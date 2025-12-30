package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.SurfaceStation;
import com.yupi.yuaiagent.mapper.SurfaceStationMapper;
import com.yupi.yuaiagent.service.ISurfaceStationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class SurfaceStationService implements ISurfaceStationService {

    private final SurfaceStationMapper mapper;

    public SurfaceStationService(SurfaceStationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(SurfaceStation entity) {
        return mapper.insert(entity);
    }

    @Override
    public SurfaceStation getById(String stationId) {
        return mapper.selectById(stationId);
    }

    @Override
    public List<SurfaceStation> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<SurfaceStation> getByAreaId(String areaId) {
        return mapper.selectByAreaId(areaId);
    }

    @Override
    public int update(SurfaceStation entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(String stationId) {
        return mapper.deleteById(stationId);
    }
}
