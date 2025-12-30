package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.DocumentVector;

import java.util.List;

/**
 * 文档向量服务接口
 */
public interface IDocumentVectorService {

    /**
     * 保存单个向量记录
     */
    int saveVector(DocumentVector documentVector);

    /**
     * 批量保存向量记录
     */
    int batchSaveVectors(List<DocumentVector> vectors);

    /**
     * 根据向量ID查询
     */
    DocumentVector getVectorById(Long vectorId);

    /**
     * 根据文件MD5查询所有向量
     */
    List<DocumentVector> getVectorsByFileMd5(String fileMd5);

    /**
     * 根据文件MD5和用户ID查询向量
     */
    List<DocumentVector> getVectorsByFileMd5AndUserId(String fileMd5, String userId);

    /**
     * 根据用户ID查询所有向量
     */
    List<DocumentVector> getVectorsByUserId(String userId);

    /**
     * 查询公开的向量记录
     */
    List<DocumentVector> getPublicVectors();

    /**
     * 根据组织标签查询向量
     */
    List<DocumentVector> getVectorsByOrgTag(String orgTag);

    /**
     * 删除文件的所有向量记录
     */
    int deleteVectorsByFileMd5(String fileMd5);

    /**
     * 删除单个向量记录
     */
    int deleteVectorById(Long vectorId);

    /**
     * 更新向量模型版本
     */
    int updateModelVersion(Long vectorId, String modelVersion);

    /**
     * 统计文件的向量数量
     */
    int countVectorsByFileMd5(String fileMd5);
}
