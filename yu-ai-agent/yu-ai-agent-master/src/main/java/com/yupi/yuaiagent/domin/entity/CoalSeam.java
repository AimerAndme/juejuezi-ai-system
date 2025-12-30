package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 煤层信息
 */
@Data
public class CoalSeam {

    /**
     * 煤层编号（如 3#）
     */
    private String seamId;

    /**
     * 煤层名称
     */
    private String seamName;

    /**
     * 平均厚度（m）
     */
    private BigDecimal averageThickness;

    /**
     * 平均倾角（°）
     */
    private BigDecimal dipAngle;

    /**
     * 顶板岩性
     */
    private String roofLithology;

    /**
     * 底板岩性
     */
    private String floorLithology;
}
