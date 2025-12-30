package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.SeamIntercept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SeamInterceptMapper {

    int insert(SeamIntercept entity);

    SeamIntercept selectById(@Param("interceptId") Long interceptId);

    List<SeamIntercept> selectAll();

    List<SeamIntercept> selectByHoleId(@Param("holeId") String holeId);

    int update(SeamIntercept entity);

    int deleteById(@Param("interceptId") Long interceptId);
}
