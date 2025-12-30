package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.Borehole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BoreholeMapper {

    int insert(Borehole entity);

    Borehole selectById(@Param("holeId") String holeId);

    List<Borehole> selectAll();

    List<Borehole> selectByAreaId(@Param("areaId") String areaId);

    int update(Borehole entity);

    int deleteById(@Param("holeId") String holeId);
}
