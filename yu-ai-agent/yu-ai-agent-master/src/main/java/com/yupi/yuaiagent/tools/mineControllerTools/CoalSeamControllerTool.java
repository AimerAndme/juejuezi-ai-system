package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.CoalSeam;
import com.yupi.yuaiagent.domin.entity.ColumnMetadata;
import com.yupi.yuaiagent.service.ICoalSeamService;
import com.yupi.yuaiagent.service.IColumnMetadataService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

//煤层信息
@Component
@Slf4j
public class CoalSeamControllerTool {

    private final ICoalSeamService service;
    private final IColumnMetadataService columnMetadataService;

    public CoalSeamControllerTool(ICoalSeamService service, IColumnMetadataService columnMetadataService) {
        this.service = service;
        this.columnMetadataService = columnMetadataService;
    }

    @Tool(name = "addCoalSeam", description = "煤层信息：通过煤层编号,煤层名称,平均厚度,平均倾角,顶板岩性,底板岩性增加新的煤层信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "煤层编号") String seamId,
            @ToolParam(description = "煤层名称") String seamName,
            @ToolParam(description = "平均厚度") BigDecimal averageThickness,
            @ToolParam(description = "平均倾角") BigDecimal dipAngle,
            @ToolParam(description = "顶板岩性") String roofLithology,
            @ToolParam(description = "底板岩性") String floorLithology
    ) {
        CoalSeam coalSeam = new CoalSeam();
        coalSeam.setSeamId(seamId);
        coalSeam.setSeamName(seamName);
        coalSeam.setAverageThickness(averageThickness);
        coalSeam.setDipAngle(dipAngle);
        coalSeam.setRoofLithology(roofLithology);
        coalSeam.setFloorLithology(floorLithology);
        try {
            log.info("Tool:添加煤层信息：{}", coalSeam);
            return service.add(coalSeam);
        } catch (Exception e) {
            log.error("Tool:添加煤层信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "getCoalSeamById", description = "煤层信息：通过煤层编号查询煤层信息，返回：煤层信息")
    public CoalSeam getById(@ToolParam(description = "煤层编号") String seamId) {
        try {
            log.info("Tool:查询煤层信息：{}", seamId);
            CoalSeam data = service.getById(seamId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询煤层信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getAllCoalSeams", description = "煤层信息：查询所有煤层信息，返回：所有煤层信息")
    public List<CoalSeam> getAll() {
        try {
            log.info("Tool:查询所有煤层信息");
            List<CoalSeam> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有煤层信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "煤层信息：查询煤层表的字段名和对应的注释信息")
    public List<ColumnMetadata> getCoalSeamColumnMetadata() {
        try {
            log.info("Tool:查询煤层表的字段元数据");
            return columnMetadataService.getColumnMetadata("coal_seam", "agent");
        } catch (Exception e) {
            log.error("Tool:查询煤层表的字段元数据失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "updateCoalSeam", description = "煤层信息：通过煤层编号,煤层名称,平均厚度,平均倾角,顶板岩性,底板岩性更新煤层信息，返回：成功数量，-1表示失败！")
    public int update(
            @ToolParam(description = "煤层编号") String seamId,
            @ToolParam(description = "煤层名称") String seamName,
            @ToolParam(description = "平均厚度") BigDecimal averageThickness,
            @ToolParam(description = "平均倾角") BigDecimal dipAngle,
            @ToolParam(description = "顶板岩性") String roofLithology,
            @ToolParam(description = "底板岩性") String floorLithology
    ) {
        CoalSeam coalSeam = new CoalSeam();
        coalSeam.setSeamId(seamId);
        coalSeam.setSeamName(seamName);
        coalSeam.setAverageThickness(averageThickness);
        coalSeam.setDipAngle(dipAngle);
        coalSeam.setRoofLithology(roofLithology);
        coalSeam.setFloorLithology(floorLithology);
        try {
            log.info("Tool:更新煤层信息：{}", coalSeam);
            return service.update(coalSeam);
        } catch (Exception e) {
            log.error("Tool:更新煤层信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "deleteCoalSeam", description = "煤层信息：通过煤层编号删除煤层信息，返回：成功数量，-1表示失败！")
    public int delete(@ToolParam(description = "煤层编号") String seamId) {
        try {
            log.info("Tool:删除煤层信息：{}", seamId);
            return service.delete(seamId);
        } catch (Exception e) {
            log.error("Tool:删除煤层信息失败：{}", e.getMessage());
            return -1;
        }
    }
}
