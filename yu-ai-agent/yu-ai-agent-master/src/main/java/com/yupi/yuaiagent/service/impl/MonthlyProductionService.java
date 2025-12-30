package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.MonthlyProduction;
import com.yupi.yuaiagent.mapper.MonthlyProductionMapper;
import com.yupi.yuaiagent.service.IMonthlyProductionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class MonthlyProductionService implements IMonthlyProductionService {

    private final MonthlyProductionMapper mapper;

    public MonthlyProductionService(MonthlyProductionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(MonthlyProduction entity) {
        return mapper.insert(entity);
    }

    @Override
    public MonthlyProduction getById(Long recordId) {
        return mapper.selectById(recordId);
    }

    @Override
    public List<MonthlyProduction> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<MonthlyProduction> getByBlockId(String blockId) {
        return mapper.selectByBlockId(blockId);
    }

    @Override
    public int update(MonthlyProduction entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(Long recordId) {
        return mapper.deleteById(recordId);
    }
}
