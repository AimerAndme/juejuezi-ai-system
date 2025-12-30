package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.DocumentVector;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档向量Mapper
 */
@Mapper
public interface DocumentVectorMapper {

    /**
     * 插入向量记录
     */
    int insert(DocumentVector documentVector);

    /**
     * 批量插入向量记录
     */
    int batchInsert(@Param("list") List<DocumentVector> list);

    /**
     * 根据向量ID查询
     */
    DocumentVector selectByVectorId(@Param("vectorId") Long vectorId);

    /**
     * 根据文件MD5查询所有向量
     */
    List<DocumentVector> selectByFileMd5(@Param("fileMd5") String fileMd5);

    /**
     * 根据文件MD5和用户ID查询向量
     */
    List<DocumentVector> selectByFileMd5AndUserId(@Param("fileMd5") String fileMd5, @Param("userId") String userId);

    /**
     * 根据用户ID查询所有向量
     */
    List<DocumentVector> selectByUserId(@Param("userId") String userId);

    /**
     * 查询公开的向量记录
     */
    List<DocumentVector> selectPublicVectors();

    /**
     * 根据组织标签查询向量
     */
    List<DocumentVector> selectByOrgTag(@Param("orgTag") String orgTag);

    /**
     * 根据文件MD5删除向量记录
     */
    int deleteByFileMd5(@Param("fileMd5") String fileMd5);

    /**
     * 根据向量ID删除
     */
    int deleteByVectorId(@Param("vectorId") Long vectorId);

    /**
     * 更新模型版本
     */
    int updateModelVersion(@Param("vectorId") Long vectorId, @Param("modelVersion") String modelVersion);

    /**
     * 统计文件的向量数量
     */
    int countByFileMd5(@Param("fileMd5") String fileMd5);
}
