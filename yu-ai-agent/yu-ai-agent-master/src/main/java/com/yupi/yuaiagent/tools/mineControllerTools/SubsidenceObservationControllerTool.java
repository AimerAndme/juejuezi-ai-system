package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.SubsidenceObservation;
import com.yupi.yuaiagent.service.ISubsidenceObservationService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//沉降观测记录
@Component
@Slf4j
public class SubsidenceObservationControllerTool {

    private final ISubsidenceObservationService service;

    public SubsidenceObservationControllerTool(ISubsidenceObservationService service) {
        this.service = service;
    }

    @Tool(description = "沉降观测记录：通过站点编号,观测日期,初始高程,当前高程增加新的沉降观测记录信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "站点编号") String stationId,
            @ToolParam(description = "观测日期") LocalDate obsDate,
            @ToolParam(description = "初始高程") BigDecimal zInitial,
            @ToolParam(description = "当前高程") BigDecimal zCurrent
    ) {
        SubsidenceObservation subsidenceObservation = new SubsidenceObservation();
        subsidenceObservation.setStationId(stationId);
        subsidenceObservation.setObsDate(obsDate);
        subsidenceObservation.setZInitial(zInitial);
        subsidenceObservation.setZCurrent(zCurrent);
        // 计算沉降量
        if (zInitial != null && zCurrent != null) {
            subsidenceObservation.setSubsidence(zInitial.subtract(zCurrent));
        }
        try {
            log.info("Tool:添加沉降观测记录信息：{}", subsidenceObservation);
            return service.add(subsidenceObservation);
        } catch (Exception e) {
            log.error("Tool:添加沉降观测记录信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(description = "沉降观测记录：通过观测记录ID查询沉降观测记录信息，返回：沉降观测记录信息")
    public SubsidenceObservation getById(@ToolParam(description = "观测记录ID") Long obsId) {
        try {
            log.info("Tool:查询沉降观测记录信息：{}", obsId);
            SubsidenceObservation data = service.getById(obsId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询沉降观测记录信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "沉降观测记录：查询所有沉降观测记录信息，返回：所有沉降观测记录信息")
    public List<SubsidenceObservation> getAll() {
        try {
            log.info("Tool:查询所有沉降观测记录信息");
            List<SubsidenceObservation> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有沉降观测记录信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "沉降观测记录：通过站点编号查询沉降观测记录信息，返回：沉降观测记录信息列表")
    public List<SubsidenceObservation> getByStationId(@ToolParam(description = "站点编号") String stationId) {
        try {
            log.info("Tool:按站点查询沉降观测记录信息：{}", stationId);
            List<SubsidenceObservation> list = service.getByStationId(stationId);
            return list;
        } catch (Exception e) {
            log.error("Tool:按站点查询沉降观测记录信息失败：{}", e.getMessage());
            return null;
        }
    }
}
