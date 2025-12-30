package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 巷道中心线点
 */
@Data
public class RoadwayPoint {

    /**
     * 点号（如 A101）
     */
    private String pointId;

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
     * 点类型
     */
    private String pointType;
}
