package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.RoadwayPoint;
import java.util.List;

public interface IRoadwayPointService {

    int add(RoadwayPoint entity);

    RoadwayPoint getById(String pointId);

    List<RoadwayPoint> getAll();

    List<RoadwayPoint> getByAreaId(String areaId);

    int update(RoadwayPoint entity);

    int delete(String pointId);
}
