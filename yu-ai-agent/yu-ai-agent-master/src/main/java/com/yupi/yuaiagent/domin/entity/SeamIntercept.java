package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 见煤记录
 */
@Data
public class SeamIntercept {

    /**
     * 记录ID
     */
    private Long interceptId;

    /**
     * 钻孔编号
     */
    private String holeId;

    /**
     * 煤层编号
     */
    private String seamId;

    /**
     * 起始深度
     */
    private BigDecimal fromDepth;

    /**
     * 终止深度
     */
    private BigDecimal toDepth;

    /**
     * 真厚度（m）
     */
    private BigDecimal thickness;
}
