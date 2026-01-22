package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import com.yupi.yuaiagent.domin.entity.Borehole;
import com.yupi.yuaiagent.domin.entity.ValueRange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoreholeMapper {

    /**
     * 插入钻孔信息
     *
     * @param entity 钻孔信息
     * @return 影响行数
     */
    int insert(Borehole entity);

    /**
     * 根据ID查询钻孔信息
     *
     * @param holeId 钻孔编号
     * @return 钻孔信息
     */
    Borehole selectById(@Param("holeId") String holeId);

    /**
     * 查询所有钻孔信息
     *
     * @return 钻孔信息列表
     */
    List<Borehole> selectAll();

    /**
     * 分页查询所有钻孔信息
     *
     * @param offset 偏移量
     * @param limit  限制条数
     * @return 钻孔信息列表
     */
    List<Borehole> selectAllWithPagination(int offset, int limit);

    /**
     * 查询钻孔总数
     *
     * @return 总数
     */
    int selectTotalCount();

    /**
     * 根据矿区编码查询钻孔信息
     *
     * @param areaId 矿区编码
     * @return 钻孔信息列表
     */
    @ExecutionTimeMonitor
    List<Borehole> selectByAreaId(@Param("areaId") String areaId);

    /**
     * 根据矿区编码分页查询钻孔信息
     *
     * @param areaId 矿区编码
     * @param offset 偏移量
     * @param limit  限制条数
     * @return 钻孔信息列表
     */
    List<Borehole> selectByAreaIdWithPagination(@Param("areaId") String areaId, int offset, int limit);

    /**
     * 根据矿区编码查询钻孔总数
     *
     * @param areaId 矿区编码
     * @return 总数
     */
    int selectTotalCountByAreaId(@Param("areaId") String areaId);

    /**
     * 更新钻孔信息
     *
     * @param entity 钻孔信息
     * @return 影响行数
     */
    int update(Borehole entity);

    /**
     * 根据ID删除钻孔信息
     *
     * @param holeId 钻孔编号
     * @return 影响行数
     */
    int deleteById(@Param("holeId") String holeId);

    ValueRange getValueRange(@Param("areaId") String areaId);

    List<Double> selectTotalDepthByAreaId(@Param("areaId") String areaId);
}
