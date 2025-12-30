package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.Borehole;
import com.yupi.yuaiagent.mapper.BoreholeMapper;
import com.yupi.yuaiagent.service.IBoreholeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class BoreholeService implements IBoreholeService {

    private final BoreholeMapper mapper;

    public BoreholeService(BoreholeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(Borehole entity) {
        return mapper.insert(entity);
    }

    @Override
    public Borehole getById(String holeId) {
        return mapper.selectById(holeId);
    }

    @Override
    public List<Borehole> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<Borehole> getByAreaId(String areaId) {
        return mapper.selectByAreaId(areaId);
    }

    @Override
    public int update(Borehole entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(String holeId) {
        return mapper.deleteById(holeId);
    }
}
