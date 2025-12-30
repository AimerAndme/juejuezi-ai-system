package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.LithologyLog;
import com.yupi.yuaiagent.mapper.LithologyLogMapper;
import com.yupi.yuaiagent.service.ILithologyLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class LithologyLogService implements ILithologyLogService {

    private final LithologyLogMapper mapper;

    public LithologyLogService(LithologyLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(LithologyLog entity) {
        return mapper.insert(entity);
    }

    @Override
    public LithologyLog getById(Long logId) {
        return mapper.selectById(logId);
    }

    @Override
    public List<LithologyLog> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<LithologyLog> getByHoleId(String holeId) {
        return mapper.selectByHoleId(holeId);
    }

    @Override
    public int update(LithologyLog entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(Long logId) {
        return mapper.deleteById(logId);
    }
}
