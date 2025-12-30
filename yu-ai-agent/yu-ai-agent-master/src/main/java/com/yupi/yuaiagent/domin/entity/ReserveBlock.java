package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 资源储量块段
 */
@Data
public class ReserveBlock {

    /**
     * 块段编号
     */
    private String blockId;

    /**
     * 矿区编码
     */
    private String areaId;

    /**
     * 煤层编号
     */
    private String seamId;

    /**
     * 块段类型：采区/工作面
     */
    private String blockType;

    /**
     * 地质储量
     */
    private BigDecimal geologicalReserve;

    /**
     * 可采储量
     */
    private BigDecimal recoverableReserve;

    /**
     * 设计回采率（%）
     */
    private BigDecimal recoveryRate;
}
