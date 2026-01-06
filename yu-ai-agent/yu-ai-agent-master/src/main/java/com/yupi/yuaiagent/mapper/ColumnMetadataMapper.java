package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.ColumnMetadata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ColumnMetadataMapper {

    /**
     * 查询指定表的字段元数据
     * @param tableName 表名
     * @param schemaName 模式名
     * @return 字段元数据列表
     */
    List<ColumnMetadata> selectColumnMetadata(@Param("tableName") String tableName, @Param("schemaName") String schemaName);
}
