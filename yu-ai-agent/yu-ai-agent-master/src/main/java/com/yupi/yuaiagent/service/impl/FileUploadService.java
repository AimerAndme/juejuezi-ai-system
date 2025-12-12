package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.dto.*;
import com.yupi.yuaiagent.domin.entity.ChunkInfo;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import com.yupi.yuaiagent.mapper.ChunkInfoMapper;
import com.yupi.yuaiagent.mapper.FileUploadMapper;
import com.yupi.yuaiagent.service.IFileUploadService;
import com.yupi.yuaiagent.util.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FileUploadService implements IFileUploadService {

    private final FileUploadMapper fileUploadMapper;
    private final ChunkInfoMapper chunkInfoMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${file.upload.chunk-size:2097152}")
    private Long chunkSize;

    @Value("${file.upload.temp-dir:./upload/temp}")
    private String tempDir;

    @Value("${file.upload.final-dir:./upload/files}")
    private String finalDir;

    private static final String REDIS_UPLOAD_KEY_PREFIX = "file:upload:";

    public FileUploadService(FileUploadMapper fileUploadMapper,
            ChunkInfoMapper chunkInfoMapper,
            RedisTemplate<String, Object> redisTemplate) {
        this.fileUploadMapper = fileUploadMapper;
        this.chunkInfoMapper = chunkInfoMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public InitiateUploadResponse initiateUpload(InitiateUploadRequest request) {
        InitiateUploadResponse response = new InitiateUploadResponse();
        response.setFileMd5(request.getFileMd5());

        // 验证文件类型
        if (!FileUtils.isAllowedFileType(request.getFileName())) {
            response.setNeedUpload(false);
            response.setMessage("不支持的文件类型，仅支持 .docx, .md, .pdf");
            return response;
        }

        // 检查是否已完成上传（秒传）
        FileUpload existingFile = fileUploadMapper.selectByFileMd5(request.getFileMd5());
        if (existingFile != null && existingFile.getStatus() == 1) {
            response.setNeedUpload(false);
            response.setMessage("文件已存在，秒传成功");
            return response;
        }

        // 计算总分片数
        int totalChunks = (int) Math.ceil((double) request.getTotalSize() / chunkSize);
        response.setTotalChunks(totalChunks);

        // 检查是否有未完成的上传（断点续传）
        if (existingFile != null && existingFile.getStatus() == 0) {
            List<ChunkInfo> uploadedChunks = chunkInfoMapper.selectByFileMd5(request.getFileMd5());
            List<Integer> uploadedIndexes = uploadedChunks.stream()
                    .map(ChunkInfo::getChunkIndex)
                    .collect(Collectors.toList());

            response.setNeedUpload(true);
            response.setUploadedChunks(uploadedIndexes);
            response.setMessage("检测到未完成的上传，支持断点续传");

            // 更新Redis缓存
            updateRedisUploadStatus(request.getFileMd5(), uploadedIndexes);
            return response;
        }

        // 新上传任务
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileMd5(request.getFileMd5());
        fileUpload.setFileName(request.getFileName());
        fileUpload.setTotalSize(request.getTotalSize());
        fileUpload.setStatus(0); // 0: Uploading
        fileUpload.setUserId(request.getUserId());
        fileUpload.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        fileUpload.setCreatedAt(LocalDateTime.now());

        fileUploadMapper.insert(fileUpload);

        response.setNeedUpload(true);
        response.setUploadedChunks(new ArrayList<>());
        response.setMessage("初始化成功，开始上传");

        // 初始化Redis缓存
        updateRedisUploadStatus(request.getFileMd5(), new ArrayList<>());

        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> uploadChunk(MultipartFile file, String fileMd5, Integer chunkIndex,
            String chunkMd5, String userId) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // 验证分片MD5
        String actualMd5 = FileUtils.calculateMD5(file.getBytes());
        if (!actualMd5.equals(chunkMd5)) {
            result.put("code", 400);
            result.put("message", "分片MD5校验失败");
            return result;
        }

        // 检查是否已上传该分片
        ChunkInfo existingChunk = chunkInfoMapper.selectByFileMd5AndChunkIndex(fileMd5, chunkIndex);
        if (existingChunk != null) {
            result.put("code", 200);
            result.put("message", "该分片已上传");
            return result;
        }

        // 保存分片到临时目录
        String chunkDir = tempDir + File.separator + fileMd5;
        FileUtils.ensureDirectory(chunkDir);

        String chunkFileName = chunkIndex + ".chunk";
        File chunkFile = new File(chunkDir, chunkFileName);
        file.transferTo(chunkFile);

        // 保存分片信息到数据库
        ChunkInfo chunkInfo = new ChunkInfo();
        chunkInfo.setFileMd5(fileMd5);
        chunkInfo.setChunkIndex(chunkIndex);
        chunkInfo.setChunkMd5(chunkMd5);
        chunkInfo.setStoragePath(chunkFile.getAbsolutePath());
        chunkInfoMapper.insert(chunkInfo);

        // 更新Redis缓存
        String redisKey = REDIS_UPLOAD_KEY_PREFIX + fileMd5;
        redisTemplate.opsForSet().add(redisKey, chunkIndex);
        redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);

        result.put("code", 200);
        result.put("message", "分片上传成功");
        result.put("chunkIndex", chunkIndex);

        return result;
    }

    @Override
    public UploadStatusResponse getUploadStatus(String fileMd5, String userId) {
        UploadStatusResponse response = new UploadStatusResponse();

        FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
        if (fileUpload == null) {
            return response;
        }

        response.setFileMd5(fileMd5);
        response.setStatus(fileUpload.getStatus());
        response.setFileName(fileUpload.getFileName());
        response.setTotalSize(fileUpload.getTotalSize());

        // 计算总分片数
        int totalChunks = (int) Math.ceil((double) fileUpload.getTotalSize() / chunkSize);
        response.setTotalChunks(totalChunks);

        // 获取已上传的分片
        List<ChunkInfo> chunks = chunkInfoMapper.selectByFileMd5(fileMd5);
        List<Integer> uploadedChunks = chunks.stream()
                .map(ChunkInfo::getChunkIndex)
                .collect(Collectors.toList());
        response.setUploadedChunks(uploadedChunks);

        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> completeUpload(CompleteUploadRequest request) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String fileMd5 = request.getFileMd5();

        FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
        if (fileUpload == null) {
            result.put("code", 404);
            result.put("message", "文件上传记录不存在");
            return result;
        }

        // 检查所有分片是否已上传
        int totalChunks = (int) Math.ceil((double) fileUpload.getTotalSize() / chunkSize);
        int uploadedCount = chunkInfoMapper.countByFileMd5(fileMd5);

        if (uploadedCount != totalChunks) {
            result.put("code", 400);
            result.put("message", "分片未全部上传，无法合并");
            result.put("uploaded", uploadedCount);
            result.put("total", totalChunks);
            return result;
        }

        // 获取所有分片
        List<ChunkInfo> chunks = chunkInfoMapper.selectByFileMd5(fileMd5);
        List<File> chunkFiles = chunks.stream()
                .sorted(Comparator.comparing(ChunkInfo::getChunkIndex))
                .map(chunk -> new File(chunk.getStoragePath()))
                .collect(Collectors.toList());

        // 合并分片
        FileUtils.ensureDirectory(finalDir);
        File mergedFile = new File(finalDir, fileMd5 + "_" + fileUpload.getFileName());
        FileUtils.mergeChunks(chunkFiles, mergedFile);

        // 验证合并后的文件MD5
        String mergedMd5 = FileUtils.calculateMD5(mergedFile);
        if (!mergedMd5.equals(fileMd5)) {
            result.put("code", 500);
            result.put("message", "文件合并后MD5校验失败");
            return result;
        }

        // 更新数据库状态
        fileUploadMapper.updateStatus(fileMd5, 1); // 1: Completed
        fileUploadMapper.updateMergedAt(fileMd5);

        // 清理临时文件
        String tempChunkDir = tempDir + File.separator + fileMd5;
        FileUtils.deleteFileOrDirectory(new File(tempChunkDir));

        // 清除Redis缓存
        String redisKey = REDIS_UPLOAD_KEY_PREFIX + fileMd5;
        redisTemplate.delete(redisKey);

        result.put("code", 200);
        result.put("message", "文件上传完成");
        result.put("filePath", mergedFile.getAbsolutePath());
        result.put("fileName", fileUpload.getFileName());

        return result;
    }

    private void updateRedisUploadStatus(String fileMd5, List<Integer> uploadedChunks) {
        String redisKey = REDIS_UPLOAD_KEY_PREFIX + fileMd5;
        redisTemplate.delete(redisKey);
        if (!uploadedChunks.isEmpty()) {
            redisTemplate.opsForSet().add(redisKey, uploadedChunks.toArray());
        }
        redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);
    }
}
