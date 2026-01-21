package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 钻孔基本信息
 */
@Data
public class Borehole {

    /**
     * 钻孔编号（如 ZK2025-001）
     */
    private String holeId;

    /**
     * 矿区编码
     */
    private String areaId;

    /**
     * X坐标
     */
    private BigDecimal x;

    /**
     * Y坐标
     */
    private BigDecimal y;

    /**
     * Z坐标
     */
    private BigDecimal z;

    /**
     * 总深度
     */
    private Double totalDepth;

    /**
     * 钻探目的
     */
    private String drillPurpose;

    /**
     * 钻探日期
     */
    private LocalDate drillDate;

    /**
     * 状态
     */
    private String status;
}
