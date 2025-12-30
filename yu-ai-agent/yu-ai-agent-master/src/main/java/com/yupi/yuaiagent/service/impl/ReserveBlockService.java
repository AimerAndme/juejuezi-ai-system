package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.ReserveBlock;
import com.yupi.yuaiagent.mapper.ReserveBlockMapper;
import com.yupi.yuaiagent.service.IReserveBlockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ReserveBlockService implements IReserveBlockService {

    private final ReserveBlockMapper mapper;

    public ReserveBlockService(ReserveBlockMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(ReserveBlock entity) {
        return mapper.insert(entity);
    }

    @Override
    public ReserveBlock getById(String blockId) {
        return mapper.selectById(blockId);
    }

    @Override
    public List<ReserveBlock> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<ReserveBlock> getByAreaId(String areaId) {
        return mapper.selectByAreaId(areaId);
    }

    @Override
    public int update(ReserveBlock entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(String blockId) {
        return mapper.deleteById(blockId);
    }
}
