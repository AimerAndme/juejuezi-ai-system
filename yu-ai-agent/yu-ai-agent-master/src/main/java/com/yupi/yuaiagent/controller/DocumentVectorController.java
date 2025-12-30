package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.domin.entity.DocumentVector;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import com.yupi.yuaiagent.mapper.FileUploadMapper;
import com.yupi.yuaiagent.service.IDocumentVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档向量管理接口
 */
@Slf4j
@RestController
@RequestMapping("/vectors/manage")
@RequiredArgsConstructor
public class DocumentVectorController {

    private final IDocumentVectorService documentVectorService;
    private final FileUploadMapper fileUploadMapper;

    /**
     * 查询用户所有文件的向量（分页可选，当前返回全部）
     */
    @GetMapping("/list")
    public Map<String, Object> getVectorsByUserId(@RequestParam String userId) {
        log.info("[文档向量-列表] 查询用户所有向量, userId={}", userId);
        Map<String, Object> result = new HashMap<>();
        try {
            List<DocumentVector> vectors = documentVectorService.getVectorsByUserId(userId);
            result.put("code", 200);
            result.put("data", vectors);
            result.put("message", "查询成功");
        } catch (Exception e) {
            log.error("[文档向量-列表] 查询失败", e);
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据文件 MD5 查询该文件的所有分片向量（仅文件所有者可见）
     */
    @GetMapping("/list/file")
    public Map<String, Object> getVectorsByFileMd5(
            @RequestParam String fileMd5,
            @RequestParam String userId) {
        log.info("[文档向量-按文件查询] 查询文件向量, fileMd5={}, userId={}", fileMd5, userId);
        Map<String, Object> result = new HashMap<>();
        try {
            FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
            if (fileUpload == null) {
                result.put("code", 404);
                result.put("message", "文件不存在");
                return result;
            }
            // 只允许文件所有者查看
            if (!userId.equals(fileUpload.getUserId())) {
                result.put("code", 403);
                result.put("message", "无权限查看该文件的向量");
                return result;
            }

            List<DocumentVector> vectors
                    = documentVectorService.getVectorsByFileMd5AndUserId(fileMd5, userId);
            result.put("code", 200);
            result.put("data", vectors);
            result.put("message", "查询成功");
        } catch (Exception e) {
            log.error("[文档向量-按文件查询] 查询失败", e);
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除单个向量（仅向量所属用户可删）
     */
    @DeleteMapping("/{vectorId}")
    public Map<String, Object> deleteVectorById(
            @PathVariable Long vectorId,
            @RequestParam String userId) {
        log.info("[文档向量-删除] 删除向量, vectorId={}, userId={}", vectorId, userId);
        Map<String, Object> result = new HashMap<>();
        try {
            DocumentVector vector = documentVectorService.getVectorById(vectorId);
            if (vector == null) {
                result.put("code", 404);
                result.put("message", "向量不存在");
                return result;
            }
            if (!userId.equals(vector.getUserId())) {
                result.put("code", 403);
                result.put("message", "无权限删除该向量");
                return result;
            }

            documentVectorService.deleteVectorById(vectorId);
            result.put("code", 200);
            result.put("message", "删除成功");
        } catch (Exception e) {
            log.error("[文档向量-删除] 删除失败", e);
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除某文件的所有向量（仅文件所有者可删，可选）
     */
    @DeleteMapping("/file/{fileMd5}")
    public Map<String, Object> deleteVectorsByFileMd5(
            @PathVariable String fileMd5,
            @RequestParam String userId) {
        log.info("[文档向量-按文件删除] fileMd5={}, userId={}", fileMd5, userId);
        Map<String, Object> result = new HashMap<>();
        try {
            FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
            if (fileUpload == null) {
                result.put("code", 404);
                result.put("message", "文件不存在");
                return result;
            }
            if (!userId.equals(fileUpload.getUserId())) {
                result.put("code", 403);
                result.put("message", "无权限删除该文件的向量");
                return result;
            }

            documentVectorService.deleteVectorsByFileMd5(fileMd5);
            result.put("code", 200);
            result.put("message", "删除成功");
        } catch (Exception e) {
            log.error("[文档向量-按文件删除] 删除失败", e);
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }
}
