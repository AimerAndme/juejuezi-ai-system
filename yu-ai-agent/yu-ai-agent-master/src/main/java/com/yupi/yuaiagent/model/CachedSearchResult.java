package com.yupi.yuaiagent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.Document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedSearchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Document> documents;

    private Map<String, String> docVersions;

    private long cacheTime;

    private String originalQuery;

    private int topK;

    private int strategy;

    private double minScore;

    public CachedSearchResult(List<Document> documents, Map<String, String> docVersions) {
        this.documents = documents;
        this.docVersions = docVersions;
        this.cacheTime = System.currentTimeMillis();
    }

    public CachedSearchResult(List<Document> documents, Map<String, String> docVersions, 
                             String originalQuery, int topK, int strategy, double minScore) {
        this.documents = documents;
        this.docVersions = docVersions;
        this.originalQuery = originalQuery;
        this.topK = topK;
        this.strategy = strategy;
        this.minScore = minScore;
        this.cacheTime = System.currentTimeMillis();
    }

    public long getCacheAge() {
        return System.currentTimeMillis() - cacheTime;
    }

    public boolean isExpired(long ttlMillis) {
        return getCacheAge() > ttlMillis;
    }
}
