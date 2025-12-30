package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.MiningFace;
import java.util.List;

public interface IMiningFaceService {

    int add(MiningFace entity);

    MiningFace getById(String faceId);

    List<MiningFace> getAll();

    List<MiningFace> getByAreaId(String areaId);

    int update(MiningFace entity);

    int delete(String faceId);
}
