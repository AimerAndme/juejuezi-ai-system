package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.ColumnMetadata;
import java.util.List;

public interface IColumnMetadataService {

    /**
     * 查询指定表的字段元数据
     * @param tableName 表名
     * @param schemaName 模式名
     * @return 字段元数据列表
     */
    List<ColumnMetadata> getColumnMetadata(String tableName, String schemaName);
}
