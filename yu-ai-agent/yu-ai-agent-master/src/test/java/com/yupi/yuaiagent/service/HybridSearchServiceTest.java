package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.model.CacheStatistics;
import com.yupi.yuaiagent.model.CachedSearchResult;
import com.yupi.yuaiagent.utils.QueryNormalizer;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HybridSearchServiceTest {

    @Test
    void testQueryNormalizer() {
        String query1 = "  传感器  安装  ";
        String query2 = "传感器安装";
        String query3 = "传感器，安装！";
        String query4 = "SENSOR INSTALLATION";
        String query5 = "sensor installation";

        String normalized1 = QueryNormalizer.normalizeForCacheKey(query1);
        String normalized2 = QueryNormalizer.normalizeForCacheKey(query2);
        String normalized3 = QueryNormalizer.normalizeForCacheKey(query3);
        String normalized4 = QueryNormalizer.normalizeForCacheKey(query4);
        String normalized5 = QueryNormalizer.normalizeForCacheKey(query5);

        System.out.println("原始查询1: " + query1 + " -> 标准化: " + normalized1);
        System.out.println("原始查询2: " + query2 + " -> 标准化: " + normalized2);
        System.out.println("原始查询3: " + query3 + " -> 标准化: " + normalized3);
        System.out.println("原始查询4: " + query4 + " -> 标准化: " + normalized4);
        System.out.println("原始查询5: " + query5 + " -> 标准化: " + normalized5);

        assertEquals(normalized1, normalized2);
        assertEquals(normalized2, normalized3);
        assertEquals(normalized4, normalized5);
    }

    @Test
    void testCachedSearchResult() {
        List<Document> documents = new ArrayList<>();
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("id", "doc1");
        metadata1.put("score", 0.95);
        documents.add(Document.builder().text("文档1内容").metadata(metadata1).build());

        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("id", "doc2");
        metadata2.put("score", 0.85);
        documents.add(Document.builder().text("文档2内容").metadata(metadata2).build());

        Map<String, String> docVersions = new HashMap<>();
        docVersions.put("doc1", "v1");
        docVersions.put("doc2", "v1");

        CachedSearchResult cachedResult = new CachedSearchResult(
                documents,
                docVersions,
                "传感器安装",
                10,
                0,
                0.0
        );

        assertNotNull(cachedResult);
        assertEquals(2, cachedResult.getDocumentsAsList().size());
        assertEquals("传感器安装", cachedResult.getOriginalQuery());
        assertEquals(10, cachedResult.getTopK());
        assertEquals(0, cachedResult.getStrategy());
        assertEquals(0.0, cachedResult.getMinScore());
        assertNotNull(cachedResult.getCacheTime());
        assertTrue(cachedResult.getCacheAge() >= 0);

        System.out.println("缓存结果: " + cachedResult);
        System.out.println("缓存时间: " + cachedResult.getCacheTime());
        System.out.println("缓存年龄: " + cachedResult.getCacheAge() + "ms");
    }

    @Test
    void testCacheStatistics() {
        CacheStatistics statistics = new CacheStatistics();

        statistics.incrementRequests();
        statistics.incrementRequests();
        statistics.incrementRequests();

        statistics.incrementHits();
        statistics.incrementHits();

        statistics.incrementMisses();

        statistics.incrementInvalidations();

        statistics.incrementEvictions();

        System.out.println("缓存统计: " + statistics);

        assertEquals(3, statistics.getTotalRequests().get());
        assertEquals(2, statistics.getCacheHits().get());
        assertEquals(1, statistics.getCacheMisses().get());
        assertEquals(1, statistics.getCacheInvalidations().get());
        assertEquals(1, statistics.getCacheEvictions().get());
        assertEquals(2.0 / 3.0, statistics.getHitRate(), 0.001);
        assertEquals(1.0 / 3.0, statistics.getMissRate(), 0.001);

        statistics.reset();

        assertEquals(0, statistics.getTotalRequests().get());
        assertEquals(0, statistics.getCacheHits().get());
        assertEquals(0, statistics.getCacheMisses().get());
        assertEquals(0, statistics.getCacheInvalidations().get());
        assertEquals(0, statistics.getCacheEvictions().get());
        assertEquals(0.0, statistics.getHitRate());
        assertEquals(0.0, statistics.getMissRate());
    }

    @Test
    void testCacheKeyGeneration() {
        String query = "传感器安装";
        int topK = 10;
        int strategy = 0;
        double minScore = 0.0;

        String normalizedQuery = QueryNormalizer.normalizeForCacheKey(query);
        String cacheKey = "search:" + normalizedQuery + ":" + topK + ":" + strategy + ":" + minScore;

        System.out.println("原始查询: " + query);
        System.out.println("标准化查询: " + normalizedQuery);
        System.out.println("缓存Key: " + cacheKey);

        assertNotNull(cacheKey);
        assertTrue(cacheKey.startsWith("search:"));
        assertTrue(cacheKey.contains(":10:0:0.0"));
    }

    @Test
    void testDifferentQueriesShouldHaveDifferentCacheKeys() {
        String query1 = "传感器安装";
        String query2 = "系统架构";

        String normalizedQuery1 = QueryNormalizer.normalizeForCacheKey(query1);
        String normalizedQuery2 = QueryNormalizer.normalizeForCacheKey(query2);

        String cacheKey1 = "search:" + normalizedQuery1 + ":10:0:0.0";
        String cacheKey2 = "search:" + normalizedQuery2 + ":10:0:0.0";

        System.out.println("查询1缓存Key: " + cacheKey1);
        System.out.println("查询2缓存Key: " + cacheKey2);

        assertNotEquals(cacheKey1, cacheKey2);
    }

    @Test
    void testSimilarQueriesShouldHaveSameCacheKeys() {
        String query1 = "  传感器  安装  ";
        String query2 = "传感器安装";
        String query3 = "传感器，安装！";

        String normalizedQuery1 = QueryNormalizer.normalizeForCacheKey(query1);
        String normalizedQuery2 = QueryNormalizer.normalizeForCacheKey(query2);
        String normalizedQuery3 = QueryNormalizer.normalizeForCacheKey(query3);

        String cacheKey1 = "search:" + normalizedQuery1 + ":10:0:0.0";
        String cacheKey2 = "search:" + normalizedQuery2 + ":10:0:0.0";
        String cacheKey3 = "search:" + normalizedQuery3 + ":10:0:0.0";

        System.out.println("查询1缓存Key: " + cacheKey1);
        System.out.println("查询2缓存Key: " + cacheKey2);
        System.out.println("查询3缓存Key: " + cacheKey3);

        assertEquals(cacheKey1, cacheKey2);
        assertEquals(cacheKey2, cacheKey3);
    }
}
