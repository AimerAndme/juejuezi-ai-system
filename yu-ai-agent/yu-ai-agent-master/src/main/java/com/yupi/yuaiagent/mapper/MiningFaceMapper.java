package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.MiningFace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MiningFaceMapper {

    int insert(MiningFace entity);

    MiningFace selectById(@Param("faceId") String faceId);

    List<MiningFace> selectAll();

    List<MiningFace> selectByAreaId(@Param("areaId") String areaId);

    int update(MiningFace entity);

    int deleteById(@Param("faceId") String faceId);
}
