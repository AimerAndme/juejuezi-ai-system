package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.ThreeQuantities;
import com.yupi.yuaiagent.service.IThreeQuantitiesService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//"三量"动态台账
@Component
@Slf4j
public class ThreeQuantitiesControllerTool {

    private final IThreeQuantitiesService service;

    public ThreeQuantitiesControllerTool(IThreeQuantitiesService service) {
        this.service = service;
    }

    @Tool(description = "三量动态台账：通过矿区编码,计算日期,开拓煤量,准备煤量,回采煤量增加新的三量动态台账信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "矿区编码") String areaId,
            @ToolParam(description = "计算日期") LocalDate calcDate,
            @ToolParam(description = "开拓煤量") BigDecimal developmentReserve,
            @ToolParam(description = "准备煤量") BigDecimal preparationReserve,
            @ToolParam(description = "回采煤量") BigDecimal miningReserve
    ) {
        ThreeQuantities threeQuantities = new ThreeQuantities();
        threeQuantities.setAreaId(areaId);
        threeQuantities.setCalcDate(calcDate);
        threeQuantities.setDevelopmentReserve(developmentReserve);
        threeQuantities.setPreparationReserve(preparationReserve);
        threeQuantities.setMiningReserve(miningReserve);
        try {
            log.info("Tool:添加三量动态台账信息：{}", threeQuantities);
            return service.add(threeQuantities);
        } catch (Exception e) {
            log.error("Tool:添加三量动态台账信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(description = "三量动态台账：通过记录ID查询三量动态台账信息，返回：三量动态台账信息")
    public ThreeQuantities getById(@ToolParam(description = "记录ID") Long recordId) {
        try {
            log.info("Tool:查询三量动态台账信息：{}", recordId);
            ThreeQuantities data = service.getById(recordId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询三量动态台账信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "三量动态台账：查询所有三量动态台账信息，返回：所有三量动态台账信息")
    public List<ThreeQuantities> getAll() {
        try {
            log.info("Tool:查询所有三量动态台账信息");
            List<ThreeQuantities> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有三量动态台账信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "三量动态台账：通过矿区编码查询三量动态台账信息，返回：三量动态台账信息列表")
    public List<ThreeQuantities> getByAreaId(@ToolParam(description = "矿区编码") String areaId) {
        try {
            log.info("Tool:按矿区查询三量动态台账信息：{}", areaId);
            List<ThreeQuantities> list = service.getByAreaId(areaId);
            return list;
        } catch (Exception e) {
            log.error("Tool:按矿区查询三量动态台账信息失败：{}", e.getMessage());
            return null;
        }
    }
}
