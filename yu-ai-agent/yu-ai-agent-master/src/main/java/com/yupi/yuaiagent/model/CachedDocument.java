package com.yupi.yuaiagent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private String text;

    private Map<String, Object> metadata;

    public static CachedDocument fromDocument(org.springframework.ai.document.Document document) {
        return CachedDocument.builder()
                .id(document.getId())
                .text(document.getText())
                .metadata(document.getMetadata())
                .build();
    }

    public org.springframework.ai.document.Document toDocument() {
        org.springframework.ai.document.Document document = new org.springframework.ai.document.Document(text, metadata);
        if (id != null) {
            document.getMetadata().put("id", id);
        }
        return document;
    }
}
