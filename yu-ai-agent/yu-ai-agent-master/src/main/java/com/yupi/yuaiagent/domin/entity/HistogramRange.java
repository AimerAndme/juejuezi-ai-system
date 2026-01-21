package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
@Data
public class HistogramRange {

    private int index;
    private double leftEndpoint;
    private double rightEndpoint;
    private long count;
}

