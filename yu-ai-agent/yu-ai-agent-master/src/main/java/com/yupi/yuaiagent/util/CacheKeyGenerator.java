package com.yupi.yuaiagent.util;

public class CacheKeyGenerator {
    private static final String CACHE_KEY_PREFIX = "cache:";
    private static final String HISTOGRAM_CACHE_KEY_PREFIX = CACHE_KEY_PREFIX + "visualization:histogram";

    public static String generateHistogramCacheKey(String areaId, int intervalCount, Double minValue, Double maxValue, Integer count) {
        String key = areaId + ":" + intervalCount + ":" + minValue + ":" + maxValue + ":" + count;
        return HISTOGRAM_CACHE_KEY_PREFIX + ":" + key;
    }
}
