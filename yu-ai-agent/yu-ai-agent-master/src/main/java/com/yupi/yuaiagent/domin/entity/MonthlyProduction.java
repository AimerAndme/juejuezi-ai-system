package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 月度采出量统计
 */
@Data
public class MonthlyProduction {

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 块段编号
     */
    private String blockId;

    /**
     * 报告月份
     */
    private LocalDate reportMonth;

    /**
     * 采出量
     */
    private BigDecimal minedTonnage;

    /**
     * 损失量
     */
    private BigDecimal lossTonnage;

    /**
     * 实际回采率（%）
     */
    private BigDecimal actualRecoveryRate;
}
