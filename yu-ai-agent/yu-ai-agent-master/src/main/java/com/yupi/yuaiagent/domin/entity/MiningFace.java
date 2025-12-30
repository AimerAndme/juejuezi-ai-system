package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采煤工作面
 */
@Data
public class MiningFace {

    /**
     * 工作面编号
     */
    private String faceId;

    /**
     * 矿区编码
     */
    private String areaId;

    /**
     * 煤层编号
     */
    private String seamId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 长度
     */
    private BigDecimal length;

    /**
     * 状态
     */
    private String status;
}
