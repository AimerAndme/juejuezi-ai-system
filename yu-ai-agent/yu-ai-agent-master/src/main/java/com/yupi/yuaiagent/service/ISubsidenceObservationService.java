package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.SubsidenceObservation;
import java.util.List;

public interface ISubsidenceObservationService {

    int add(SubsidenceObservation entity);

    SubsidenceObservation getById(Long obsId);

    List<SubsidenceObservation> getAll();

    List<SubsidenceObservation> getByStationId(String stationId);

    int update(SubsidenceObservation entity);

    int delete(Long obsId);
}
