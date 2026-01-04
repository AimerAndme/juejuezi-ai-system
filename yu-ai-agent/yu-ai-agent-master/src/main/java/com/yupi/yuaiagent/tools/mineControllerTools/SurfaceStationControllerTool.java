package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.SurfaceStation;
import com.yupi.yuaiagent.service.ISurfaceStationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

//观测站
@Component
@Slf4j
public class SurfaceStationControllerTool {

    private final ISurfaceStationService service;

    public SurfaceStationControllerTool(ISurfaceStationService service) {
        this.service = service;
    }

    @Tool(description = "观测站：通过站点编号,矿区编码,X坐标,Y坐标,初始高程增加新的观测站信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "站点编号") String stationId,
            @ToolParam(description = "矿区编码") String areaId,
            @ToolParam(description = "X坐标") BigDecimal x,
            @ToolParam(description = "Y坐标") BigDecimal y,
            @ToolParam(description = "初始高程") BigDecimal zInitial
    ) {
        SurfaceStation surfaceStation = new SurfaceStation();
        surfaceStation.setStationId(stationId);
        surfaceStation.setAreaId(areaId);
        surfaceStation.setX(x);
        surfaceStation.setY(y);
        surfaceStation.setZInitial(zInitial);
        try {
            log.info("Tool:添加观测站信息：{}", surfaceStation);
            return service.add(surfaceStation);
        } catch (Exception e) {
            log.error("Tool:添加观测站信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(description = "观测站：通过站点编号查询观测站信息，返回：观测站信息")
    public SurfaceStation getById(@ToolParam(description = "站点编号") String stationId) {
        try {
            log.info("Tool:查询观测站信息：{}", stationId);
            SurfaceStation data = service.getById(stationId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询观测站信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "观测站：查询所有观测站信息，返回：所有观测站信息")
    public List<SurfaceStation> getAll() {
        try {
            log.info("Tool:查询所有观测站信息");
            List<SurfaceStation> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有观测站信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "观测站：通过矿区编码查询观测站信息，返回：观测站信息列表")
    public List<SurfaceStation> getByAreaId(@ToolParam(description = "矿区编码") String areaId) {
        try {
            log.info("Tool:按矿区查询观测站信息：{}", areaId);
            List<SurfaceStation> list = service.getByAreaId(areaId);
            return list;
        } catch (Exception e) {
            log.error("Tool:按矿区查询观测站信息失败：{}", e.getMessage());
            return null;
        }
    }
}
