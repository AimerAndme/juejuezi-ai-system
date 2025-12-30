package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.ThreeQuantities;
import java.util.List;

public interface IThreeQuantitiesService {

    int add(ThreeQuantities entity);

    ThreeQuantities getById(Long recordId);

    List<ThreeQuantities> getAll();

    List<ThreeQuantities> getByAreaId(String areaId);

    int update(ThreeQuantities entity);

    int delete(Long recordId);
}
