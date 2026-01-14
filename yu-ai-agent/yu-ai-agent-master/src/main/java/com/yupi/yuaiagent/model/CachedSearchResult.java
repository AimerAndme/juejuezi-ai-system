package com.yupi.yuaiagent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CachedSearchResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<CachedDocument> documents;

    private Map<String, String> docVersions;

    private long cacheTime;

    private String originalQuery;

    private int topK;

    private int strategy;

    private double minScore;

    public CachedSearchResult(List<Document> documents, Map<String, String> docVersions) {
        this.documents = documents.stream()
                .map(CachedDocument::fromDocument)
                .collect(Collectors.toList());
        this.docVersions = docVersions;
        this.cacheTime = System.currentTimeMillis();
    }

    public CachedSearchResult(List<Document> documents, Map<String, String> docVersions,
            String originalQuery, int topK, int strategy, double minScore) {
        this.documents = documents.stream()
                .map(CachedDocument::fromDocument)
                .collect(Collectors.toList());
        this.docVersions = docVersions;
        this.originalQuery = originalQuery;
        this.topK = topK;
        this.strategy = strategy;
        this.minScore = minScore;
        this.cacheTime = System.currentTimeMillis();
    }

    @JsonIgnore
    public List<Document> getDocumentsAsList() {
        if (documents == null) {
            return List.of();
        }
        return documents.stream()
                .map(CachedDocument::toDocument)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public long getCacheAge() {
        return System.currentTimeMillis() - cacheTime;
    }

    @JsonIgnore
    public boolean isExpired(long ttlMillis) {
        return getCacheAge() > ttlMillis;
    }
}
