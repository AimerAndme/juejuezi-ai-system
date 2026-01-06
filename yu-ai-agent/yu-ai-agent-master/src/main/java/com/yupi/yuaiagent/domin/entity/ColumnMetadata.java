package com.yupi.yuaiagent.domin.entity;

import lombok.Data;

/**
 * 表字段元数据
 */
@Data
public class ColumnMetadata {

    /**
     * 字段名
     */
    private String columnName;

    /**
     * 字段注释
     */
    private String columnComment;
}
