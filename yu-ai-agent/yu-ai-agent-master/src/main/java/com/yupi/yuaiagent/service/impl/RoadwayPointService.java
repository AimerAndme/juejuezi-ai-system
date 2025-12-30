package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.RoadwayPoint;
import com.yupi.yuaiagent.mapper.RoadwayPointMapper;
import com.yupi.yuaiagent.service.IRoadwayPointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class RoadwayPointService implements IRoadwayPointService {

    private final RoadwayPointMapper mapper;

    public RoadwayPointService(RoadwayPointMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(RoadwayPoint entity) {
        return mapper.insert(entity);
    }

    @Override
    public RoadwayPoint getById(String pointId) {
        return mapper.selectById(pointId);
    }

    @Override
    public List<RoadwayPoint> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<RoadwayPoint> getByAreaId(String areaId) {
        return mapper.selectByAreaId(areaId);
    }

    @Override
    public int update(RoadwayPoint entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(String pointId) {
        return mapper.deleteById(pointId);
    }
}
