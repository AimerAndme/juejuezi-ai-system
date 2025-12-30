package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.ReserveBlock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReserveBlockMapper {

    int insert(ReserveBlock entity);

    ReserveBlock selectById(@Param("blockId") String blockId);

    List<ReserveBlock> selectAll();

    List<ReserveBlock> selectByAreaId(@Param("areaId") String areaId);

    int update(ReserveBlock entity);

    int deleteById(@Param("blockId") String blockId);
}
