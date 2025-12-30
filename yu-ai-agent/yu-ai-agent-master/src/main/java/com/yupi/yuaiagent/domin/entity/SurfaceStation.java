package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 观测站
 */
@Data
public class SurfaceStation {

    /**
     * 站点编号
     */
    private String stationId;

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
     * 初始高程
     */
    private BigDecimal zInitial;
}
