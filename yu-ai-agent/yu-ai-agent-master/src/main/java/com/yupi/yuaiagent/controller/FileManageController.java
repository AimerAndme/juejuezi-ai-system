package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.domin.constant.FileConstant;
import com.yupi.yuaiagent.domin.entity.FileExtractedImages;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import com.yupi.yuaiagent.mapper.ChunkInfoMapper;
import com.yupi.yuaiagent.mapper.DocumentVectorMapper;
import com.yupi.yuaiagent.mapper.FileExtractedImagesMapper;
import com.yupi.yuaiagent.mapper.FileUploadMapper;
import com.yupi.yuaiagent.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/files/manage")
public class FileManageController {

    private final FileUploadMapper fileUploadMapper;
    private final ChunkInfoMapper chunkInfoMapper;
    private final DocumentVectorMapper documentVectorMapper;
    private final FileExtractedImagesMapper fileExtractedImagesMapper;
    @Value("${file.upload.final-dir:./upload/files}")
    private String finalDir;

    public FileManageController(FileUploadMapper fileUploadMapper, ChunkInfoMapper chunkInfoMapper, DocumentVectorMapper documentVectorMapper, FileExtractedImagesMapper fileExtractedImagesMapper) {
        this.fileUploadMapper = fileUploadMapper;
        this.chunkInfoMapper = chunkInfoMapper;
        this.documentVectorMapper = documentVectorMapper;
        this.fileExtractedImagesMapper = fileExtractedImagesMapper;
    }

