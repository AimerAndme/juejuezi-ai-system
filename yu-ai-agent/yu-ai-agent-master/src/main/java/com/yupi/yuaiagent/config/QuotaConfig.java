package com.yupi.yuaiagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "quota")
public class QuotaConfig {
    private Map<String, QuotaRule> rules = new HashMap<>();

    @Data
    public static class QuotaRule {
        private long windowMs;
        private long maxTokens;
        private int thresholdPercent;
        private String description;
    }
}
