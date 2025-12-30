package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.SubsidenceObservation;
import com.yupi.yuaiagent.mapper.SubsidenceObservationMapper;
import com.yupi.yuaiagent.service.ISubsidenceObservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class SubsidenceObservationService implements ISubsidenceObservationService {

    private final SubsidenceObservationMapper mapper;

    public SubsidenceObservationService(SubsidenceObservationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(SubsidenceObservation entity) {
        return mapper.insert(entity);
    }

    @Override
    public SubsidenceObservation getById(Long obsId) {
        return mapper.selectById(obsId);
    }

    @Override
    public List<SubsidenceObservation> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<SubsidenceObservation> getByStationId(String stationId) {
        return mapper.selectByStationId(stationId);
    }

    @Override
    public int update(SubsidenceObservation entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(Long obsId) {
        return mapper.deleteById(obsId);
    }
}
