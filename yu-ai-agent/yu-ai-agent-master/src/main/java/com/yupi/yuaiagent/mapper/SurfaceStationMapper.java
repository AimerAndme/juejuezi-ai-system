package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.SurfaceStation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SurfaceStationMapper {

    int insert(SurfaceStation entity);

    SurfaceStation selectById(@Param("stationId") String stationId);

    List<SurfaceStation> selectAll();

    List<SurfaceStation> selectByAreaId(@Param("areaId") String areaId);

    int update(SurfaceStation entity);

    int deleteById(@Param("stationId") String stationId);
}
