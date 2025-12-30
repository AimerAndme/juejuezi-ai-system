package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.CoalSeam;
import com.yupi.yuaiagent.mapper.CoalSeamMapper;
import com.yupi.yuaiagent.service.ICoalSeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CoalSeamService implements ICoalSeamService {

    private final CoalSeamMapper mapper;

    public CoalSeamService(CoalSeamMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(CoalSeam entity) {
        return mapper.insert(entity);
    }

    @Override
    public CoalSeam getById(String seamId) {
        return mapper.selectById(seamId);
    }

    @Override
    public List<CoalSeam> getAll() {
        return mapper.selectAll();
    }

    @Override
    public int update(CoalSeam entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(String seamId) {
        return mapper.deleteById(seamId);
    }
}
