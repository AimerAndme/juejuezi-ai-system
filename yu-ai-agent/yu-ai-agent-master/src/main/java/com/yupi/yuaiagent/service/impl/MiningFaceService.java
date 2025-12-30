package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.MiningFace;
import com.yupi.yuaiagent.mapper.MiningFaceMapper;
import com.yupi.yuaiagent.service.IMiningFaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class MiningFaceService implements IMiningFaceService {

    private final MiningFaceMapper mapper;

    public MiningFaceService(MiningFaceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int add(MiningFace entity) {
        return mapper.insert(entity);
    }

    @Override
    public MiningFace getById(String faceId) {
        return mapper.selectById(faceId);
    }

    @Override
    public List<MiningFace> getAll() {
        return mapper.selectAll();
    }

    @Override
    public List<MiningFace> getByAreaId(String areaId) {
        return mapper.selectByAreaId(areaId);
    }

    @Override
    public int update(MiningFace entity) {
        return mapper.update(entity);
    }

    @Override
    public int delete(String faceId) {
        return mapper.deleteById(faceId);
    }
}
