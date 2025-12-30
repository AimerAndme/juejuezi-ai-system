package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.ThreeQuantities;
import com.yupi.yuaiagent.mapper.ThreeQuantitiesMapper;
import com.yupi.yuaiagent.service.IThreeQuantitiesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ThreeQuantitiesService implements IThreeQuantitiesService {

    private final ThreeQuantitiesMapper mapper;

    public ThreeQuantitiesService(ThreeQuantitiesMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(ThreeQuantities entity) {
        return mapper.insert(entity);
    }

    @Override
    public ThreeQuantities getById(Long recordId) {
        return mapper.selectById(recordId);
    }

    @Override
    public List<ThreeQuantities> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<ThreeQuantities> getByAreaId(String areaId) {
        return mapper.selectByAreaId(areaId);
    }

    @Override
    public int update(ThreeQuantities entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(Long recordId) {
        return mapper.deleteById(recordId);
    }
}
