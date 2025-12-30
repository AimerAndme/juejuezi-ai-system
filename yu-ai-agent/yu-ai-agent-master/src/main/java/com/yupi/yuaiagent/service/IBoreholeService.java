package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.Borehole;
import java.util.List;

public interface IBoreholeService {

    int add(Borehole entity);

    Borehole getById(String holeId);

    List<Borehole> getAll();

    List<Borehole> getByAreaId(String areaId);

    int update(Borehole entity);

    int delete(String holeId);
}
