package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.SeamIntercept;
import com.yupi.yuaiagent.mapper.SeamInterceptMapper;
import com.yupi.yuaiagent.service.ISeamInterceptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class SeamInterceptService implements ISeamInterceptService {

    private final SeamInterceptMapper mapper;

    public SeamInterceptService(SeamInterceptMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(SeamIntercept entity) {
        return mapper.insert(entity);
    }

    @Override
    public SeamIntercept getById(Long interceptId) {
        return mapper.selectById(interceptId);
    }

    @Override
    public List<SeamIntercept> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<SeamIntercept> getByHoleId(String holeId) {
        return mapper.selectByHoleId(holeId);
    }

    @Override
    public int update(SeamIntercept entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(Long interceptId) {
        return mapper.deleteById(interceptId);
    }
}
