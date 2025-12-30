package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.LithologyLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LithologyLogMapper {

    int insert(LithologyLog entity);

    LithologyLog selectById(@Param("logId") Long logId);

    List<LithologyLog> selectAll();

    List<LithologyLog> selectByHoleId(@Param("holeId") String holeId);

    int update(LithologyLog entity);

    int deleteById(@Param("logId") Long logId);
}
