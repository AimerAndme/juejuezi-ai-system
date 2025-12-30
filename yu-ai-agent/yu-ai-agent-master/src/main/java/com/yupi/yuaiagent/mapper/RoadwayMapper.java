package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.Roadway;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoadwayMapper {

    int insert(Roadway entity);

    Roadway selectById(@Param("roadwayId") String roadwayId);

    List<Roadway> selectAll();

    List<Roadway> selectByAreaId(@Param("areaId") String areaId);

    int update(Roadway entity);

    int deleteById(@Param("roadwayId") String roadwayId);
}
