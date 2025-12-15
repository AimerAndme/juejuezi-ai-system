package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.domin.dto.*;
import com.yupi.yuaiagent.service.IFileUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/files/upload")
public class FileUploadController {

    private final IFileUploadService fileUploadService;

    public FileUploadController(IFileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * 初始化上传
     */
    @PostMapping("/initiate")
    public Map<String, Object> initiateUpload(@RequestBody InitiateUploadRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 先验证文件类型
            if (!com.yupi.yuaiagent.util.FileUtils.isAllowedFileType(request.getFileName())) {
                result.put("code", 400);
                result.put("message", "不支持的文件类型，仅支持 .docx, .md, .pdf");
                result.put("data", null);
                return result;
            }

            InitiateUploadResponse response = fileUploadService.initiateUpload(request);
            result.put("code", 200);
            result.put("message", "初始化成功");
            result.put("data", response);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "初始化失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 上传分片
     */
    @PostMapping("/chunk")
    public Map<String, Object> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileMd5") String fileMd5,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("chunkMd5") String chunkMd5,
            @RequestParam("userId") String userId) {

        try {
            return fileUploadService.uploadChunk(file, fileMd5, chunkIndex, chunkMd5, userId);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "分片上传失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 查询上传状态
     */
    @GetMapping("/status/{fileMd5}")
    public Map<String, Object> getUploadStatus(
            @PathVariable String fileMd5,
            @RequestParam String userId) {

        Map<String, Object> result = new HashMap<>();

        try {
            UploadStatusResponse response = fileUploadService.getUploadStatus(fileMd5, userId);
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", response);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 完成上传（合并分片）
     */
    @PostMapping("/complete")
    public Map<String, Object> completeUpload(@RequestBody CompleteUploadRequest request) {
        try {
            return fileUploadService.completeUpload(request);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "完成上传失败: " + e.getMessage());
            return result;
        }
    }
}
