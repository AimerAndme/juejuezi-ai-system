package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.ReserveBlock;
import java.util.List;

public interface IReserveBlockService {

    int add(ReserveBlock entity);

    ReserveBlock getById(String blockId);

    List<ReserveBlock> getAll();

    List<ReserveBlock> getByAreaId(String areaId);

    int update(ReserveBlock entity);

    int delete(String blockId);
}


    
    
    
    
    
    