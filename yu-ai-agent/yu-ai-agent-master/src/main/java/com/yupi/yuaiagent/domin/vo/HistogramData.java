package com.yupi.yuaiagent.domin.vo;

import com.yupi.yuaiagent.domin.entity.HistogramInterval;
import com.yupi.yuaiagent.domin.entity.HistogramRange;
import lombok.Data;

import java.util.List;

@Data
public class HistogramData {
    private List<HistogramRange> interval;
}