    /**
     * 查询用户的文件列表
     */
    @GetMapping("/list")
    public Map<String, Object> getFileList(@RequestParam String userId) {
        log.info("[文件管理-列表] 查询用户文件, userId={}", userId);
        Map<String, Object> result = new HashMap<>();

        try {
            List<FileUpload> files = fileUploadMapper.selectByUserId(userId);
            result.put("code", 200);
            result.put("data", files);
            result.put("message", "查询成功");
            log.info("[文件管理-列表] 查询成功, count={}", files.size());
        } catch (Exception e) {
            log.error("[文件管理-列表] 查询失败", e);
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 查询所有文件列表（管理员）
     */
    @GetMapping("/list/all")
    public Map<String, Object> getAllFileList() {
        log.info("[文件管理-列表] 查询所有文件");
        Map<String, Object> result = new HashMap<>();

        try {
            List<FileUpload> files = fileUploadMapper.selectAll();
            result.put("code", 200);
            result.put("data", files);
            result.put("message", "查询成功");
            log.info("[文件管理-列表] 查询成功, count={}", files.size());
        } catch (Exception e) {
            log.error("[文件管理-列表] 查询失败", e);
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileMd5}")
    @Transactional
    public Map<String, Object> deleteFile(@PathVariable String fileMd5, @RequestParam String userId) {
        log.info("[文件管理-删除] 删除文件, fileMd5={}, userId={}", fileMd5, userId);
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询文件信息
            FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
            if (fileUpload == null) {
                result.put("code", 404);
                result.put("message", "文件不存在");
                return result;
            }

            // 验证权限
            if (!fileUpload.getUserId().equals(userId)) {
                log.warn("[文件管理-删除] 权限不足, userId={}", userId);
                result.put("code", 403);
                result.put("message", "无权限删除此文件");
                return result;
            }

            // 删除数据库记录
            chunkInfoMapper.deleteByFileMd5(fileMd5);
            log.info("[文件管理-删除] 删除数据库分片记录");
            fileUploadMapper.deleteByFileMd5(fileMd5);
            log.info("[文件管理-删除] 删除数据库文件记录");
            documentVectorMapper.deleteByFileMd5(fileMd5);
            log.info("[文件管理-删除] 删除文件向量记录");
            // 删除物理文件
            String filePath = finalDir + File.separator + fileMd5 + "_" + fileUpload.getFileName();
            List<FileExtractedImages> fileExtractedImages = fileExtractedImagesMapper.selectByFileMd5(fileMd5);
            for (FileExtractedImages fileExtractedImage : fileExtractedImages) {
                String imagePath = fileExtractedImage.getImagePath();
                FileUtils.deleteFileOrDirectory(new File(imagePath));
                log.info("[文件管理-删除] 删除图片文件, path={}", imagePath);
            }
            fileExtractedImagesMapper.deleteByFileMd5(fileMd5);
            log.info("[文件管理-删除] 删除文件, path={}", filePath);
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
                log.info("[文件管理-删除] 物理文件已删除, path={}", filePath);
            }
            result.put("code", 200);
            result.put("message", "删除成功");
            log.info("[文件管理-删除] 删除成功");
        } catch (Exception e) {
            log.error("[文件管理-删除] 删除失败", e);
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新文件名
     */
    @PutMapping("/{fileMd5}/name")
    public Map<String, Object> updateFileName(
            @PathVariable String fileMd5,
            @RequestParam String fileName,
            @RequestParam String userId) {
        log.info("[文件管理-重命名] fileMd5={}, newName={}", fileMd5, fileName);
        Map<String, Object> result = new HashMap<>();

        try {
            FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
            if (fileUpload == null) {
                result.put("code", 404);
                result.put("message", "文件不存在");
                return result;
            }

            // 验证权限
            if (!fileUpload.getUserId().equals(userId)) {
                result.put("code", 403);
                result.put("message", "无权限修改此文件");
                return result;
            }

            // 验证文件类型
            if (!FileUtils.isAllowedFileType(fileName)) {
                result.put("code", 400);
                result.put("message", "不支持的文件类型");
                return result;
            }

            // 重命名物理文件
            String oldPath = finalDir + File.separator + fileMd5 + "_" + fileUpload.getFileName();
            String newPath = finalDir + File.separator + fileMd5 + "_" + fileName;
            File oldFile = new File(oldPath);
            File newFile = new File(newPath);

            if (oldFile.exists()) {
                oldFile.renameTo(newFile);
            }

            // 更新数据库
            fileUploadMapper.updateFileName(fileMd5, fileName);

            result.put("code", 200);
            result.put("message", "重命名成功");
            log.info("[文件管理-重命名] 成功");
        } catch (Exception e) {
            log.error("[文件管理-重命名] 失败", e);
            result.put("code", 500);
            result.put("message", "重命名失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新文件公开状态
     */
    @PutMapping("/{fileMd5}/public")
    public Map<String, Object> updateIsPublic(
            @PathVariable String fileMd5,
            @RequestParam Boolean isPublic,
            @RequestParam String userId) {
        log.info("[文件管理-公开设置] fileMd5={}, isPublic={}", fileMd5, isPublic);
        Map<String, Object> result = new HashMap<>();

        try {
            FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
            if (fileUpload == null) {
                result.put("code", 404);
                result.put("message", "文件不存在");
                return result;
            }

            if (!fileUpload.getUserId().equals(userId)) {
                result.put("code", 403);
                result.put("message", "无权限修改此文件");
                return result;
            }

            fileUploadMapper.updateIsPublic(fileMd5, isPublic);

            result.put("code", 200);
            result.put("message", "更新成功");
            log.info("[文件管理-公开设置] 成功");
        } catch (Exception e) {
            log.error("[文件管理-公开设置] 失败", e);
            result.put("code", 500);
            result.put("message", "更新失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 下载文件
     */
    @GetMapping("/{fileMd5}/download")
    public Map<String, Object> getDownloadInfo(@PathVariable String fileMd5, @RequestParam String userId) {
        log.info("[文件管理-下载] fileMd5={}, userId={}", fileMd5, userId);
        Map<String, Object> result = new HashMap<>();

        try {
            FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
            if (fileUpload == null) {
                result.put("code", 404);
                result.put("message", "文件不存在");
                return result;
            }

            // 检查权限（公开文件或自己的文件）
            if (!fileUpload.getIsPublic() && !fileUpload.getUserId().equals(userId)) {
                result.put("code", 403);
                result.put("message", "无权限访问此文件");
                return result;
            }

            String filePath = finalDir + File.separator + fileMd5 + "_" + fileUpload.getFileName();
            File file = new File(filePath);

            if (!file.exists()) {
                result.put("code", 404);
                result.put("message", "文件不存在");
                return result;
            }

            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileName", fileUpload.getFileName());
            fileInfo.put("filePath", filePath);
            fileInfo.put("fileSize", file.length());

            result.put("code", 200);
            result.put("data", fileInfo);
            result.put("message", "获取成功");
        } catch (Exception e) {
            log.error("[文件管理-下载] 失败", e);
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
        }

        return result;
    }
}
