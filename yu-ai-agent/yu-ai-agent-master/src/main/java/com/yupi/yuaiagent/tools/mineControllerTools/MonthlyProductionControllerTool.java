package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.MonthlyProduction;
import com.yupi.yuaiagent.service.IMonthlyProductionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//月度采出量统计
@Component
@Slf4j
public class MonthlyProductionControllerTool {

    private final IMonthlyProductionService service;

    public MonthlyProductionControllerTool(IMonthlyProductionService service) {
        this.service = service;
    }

    @Tool(name = "addMonthlyProduction", description = "月度采出量统计：通过块段编号,报告月份,采出量,损失量,实际回采率增加新的月度采出量统计信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "块段编号") String blockId,
            @ToolParam(description = "报告月份") LocalDate reportMonth,
            @ToolParam(description = "采出量") BigDecimal minedTonnage,
            @ToolParam(description = "损失量") BigDecimal lossTonnage,
            @ToolParam(description = "实际回采率") BigDecimal actualRecoveryRate
    ) {
        MonthlyProduction monthlyProduction = new MonthlyProduction();
        monthlyProduction.setBlockId(blockId);
        monthlyProduction.setReportMonth(reportMonth);
        monthlyProduction.setMinedTonnage(minedTonnage);
        monthlyProduction.setLossTonnage(lossTonnage);
        monthlyProduction.setActualRecoveryRate(actualRecoveryRate);
        try {
            log.info("Tool:添加月度采出量统计信息：{}", monthlyProduction);
            return service.add(monthlyProduction);
        } catch (Exception e) {
            log.error("Tool:添加月度采出量统计信息失败：{}", e.getMessage());
            return -1;
        }
    }


    @Tool(name = "getMonthlyProductionById", description = "月度采出量统计：通过记录ID查询月度采出量统计信息，返回：月度采出量统计信息")
    public MonthlyProduction getById(@ToolParam(description = "记录ID") Long recordId) {
        try {
            log.info("Tool:查询月度采出量统计信息：{}", recordId);
            MonthlyProduction data = service.getById(recordId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询月度采出量统计信息失败：{}", e.getMessage());
            return null;
        }
    }


    @Tool(name = "getAllMonthlyProductions", description = "月度采出量统计：查询所有月度采出量统计信息，返回：所有月度采出量统计信息")
    public List<MonthlyProduction> getAll() {
        try {
            log.info("Tool:查询所有月度采出量统计信息");
            List<MonthlyProduction> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有月度采出量统计信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getMonthlyProductionsByBlockId", description = "月度采出量统计：通过块段编号查询月度采出量统计信息，返回：月度采出量统计信息列表")
    public List<MonthlyProduction> getByBlockId(@ToolParam(description = "块段编号") String blockId) {
        try {
            log.info("Tool:通过块段编号查询月度采出量统计信息：{}", blockId);
            List<MonthlyProduction> list = service.getByBlockId(blockId);
            return list;
        } catch (Exception e) {
            log.error("Tool:通过块段编号查询月度采出量统计信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "updateMonthlyProduction", description = "月度采出量统计：通过记录ID,块段编号,报告月份,采出量,损失量,实际回采率更新月度采出量统计信息，返回：成功数量，-1表示失败！")
    public int update(
            @ToolParam(description = "记录ID") Long recordId,
            @ToolParam(description = "块段编号") String blockId,
            @ToolParam(description = "报告月份") LocalDate reportMonth,
            @ToolParam(description = "采出量") BigDecimal minedTonnage,
            @ToolParam(description = "损失量") BigDecimal lossTonnage,
            @ToolParam(description = "实际回采率") BigDecimal actualRecoveryRate
    ) {
        MonthlyProduction monthlyProduction = new MonthlyProduction();
        monthlyProduction.setRecordId(recordId);
        monthlyProduction.setBlockId(blockId);
        monthlyProduction.setReportMonth(reportMonth);
        monthlyProduction.setMinedTonnage(minedTonnage);
        monthlyProduction.setLossTonnage(lossTonnage);
        monthlyProduction.setActualRecoveryRate(actualRecoveryRate);
        try {
            log.info("Tool:更新月度采出量统计信息：{}", monthlyProduction);
            return service.update(monthlyProduction);
        } catch (Exception e) {
            log.error("Tool:更新月度采出量统计信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "deleteMonthlyProduction", description = "月度采出量统计：通过记录ID删除月度采出量统计信息，返回：成功数量，-1表示失败！")
    public int delete(@ToolParam(description = "记录ID") Long recordId) {
        try {
            log.info("Tool:删除月度采出量统计信息：{}", recordId);
            return service.delete(recordId);
        } catch (Exception e) {
            log.error("Tool:删除月度采出量统计信息失败：{}", e.getMessage());
            return -1;
        }
    }
}