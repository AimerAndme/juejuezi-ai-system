package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.MineArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MineAreaMapper {

    int insert(MineArea entity);

    MineArea selectById(@Param("areaId") String areaId);

    List<MineArea> selectAll();

    int update(MineArea entity);

    int deleteById(@Param("areaId") String areaId);
}
