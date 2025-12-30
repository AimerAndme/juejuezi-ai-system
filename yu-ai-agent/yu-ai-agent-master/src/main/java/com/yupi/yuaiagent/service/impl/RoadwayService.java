package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.Roadway;
import com.yupi.yuaiagent.mapper.RoadwayMapper;
import com.yupi.yuaiagent.service.IRoadwayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class RoadwayService implements IRoadwayService {

    private final RoadwayMapper mapper;

    public RoadwayService(RoadwayMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(Roadway entity) {
        return mapper.insert(entity);
    }

    @Override
    public Roadway getById(String roadwayId) {
        return mapper.selectById(roadwayId);
    }

    @Override
    public List<Roadway> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<Roadway> getByAreaId(String areaId) {
        return mapper.selectByAreaId(areaId);
    }

    @Override
    public int update(Roadway entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(String roadwayId) {
        return mapper.deleteById(roadwayId);
    }
}
