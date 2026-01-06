package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.ColumnMetadata;
import com.yupi.yuaiagent.service.IColumnMetadataService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

//字段元数据查询
@Component
@Slf4j
public class ColumnMetadataControllerTool {

    private final IColumnMetadataService service;

    public ColumnMetadataControllerTool(IColumnMetadataService service) {
        this.service = service;
    }

    @Tool(description = "字段元数据查询：通过表名和模式名查询数据库表的字段名和对应的注释信息")
    public List<ColumnMetadata> getColumnMetadata(
            @ToolParam(description = "表名") String tableName,
            @ToolParam(description = "模式名") String schemaName
    ) {
        try {
            log.info("Tool:查询表字段元数据，表名：{}，模式名：{}", tableName, schemaName);
            return service.getColumnMetadata(tableName, schemaName);
        } catch (Exception e) {
            log.error("Tool:查询表字段元数据失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "字段元数据查询：通过表名查询agent模式下的数据库表的字段名和对应的注释信息")
    public List<ColumnMetadata> getColumnMetadataByTableName(
            @ToolParam(description = "表名") String tableName
    ) {
        try {
            log.info("Tool:查询agent模式下表字段元数据，表名：{}", tableName);
            return service.getColumnMetadata(tableName, "agent");
        } catch (Exception e) {
            log.error("Tool:查询agent模式下表字段元数据失败：{}", e.getMessage());
            return null;
        }
    }
}
