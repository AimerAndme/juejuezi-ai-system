package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 巷道基本信息
 */
@Data
public class Roadway {

    /**
     * 巷道编号
     */
    private String roadwayId;

    /**
     * 矿区编码
     */
    private String areaId;

    /**
     * 巷道名称
     */
    private String name;

    /**
     * 起点编号
     */
    private String startPoint;

    /**
     * 终点编号
     */
    private String endPoint;

    /**
     * 巷道类型：开拓/准备/回采
     */
    private String roadwayType;

    /**
     * 断面面积
     */
    private BigDecimal crossSection;

    /**
     * 状态
     */
    private String status;
}
