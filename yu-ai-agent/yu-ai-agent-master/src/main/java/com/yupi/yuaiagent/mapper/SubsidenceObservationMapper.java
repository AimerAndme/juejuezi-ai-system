package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.SubsidenceObservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SubsidenceObservationMapper {

    int insert(SubsidenceObservation entity);

    SubsidenceObservation selectById(@Param("obsId") Long obsId);

    List<SubsidenceObservation> selectAll();

    List<SubsidenceObservation> selectByStationId(@Param("stationId") String stationId);

    int update(SubsidenceObservation entity);

    int deleteById(@Param("obsId") Long obsId);
}
