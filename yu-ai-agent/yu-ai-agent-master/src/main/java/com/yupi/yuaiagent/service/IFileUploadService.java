package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.dto.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface IFileUploadService {

    /**
     * 初始化上传（检查秒传和断点续传）
     */
    InitiateUploadResponse initiateUpload(InitiateUploadRequest request);

    /**
     * 上传分片
     */
    Map<String, Object> uploadChunk(MultipartFile file, String fileMd5, Integer chunkIndex, String chunkMd5, String userId) throws Exception;

    /**
     * 查询上传状态
     */
    UploadStatusResponse getUploadStatus(String fileMd5, String userId);

    /**
     * 完成上传（合并分片）
     */
    Map<String, Object> completeUpload(CompleteUploadRequest request) throws Exception;
}
