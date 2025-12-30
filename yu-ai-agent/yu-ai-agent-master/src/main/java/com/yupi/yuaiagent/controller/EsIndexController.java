package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.service.ElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch索引管理接口 提供ES文档的增删改查操作
 */
@Slf4j
@RestController
@RequestMapping("/es/index")
@RequiredArgsConstructor
public class EsIndexController {

    private final ElasticsearchService elasticsearchService;

    /**
     * 根据文件MD5查询所有分片文档（分页）
     *
     * @param fileMd5 文件MD5
     * @param from 起始位置，默认0
     * @param size 查询数量，默认100
     */
    @GetMapping("/query/fileMd5")
    public Map<String, Object> queryByFileMd5(
            @RequestParam String fileMd5,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "100") int size) {
        log.info("[ES索引-查询] 根据fileMd5查询, fileMd5={}, from={}, size={}", fileMd5, from, size);
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> docs = elasticsearchService.queryByFileMd5(fileMd5, from, size);
            long total = elasticsearchService.countByFileMd5(fileMd5);

            result.put("code", 200);
            result.put("data", docs);
            result.put("total", total);
            result.put("message", "查询成功");
        } catch (Exception e) {
            log.error("[ES索引-查询] 查询失败", e);
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据文档ID查询单个文档
     *
     * @param docId 文档ID
     */
    @GetMapping("/query/{docId}")
    public Map<String, Object> getById(@PathVariable String docId) {
        log.info("[ES索引-查询] 根据docId查询, docId={}", docId);
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> doc = elasticsearchService.getById(docId);
            if (doc == null) {
                result.put("code", 404);
                result.put("message", "文档不存在");
                return result;
            }
            result.put("code", 200);
            result.put("data", doc);
            result.put("message", "查询成功");
        } catch (Exception e) {
            log.error("[ES索引-查询] 查询失败", e);
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 统计fileMd5对应的文档数量
     *
     * @param fileMd5 文件MD5
     */
    @GetMapping("/count/fileMd5")
    public Map<String, Object> countByFileMd5(@RequestParam String fileMd5) {
        log.info("[ES索引-统计] fileMd5={}", fileMd5);
        Map<String, Object> result = new HashMap<>();
        try {
            long count = elasticsearchService.countByFileMd5(fileMd5);
            result.put("code", 200);
            result.put("count", count);
            result.put("message", "统计成功");
        } catch (Exception e) {
            log.error("[ES索引-统计] 统计失败", e);
            result.put("code", 500);
            result.put("message", "统计失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据fileMd5删除该文件的所有分片文档
     *
     * @param fileMd5 文件MD5
     */
    @DeleteMapping("/delete/fileMd5")
    public Map<String, Object> deleteByFileMd5(@RequestParam String fileMd5) {
        log.info("[ES索引-删除] 根据fileMd5删除, fileMd5={}", fileMd5);
        Map<String, Object> result = new HashMap<>();
        try {
            elasticsearchService.deleteByFileMd5(fileMd5);
            result.put("code", 200);
            result.put("message", "删除成功");
        } catch (Exception e) {
            log.error("[ES索引-删除] 删除失败", e);
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据文档ID删除单个文档
     *
     * @param docId 文档ID
     */
    @DeleteMapping("/delete/{docId}")
    public Map<String, Object> deleteById(@PathVariable String docId) {
        log.info("[ES索引-删除] 根据docId删除, docId={}", docId);
        Map<String, Object> result = new HashMap<>();
        try {
            elasticsearchService.deleteById(docId);
            result.put("code", 200);
            result.put("message", "删除成功");
        } catch (Exception e) {
            log.error("[ES索引-删除] 删除失败", e);
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }
}
