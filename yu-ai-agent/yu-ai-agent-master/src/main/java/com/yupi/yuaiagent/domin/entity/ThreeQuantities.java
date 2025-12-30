package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * "三量"动态台账
 */
@Data
public class ThreeQuantities {

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 矿区编码
     */
    private String areaId;

    /**
     * 计算日期
     */
    private LocalDate calcDate;

    /**
     * 开拓煤量
     */
    private BigDecimal developmentReserve;

    /**
     * 准备煤量
     */
    private BigDecimal preparationReserve;

    /**
     * 回采煤量
     */
    private BigDecimal miningReserve;
}
