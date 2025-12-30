package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.MonthlyProduction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MonthlyProductionMapper {

    int insert(MonthlyProduction entity);

    MonthlyProduction selectById(@Param("recordId") Long recordId);

    List<MonthlyProduction> selectAll();

    List<MonthlyProduction> selectByBlockId(@Param("blockId") String blockId);

    int update(MonthlyProduction entity);

    int deleteById(@Param("recordId") Long recordId);
}
