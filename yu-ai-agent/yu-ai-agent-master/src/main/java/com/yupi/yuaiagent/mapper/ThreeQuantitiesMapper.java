package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.ThreeQuantities;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ThreeQuantitiesMapper {

    int insert(ThreeQuantities entity);

    ThreeQuantities selectById(@Param("recordId") Long recordId);

    List<ThreeQuantities> selectAll();

    List<ThreeQuantities> selectByAreaId(@Param("areaId") String areaId);

    int update(ThreeQuantities entity);

    int deleteById(@Param("recordId") Long recordId);
}
