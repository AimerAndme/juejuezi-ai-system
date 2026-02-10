package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.ValueRange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VisualizationMapper {

    /**
     * 获取值范围
     */
    @Select({"SELECT MIN(CAST(${columnName} AS DOUBLE PRECISION)) as minValue, MAX(CAST(${columnName} AS DOUBLE PRECISION)) as maxValue FROM ${tableName} WHERE area_id=#{areaId}"})
    ValueRange getValueRange(
            @Param("areaId") String areaId,
            @Param("tableName") String tableName,
            @Param("columnName") String columnName
    );

    /**
     * 计算直方图数据
     */
    List<Map<String, Object>> calculateHistogram(
            @Param("areaId") String areaId,
            @Param("tableName") String tableName,
            @Param("columnName") String columnName,
            @Param("intervalCount") int intervalCount,
            @Param("minValue") double minValue,
            @Param("maxValue") double maxValue,
            @Param("intervalWidth") double intervalWidth
    );

    /**
     * 获取散点图数据
     */
    List<Map<String, Object>> getScatterData(
            @Param("areaId") String areaId,
            @Param("tableName") String tableName,
            @Param("xColumnName") String xColumnName,
            @Param("yColumnName") String yColumnName,
            @Param("sampleRate") double sampleRate
    );
}
