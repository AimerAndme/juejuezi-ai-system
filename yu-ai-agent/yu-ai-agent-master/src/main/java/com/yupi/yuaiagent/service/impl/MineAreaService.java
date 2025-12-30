package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.MineArea;
import com.yupi.yuaiagent.mapper.MineAreaMapper;
import com.yupi.yuaiagent.service.IMineAreaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class MineAreaService implements IMineAreaService {

    private final MineAreaMapper mapper;

    public MineAreaService(MineAreaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(MineArea entity) {
        return mapper.insert(entity);
    }

    @Override
    public MineArea getById(String areaId) {
        return mapper.selectById(areaId);
    }

    @Override
    public List<MineArea> getAll() {
        return mapper.selectAll();
    }

    @Override
    public int update(MineArea entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(String areaId) {
        return mapper.deleteById(areaId);
    }
}
