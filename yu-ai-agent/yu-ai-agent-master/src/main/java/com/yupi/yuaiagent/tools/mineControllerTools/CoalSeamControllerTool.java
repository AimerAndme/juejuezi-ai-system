package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.CoalSeam;
import com.yupi.yuaiagent.service.ICoalSeamService;
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

    public CoalSeamControllerTool(ICoalSeamService service) {
        this.service = service;
    }

    @Tool(description = "煤层信息：通过煤层编号,煤层名称,平均厚度,平均倾角,顶板岩性,底板岩性增加新的煤层信息，返回：成功数量，-1表示失败！")
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


    @Tool(description = "煤层信息：通过煤层编号查询煤层信息，返回：煤层信息")
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


    @Tool(description = "煤层信息：查询所有煤层信息，返回：所有煤层信息")
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
}