package com.yupi.yuaiagent.domin.entity;

import lombok.Data;

@Data
public class HistogramInterval {
    private int index;
    private double minValue;
    private double maxValue;
    private long count;
}
