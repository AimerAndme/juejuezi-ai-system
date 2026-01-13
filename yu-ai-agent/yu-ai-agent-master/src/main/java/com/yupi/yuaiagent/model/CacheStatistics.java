package com.yupi.yuaiagent.model;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class CacheStatistics {

    private final AtomicLong totalRequests = new AtomicLong(0);

    private final AtomicLong cacheHits = new AtomicLong(0);

    private final AtomicLong cacheMisses = new AtomicLong(0);

    private final AtomicLong cacheInvalidations = new AtomicLong(0);

    private final AtomicLong cacheEvictions = new AtomicLong(0);

    public void incrementRequests() {
        totalRequests.incrementAndGet();
    }

    public void incrementHits() {
        cacheHits.incrementAndGet();
    }

    public void incrementMisses() {
        cacheMisses.incrementAndGet();
    }

    public void incrementInvalidations() {
        cacheInvalidations.incrementAndGet();
    }

    public void incrementEvictions() {
        cacheEvictions.incrementAndGet();
    }

    public double getHitRate() {
        long total = totalRequests.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) cacheHits.get() / total;
    }

    public double getMissRate() {
        long total = totalRequests.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) cacheMisses.get() / total;
    }

    public void reset() {
        totalRequests.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        cacheInvalidations.set(0);
        cacheEvictions.set(0);
    }

    @Override
    public String toString() {
        return String.format(
                "CacheStatistics{totalRequests=%d, hits=%d, misses=%d, hitRate=%.2f%%, missRate=%.2f%%, invalidations=%d, evictions=%d}",
                totalRequests.get(),
                cacheHits.get(),
                cacheMisses.get(),
                getHitRate() * 100,
                getMissRate() * 100,
                cacheInvalidations.get(),
                cacheEvictions.get()
        );
    }
}
