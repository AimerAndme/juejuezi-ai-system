package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 岩性分层
 */
@Data
public class LithologyLog {

    /**
     * 记录ID
     */
    private Long logId;

    /**
     * 钻孔编号
     */
    private String holeId;

    /**
     * 起始深度
     */
    private BigDecimal fromDepth;

    /**
     * 终止深度
     */
    private BigDecimal toDepth;

    /**
     * 岩石类型
     */
    private String rockType;
}
