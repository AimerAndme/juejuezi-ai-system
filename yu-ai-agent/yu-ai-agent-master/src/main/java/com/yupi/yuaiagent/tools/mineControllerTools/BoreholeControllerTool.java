package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.Borehole;
import com.yupi.yuaiagent.service.IBoreholeService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//钻孔基本信息
@Component
@Slf4j
public class BoreholeControllerTool {

    private final IBoreholeService service;

    public BoreholeControllerTool(IBoreholeService service) {
        this.service = service;
    }

    @Tool(name = "addBorehole", description = "钻孔信息：通过钻孔编号,矿区编码,X坐标,Y坐标,Z坐标,总深度,钻探目的,钻探日期,状态增加新的钻孔信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "钻孔编号") String holeId,
            @ToolParam(description = "矿区编码") String areaId,
            @ToolParam(description = "X坐标") BigDecimal x,
            @ToolParam(description = "Y坐标") BigDecimal y,
            @ToolParam(description = "Z坐标") BigDecimal z,
            @ToolParam(description = "总深度") BigDecimal totalDepth,
            @ToolParam(description = "钻探目的") String drillPurpose,
            @ToolParam(description = "钻探日期") LocalDate drillDate,
            @ToolParam(description = "状态") String status
    ) {
        Borehole borehole = new Borehole();
        borehole.setHoleId(holeId);
        borehole.setAreaId(areaId);
        borehole.setX(x);
        borehole.setY(y);
        borehole.setZ(z);
        borehole.setTotalDepth(totalDepth);
        borehole.setDrillPurpose(drillPurpose);
        borehole.setDrillDate(drillDate);
        borehole.setStatus(status);
        try {
            log.info("Tool:添加钻孔信息：{}", borehole);
            return service.add(borehole);
        } catch (Exception e) {
            log.error("Tool:添加钻孔信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "getBoreholeById", description = "钻孔信息：通过钻孔编号查询钻孔信息，返回：钻孔信息")
    public Borehole getById(@ToolParam(description = "钻孔编号") String holeId) {
        try {
            log.info("Tool:查询钻孔信息：{}", holeId);
            Borehole data = service.getById(holeId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询钻孔信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getAllBoreholes", description = "钻孔信息：查询所有钻孔信息，返回：所有钻孔信息")
    public List<Borehole> getAll() {
        try {
            log.info("Tool:查询所有钻孔信息");
            List<Borehole> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有钻孔信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getBoreholesByAreaId", description = "钻孔信息：通过矿区编码查询钻孔信息，返回：钻孔信息列表")
    public List<Borehole> getByAreaId(@ToolParam(description = "矿区编码") String areaId) {
        try {
            log.info("Tool:通过矿区编码查询钻孔信息：{}", areaId);
            List<Borehole> list = service.getByAreaId(areaId);
            return list;
        } catch (Exception e) {
            log.error("Tool:通过矿区编码查询钻孔信息失败：{}", e.getMessage());
            return null;
        }
    }
}
