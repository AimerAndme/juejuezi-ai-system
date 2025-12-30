package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 矿区基本信息
 */
@Data
public class MineArea {

    /**
     * 矿区编码（如 M01）
     */
    private String areaId;

    /**
     * 矿区名称
     */
    private String areaName;

    /**
     * 坐标系
     */
    private String coordinateSystem;

    /**
     * 高程基准（m）
     */
    private BigDecimal datumHeight;
}
