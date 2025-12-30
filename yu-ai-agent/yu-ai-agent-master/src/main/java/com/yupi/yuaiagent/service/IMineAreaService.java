package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.MineArea;
import java.util.List;

public interface IMineAreaService {

    int add(MineArea entity);

    MineArea getById(String areaId);

    List<MineArea> getAll();

    int update(MineArea entity);

    int delete(String areaId);
}
