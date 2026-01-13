package com.yupi.yuaiagent.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class QueryNormalizer {

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern PUNCTUATION = Pattern.compile("[\\p{P}\\p{S}]");
    private static final Pattern CHINESE_PUNCTUATION = Pattern.compile("[\\u3000-\\u303F\\uFF00-\\uFFEF]");

    public static String normalize(String query) {
        if (StringUtils.isBlank(query)) {
            return "";
        }

        String normalized = query;

        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKC);

        normalized = normalized.trim();

        normalized = MULTIPLE_SPACES.matcher(normalized).replaceAll(" ");

        normalized = PUNCTUATION.matcher(normalized).replaceAll("");

        normalized = CHINESE_PUNCTUATION.matcher(normalized).replaceAll("");

        normalized = normalized.toLowerCase();

        normalized = normalized.trim();

        return normalized;
    }

    public static String normalizeWithPreservePunctuation(String query) {
        if (StringUtils.isBlank(query)) {
            return "";
        }

        String normalized = query;

        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKC);

        normalized = normalized.trim();

        normalized = MULTIPLE_SPACES.matcher(normalized).replaceAll(" ");

        normalized = normalized.toLowerCase();

        normalized = normalized.trim();

        return normalized;
    }

    public static String normalizeForCacheKey(String query) {
        if (StringUtils.isBlank(query)) {
            return "";
        }

        String normalized = query;

        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKC);

        normalized = normalized.trim();

        normalized = MULTIPLE_SPACES.matcher(normalized).replaceAll(" ");

        normalized = normalized.toLowerCase();

        normalized = normalized.trim();

        return normalized;
    }
}
