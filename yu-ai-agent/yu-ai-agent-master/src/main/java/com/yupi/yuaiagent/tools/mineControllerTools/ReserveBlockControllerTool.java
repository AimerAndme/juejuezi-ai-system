package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.ReserveBlock;
import com.yupi.yuaiagent.service.IReserveBlockService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

//资源储量块段
@Component
@Slf4j
public class ReserveBlockControllerTool {

    private final IReserveBlockService service;

    public ReserveBlockControllerTool(IReserveBlockService service) {
        this.service = service;
    }

    @Tool(name = "addReserveBlock", description = "资源储量块段信息：通过块段编号,矿区编码,煤层编号,块段类型,地质储量,可采储量,设计回采率增加新的资源储量块段信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "块段编号") String blockId,
            @ToolParam(description = "矿区编码") String areaId,
            @ToolParam(description = "煤层编号") String seamId,
            @ToolParam(description = "块段类型") String blockType,
            @ToolParam(description = "地质储量") BigDecimal geologicalReserve,
            @ToolParam(description = "可采储量") BigDecimal recoverableReserve,
            @ToolParam(description = "设计回采率") BigDecimal recoveryRate
    ) {
        ReserveBlock reserveBlock = new ReserveBlock();
        reserveBlock.setBlockId(blockId);
        reserveBlock.setAreaId(areaId);
        reserveBlock.setSeamId(seamId);
        reserveBlock.setBlockType(blockType);
        reserveBlock.setGeologicalReserve(geologicalReserve);
        reserveBlock.setRecoverableReserve(recoverableReserve);
        reserveBlock.setRecoveryRate(recoveryRate);
        try {
            log.info("Tool:添加资源储量块段信息：{}", reserveBlock);
            return service.add(reserveBlock);
        } catch (Exception e) {
            log.error("Tool:添加资源储量块段信息失败：{}", e.getMessage());
            return -1;
        }
    }


    @Tool(name = "getReserveBlockById", description = "资源储量块段信息：通过块段编号查询资源储量块段信息，返回：资源储量块段信息")
    public ReserveBlock getById(@ToolParam(description = "块段编号") String blockId) {
        try {
            log.info("Tool:查询资源储量块段信息：{}", blockId);
            ReserveBlock data = service.getById(blockId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询资源储量块段信息失败：{}", e.getMessage());
            return null;
        }
    }


    @Tool(name = "getAllReserveBlocks", description = "资源储量块段信息：查询所有资源储量块段信息，返回：所有资源储量块段信息")
    public List<ReserveBlock> getAll() {
        try {
            log.info("Tool:查询所有资源储量块段信息");
            List<ReserveBlock> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有资源储量块段信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getReserveBlocksByAreaId", description = "资源储量块段信息：通过矿区编码查询资源储量块段信息，返回：资源储量块段信息列表")
    public List<ReserveBlock> getByAreaId(@ToolParam(description = "矿区编码") String areaId) {
        try {
            log.info("Tool:通过矿区编码查询资源储量块段信息：{}", areaId);
            List<ReserveBlock> list = service.getByAreaId(areaId);
            return list;
        } catch (Exception e) {
            log.error("Tool:通过矿区编码查询资源储量块段信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "updateReserveBlock", description = "资源储量块段信息：通过块段编号,矿区编码,煤层编号,块段类型,地质储量,可采储量,设计回采率更新资源储量块段信息，返回：成功数量，-1表示失败！")
    public int update(
            @ToolParam(description = "块段编号") String blockId,
            @ToolParam(description = "矿区编码") String areaId,
            @ToolParam(description = "煤层编号") String seamId,
            @ToolParam(description = "块段类型") String blockType,
            @ToolParam(description = "地质储量") BigDecimal geologicalReserve,
            @ToolParam(description = "可采储量") BigDecimal recoverableReserve,
            @ToolParam(description = "设计回采率") BigDecimal recoveryRate
    ) {
        ReserveBlock reserveBlock = new ReserveBlock();
        reserveBlock.setBlockId(blockId);
        reserveBlock.setAreaId(areaId);
        reserveBlock.setSeamId(seamId);
        reserveBlock.setBlockType(blockType);
        reserveBlock.setGeologicalReserve(geologicalReserve);
        reserveBlock.setRecoverableReserve(recoverableReserve);
        reserveBlock.setRecoveryRate(recoveryRate);
        try {
            log.info("Tool:更新资源储量块段信息：{}", reserveBlock);
            return service.update(reserveBlock);
        } catch (Exception e) {
            log.error("Tool:更新资源储量块段信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "deleteReserveBlock", description = "资源储量块段信息：通过块段编号删除资源储量块段信息，返回：成功数量，-1表示失败！")
    public int delete(@ToolParam(description = "块段编号") String blockId) {
        try {
            log.info("Tool:删除资源储量块段信息：{}", blockId);
            return service.delete(blockId);
        } catch (Exception e) {
            log.error("Tool:删除资源储量块段信息失败：{}", e.getMessage());
            return -1;
        }
    }
}