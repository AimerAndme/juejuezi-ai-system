package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.CoalSeam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CoalSeamMapper {

    int insert(CoalSeam entity);

    CoalSeam selectById(@Param("seamId") String seamId);

    List<CoalSeam> selectAll();

    int update(CoalSeam entity);

    int deleteById(@Param("seamId") String seamId);
}
