package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.RoadwayPoint;
import com.yupi.yuaiagent.service.IRoadwayPointService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

//巷道中心线点
@Component
@Slf4j
public class RoadwayPointControllerTool {

    private final IRoadwayPointService service;

    public RoadwayPointControllerTool(IRoadwayPointService service) {
        this.service = service;
    }

    @Tool(name = "addRoadwayPoint", description = "巷道中心线点信息：通过点号,矿区编码,X坐标,Y坐标,Z坐标,点类型增加新的巷道中心线点信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "点号") String pointId,
            @ToolParam(description = "矿区编码") String areaId,
            @ToolParam(description = "X坐标") BigDecimal x,
            @ToolParam(description = "Y坐标") BigDecimal y,
            @ToolParam(description = "Z坐标") BigDecimal z,
            @ToolParam(description = "点类型") String pointType
    ) {
        RoadwayPoint roadwayPoint = new RoadwayPoint();
        roadwayPoint.setPointId(pointId);
        roadwayPoint.setAreaId(areaId);
        roadwayPoint.setX(x);
        roadwayPoint.setY(y);
        roadwayPoint.setZ(z);
        roadwayPoint.setPointType(pointType);
        try {
            log.info("Tool:添加巷道中心线点信息：{}", roadwayPoint);
            return service.add(roadwayPoint);
        } catch (Exception e) {
            log.error("Tool:添加巷道中心线点信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "getRoadwayPointById", description = "巷道中心线点信息：通过点号查询巷道中心线点信息，返回：巷道中心线点信息")
    public RoadwayPoint getById(@ToolParam(description = "点号") String pointId) {
        try {
            log.info("Tool:查询巷道中心线点信息：{}", pointId);
            RoadwayPoint data = service.getById(pointId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询巷道中心线点信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getAllRoadwayPoints", description = "巷道中心线点信息：查询所有巷道中心线点信息，返回：所有巷道中心线点信息")
    public List<RoadwayPoint> getAll() {
        try {
            log.info("Tool:查询所有巷道中心线点信息");
            List<RoadwayPoint> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有巷道中心线点信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getRoadwayPointsByAreaId", description = "巷道中心线点信息：通过矿区编码查询巷道中心线点信息，返回：巷道中心线点信息列表")
    public List<RoadwayPoint> getByAreaId(@ToolParam(description = "矿区编码") String areaId) {
        try {
            log.info("Tool:通过矿区编码查询巷道中心线点信息：{}", areaId);
            List<RoadwayPoint> list = service.getByAreaId(areaId);
            return list;
        } catch (Exception e) {
            log.error("Tool:通过矿区编码查询巷道中心线点信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "updateRoadwayPoint", description = "巷道中心线点信息：通过点号,矿区编码,X坐标,Y坐标,Z坐标,点类型更新巷道中心线点信息，返回：成功数量，-1表示失败！")
    public int update(
            @ToolParam(description = "点号") String pointId,
            @ToolParam(description = "矿区编码") String areaId,
            @ToolParam(description = "X坐标") BigDecimal x,
            @ToolParam(description = "Y坐标") BigDecimal y,
            @ToolParam(description = "Z坐标") BigDecimal z,
            @ToolParam(description = "点类型") String pointType
    ) {
        RoadwayPoint roadwayPoint = new RoadwayPoint();
        roadwayPoint.setPointId(pointId);
        roadwayPoint.setAreaId(areaId);
        roadwayPoint.setX(x);
        roadwayPoint.setY(y);
        roadwayPoint.setZ(z);
        roadwayPoint.setPointType(pointType);
        try {
            log.info("Tool:更新巷道中心线点信息：{}", roadwayPoint);
            return service.update(roadwayPoint);
        } catch (Exception e) {
            log.error("Tool:更新巷道中心线点信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "deleteRoadwayPoint", description = "巷道中心线点信息：通过点号删除巷道中心线点信息，返回：成功数量，-1表示失败！")
    public int delete(@ToolParam(description = "点号") String pointId) {
        try {
            log.info("Tool:删除巷道中心线点信息：{}", pointId);
            return service.delete(pointId);
        } catch (Exception e) {
            log.error("Tool:删除巷道中心线点信息失败：{}", e.getMessage());
            return -1;
        }
    }
}
