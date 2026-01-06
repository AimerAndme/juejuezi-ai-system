package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.Roadway;
import com.yupi.yuaiagent.service.IRoadwayService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

//巷道基本信息
@Component
@Slf4j
public class RoadwayControllerTool {

    private final IRoadwayService service;

    public RoadwayControllerTool(IRoadwayService service) {
        this.service = service;
    }

    @Tool(name = "addRoadway", description = "巷道基本信息：通过巷道编号,矿区编码,巷道名称,起点编号,终点编号,巷道类型,断面面积,状态增加新的巷道基本信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "巷道编号") String roadwayId,
            @ToolParam(description = "矿区编码") String areaId,
            @ToolParam(description = "巷道名称") String name,
            @ToolParam(description = "起点编号") String startPoint,
            @ToolParam(description = "终点编号") String endPoint,
            @ToolParam(description = "巷道类型") String roadwayType,
            @ToolParam(description = "断面面积") BigDecimal crossSection,
            @ToolParam(description = "状态") String status
    ) {
        Roadway roadway = new Roadway();
        roadway.setRoadwayId(roadwayId);
        roadway.setAreaId(areaId);
        roadway.setName(name);
        roadway.setStartPoint(startPoint);
        roadway.setEndPoint(endPoint);
        roadway.setRoadwayType(roadwayType);
        roadway.setCrossSection(crossSection);
        roadway.setStatus(status);
        try {
            log.info("Tool:添加巷道基本信息：{}", roadway);
            return service.add(roadway);
        } catch (Exception e) {
            log.error("Tool:添加巷道基本信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "getRoadwayById", description = "巷道基本信息：通过巷道编号查询巷道基本信息，返回：巷道基本信息")
    public Roadway getById(@ToolParam(description = "巷道编号") String roadwayId) {
        try {
            log.info("Tool:查询巷道基本信息：{}", roadwayId);
            Roadway data = service.getById(roadwayId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询巷道基本信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getAllRoadways", description = "巷道基本信息：查询所有巷道基本信息，返回：所有巷道基本信息")
    public List<Roadway> getAll() {
        try {
            log.info("Tool:查询所有巷道基本信息");
            List<Roadway> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有巷道基本信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getRoadwaysByAreaId", description = "巷道基本信息：通过矿区编码查询巷道基本信息，返回：巷道基本信息列表")
    public List<Roadway> getByAreaId(@ToolParam(description = "矿区编码") String areaId) {
        try {
            log.info("Tool:按矿区查询巷道基本信息：{}", areaId);
            List<Roadway> list = service.getByAreaId(areaId);
            return list;
        } catch (Exception e) {
            log.error("Tool:按矿区查询巷道基本信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "updateRoadway", description = "巷道基本信息：通过巷道编号,矿区编码,巷道名称,起点编号,终点编号,巷道类型,断面面积,状态更新巷道基本信息，返回：成功数量，-1表示失败！")
    public int update(
            @ToolParam(description = "巷道编号") String roadwayId,
            @ToolParam(description = "矿区编码") String areaId,
            @ToolParam(description = "巷道名称") String name,
            @ToolParam(description = "起点编号") String startPoint,
            @ToolParam(description = "终点编号") String endPoint,
            @ToolParam(description = "巷道类型") String roadwayType,
            @ToolParam(description = "断面面积") BigDecimal crossSection,
            @ToolParam(description = "状态") String status
    ) {
        Roadway roadway = new Roadway();
        roadway.setRoadwayId(roadwayId);
        roadway.setAreaId(areaId);
        roadway.setName(name);
        roadway.setStartPoint(startPoint);
        roadway.setEndPoint(endPoint);
        roadway.setRoadwayType(roadwayType);
        roadway.setCrossSection(crossSection);
        roadway.setStatus(status);
        try {
            log.info("Tool:更新巷道基本信息：{}", roadway);
            return service.update(roadway);
        } catch (Exception e) {
            log.error("Tool:更新巷道基本信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "deleteRoadway", description = "巷道基本信息：通过巷道编号删除巷道基本信息，返回：成功数量，-1表示失败！")
    public int delete(@ToolParam(description = "巷道编号") String roadwayId) {
        try {
            log.info("Tool:删除巷道基本信息：{}", roadwayId);
            return service.delete(roadwayId);
        } catch (Exception e) {
            log.error("Tool:删除巷道基本信息失败：{}", e.getMessage());
            return -1;
        }
    }
}
