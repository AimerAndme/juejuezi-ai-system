package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.DocumentVector;
import com.yupi.yuaiagent.mapper.DocumentVectorMapper;
import com.yupi.yuaiagent.service.IDocumentVectorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文档向量服务实现
 */
@Slf4j
@Service
public class DocumentVectorServiceImpl implements IDocumentVectorService {

    @Resource
    private DocumentVectorMapper documentVectorMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveVector(DocumentVector documentVector) {
        log.info("保存向量记录: fileMd5={}, chunkId={}, userId={}",
                documentVector.getFileMd5(), documentVector.getChunkId(), documentVector.getUserId());
        return documentVectorMapper.insert(documentVector);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveVectors(List<DocumentVector> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            log.warn("批量保存向量记录失败: 向量列表为空");
            return 0;
        }
        log.info("批量保存向量记录: count={}", vectors.size());
        return documentVectorMapper.batchInsert(vectors);
    }

    @Override
    public DocumentVector getVectorById(Long vectorId) {
        log.info("根据向量ID查询: vectorId={}", vectorId);
        return documentVectorMapper.selectByVectorId(vectorId);
    }

    @Override
    public List<DocumentVector> getVectorsByFileMd5(String fileMd5) {
        log.info("根据文件MD5查询向量: fileMd5={}", fileMd5);
        return documentVectorMapper.selectByFileMd5(fileMd5);
    }

    @Override
    public List<DocumentVector> getVectorsByFileMd5AndUserId(String fileMd5, String userId) {
        log.info("根据文件MD5和用户ID查询向量: fileMd5={}, userId={}", fileMd5, userId);
        return documentVectorMapper.selectByFileMd5AndUserId(fileMd5, userId);
    }

    @Override
    public List<DocumentVector> getVectorsByUserId(String userId) {
        log.info("根据用户ID查询向量: userId={}", userId);
        return documentVectorMapper.selectByUserId(userId);
    }

    @Override
    public List<DocumentVector> getPublicVectors() {
        log.info("查询公开的向量记录");
        return documentVectorMapper.selectPublicVectors();
    }

    @Override
    public List<DocumentVector> getVectorsByOrgTag(String orgTag) {
        log.info("根据组织标签查询向量: orgTag={}", orgTag);
        return documentVectorMapper.selectByOrgTag(orgTag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteVectorsByFileMd5(String fileMd5) {
        log.info("删除文件的所有向量记录: fileMd5={}", fileMd5);
        return documentVectorMapper.deleteByFileMd5(fileMd5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteVectorById(Long vectorId) {
        log.info("删除向量记录: vectorId={}", vectorId);
        return documentVectorMapper.deleteByVectorId(vectorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateModelVersion(Long vectorId, String modelVersion) {
        log.info("更新向量模型版本: vectorId={}, modelVersion={}", vectorId, modelVersion);
        return documentVectorMapper.updateModelVersion(vectorId, modelVersion);
    }

    @Override
    public int countVectorsByFileMd5(String fileMd5) {
        log.info("统计文件的向量数量: fileMd5={}", fileMd5);
        return documentVectorMapper.countByFileMd5(fileMd5);
    }
}
