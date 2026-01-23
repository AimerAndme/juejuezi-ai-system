package com.yupi.yuaiagent.service.quota;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitResult {
    private boolean allowed;
    private long remainingTokens;
    private long resetTime;
    private long windowStart;
    private long windowEnd;
    private int usagePercent;
    private LimitReason limitReason;

    public enum LimitReason {
        NORMAL(0, "正常"),
        THRESHOLD_REACHED(1, "达到阈值百分比"),
        LIMIT_EXCEEDED(2, "超过配额上限");

        private final int code;
        private final String desc;

        LimitReason(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static LimitReason fromCode(int code) {
            return Arrays.stream(values())
                    .filter(r -> r.code == code)
                    .findFirst()
                    .orElse(NORMAL);
        }
    }
}