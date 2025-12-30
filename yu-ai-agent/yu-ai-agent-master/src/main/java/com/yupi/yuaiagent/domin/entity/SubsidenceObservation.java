package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 沉降观测记录
 */
@Data
public class SubsidenceObservation {

    /**
     * 观测记录ID
     */
    private Long obsId;

    /**
     * 站点编号
     */
    private String stationId;

    /**
     * 观测日期
     */
    private LocalDate obsDate;

    /**
     * 初始高程
     */
    private BigDecimal zInitial;

    /**
     * 当前高程
     */
    private BigDecimal zCurrent;

    /**
     * 沉降量（计算字段）
     */
    private BigDecimal subsidence;
}
