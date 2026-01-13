package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.model.CacheStatistics;
import com.yupi.yuaiagent.service.HybridSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cache")
public class CacheController {

    private final HybridSearchService hybridSearchService;

    public CacheController(HybridSearchService hybridSearchService) {
        this.hybridSearchService = hybridSearchService;
    }

    @GetMapping("/statistics")
    public Map<String, Object> getCacheStatistics() {
        log.info("[缓存统计] 查询缓存统计信息");
        Map<String, Object> result = new HashMap<>();

        try {
            CacheStatistics statistics = hybridSearchService.getCacheStatistics();
            result.put("code", 200);
            result.put("data", statistics);
            result.put("message", "查询成功");
            log.info("[缓存统计] 查询成功: {}", statistics);
        } catch (Exception e) {
            log.error("[缓存统计] 查询失败", e);
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/statistics/reset")
    public Map<String, Object> resetCacheStatistics() {
        log.info("[缓存统计] 重置缓存统计信息");
        Map<String, Object> result = new HashMap<>();

        try {
            hybridSearchService.resetCacheStatistics();
            result.put("code", 200);
            result.put("message", "重置成功");
            log.info("[缓存统计] 重置成功");
        } catch (Exception e) {
            log.error("[缓存统计] 重置失败", e);
            result.put("code", 500);
            result.put("message", "重置失败: " + e.getMessage());
        }

        return result;
    }

    @DeleteMapping("/invalidate/{docId}")
    public Map<String, Object> invalidateDocumentCache(@PathVariable String docId) {
        log.info("[缓存管理] 使文档缓存失效, docId={}", docId);
        Map<String, Object> result = new HashMap<>();

        try {
            hybridSearchService.invalidateDocumentCache(docId);
            result.put("code", 200);
            result.put("message", "缓存失效成功");
            log.info("[缓存管理] 文档缓存失效成功, docId={}", docId);
        } catch (Exception e) {
            log.error("[缓存管理] 使文档缓存失败", e);
            result.put("code", 500);
            result.put("message", "缓存失效失败: " + e.getMessage());
        }

        return result;
    }

    @DeleteMapping("/invalidate/all")
    public Map<String, Object> invalidateAllCache() {
        log.info("[缓存管理] 使所有文档缓存失效");
        Map<String, Object> result = new HashMap<>();

        try {
            hybridSearchService.invalidateAllDocumentCache();
            result.put("code", 200);
            result.put("message", "所有缓存失效成功");
            log.info("[缓存管理] 所有文档缓存失效成功");
        } catch (Exception e) {
            log.error("[缓存管理] 使所有文档缓存失败", e);
            result.put("code", 500);
            result.put("message", "缓存失效失败: " + e.getMessage());
        }

        return result;
    }
}
