package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.RoadwayPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoadwayPointMapper {

    int insert(RoadwayPoint entity);

    RoadwayPoint selectById(@Param("pointId") String pointId);

    List<RoadwayPoint> selectAll();

    List<RoadwayPoint> selectByAreaId(@Param("areaId") String areaId);

    int update(RoadwayPoint entity);

    int deleteById(@Param("pointId") String pointId);
}
