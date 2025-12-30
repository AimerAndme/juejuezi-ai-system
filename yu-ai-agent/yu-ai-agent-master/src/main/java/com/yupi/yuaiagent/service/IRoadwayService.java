package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.Roadway;
import java.util.List;

public interface IRoadwayService {

    int add(Roadway entity);

    Roadway getById(String roadwayId);

    List<Roadway> getAll();

    List<Roadway> getByAreaId(String areaId);

    int update(Roadway entity);

    int delete(String roadwayId);
}
