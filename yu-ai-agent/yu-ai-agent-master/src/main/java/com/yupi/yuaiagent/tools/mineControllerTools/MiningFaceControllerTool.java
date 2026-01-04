package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.MiningFace;
import com.yupi.yuaiagent.service.IMiningFaceService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//采煤工作面
@Component
@Slf4j
public class MiningFaceControllerTool {

    private final IMiningFaceService service;

    public MiningFaceControllerTool(IMiningFaceService service) {
        this.service = service;
    }

    @Tool(description = "采煤工作面信息：通过工作面编号,矿区编码,煤层编号,开始日期,结束日期,长度,状态增加新的采煤工作面信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "工作面编号") String faceId,
            @ToolParam(description = "矿区编码") String areaId,
            @ToolParam(description = "煤层编号") String seamId,
            @ToolParam(description = "开始日期") LocalDate startDate,
            @ToolParam(description = "结束日期") LocalDate endDate,
            @ToolParam(description = "长度") BigDecimal length,
            @ToolParam(description = "状态") String status
    ) {
        MiningFace miningFace = new MiningFace();
        miningFace.setFaceId(faceId);
        miningFace.setAreaId(areaId);
        miningFace.setSeamId(seamId);
        miningFace.setStartDate(startDate);
        miningFace.setEndDate(endDate);
        miningFace.setLength(length);
        miningFace.setStatus(status);
        try {
            log.info("Tool:添加采煤工作面信息：{}", miningFace);
            return service.add(miningFace);
        } catch (Exception e) {
            log.error("Tool:添加采煤工作面信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(description = "采煤工作面信息：通过工作面编号查询采煤工作面信息，返回：采煤工作面信息")
    public MiningFace getById(@ToolParam(description = "工作面编号") String faceId) {
        try {
            log.info("Tool:查询采煤工作面信息：{}", faceId);
            MiningFace data = service.getById(faceId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询采煤工作面信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "采煤工作面信息：查询所有采煤工作面信息，返回：所有采煤工作面信息")
    public List<MiningFace> getAll() {
        try {
            log.info("Tool:查询所有采煤工作面信息");
            List<MiningFace> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有采煤工作面信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(description = "采煤工作面信息：通过矿区编码查询采煤工作面信息，返回：采煤工作面信息列表")
    public List<MiningFace> getByAreaId(@ToolParam(description = "矿区编码") String areaId) {
        try {
            log.info("Tool:通过矿区编码查询采煤工作面信息：{}", areaId);
            List<MiningFace> list = service.getByAreaId(areaId);
            return list;
        } catch (Exception e) {
            log.error("Tool:通过矿区编码查询采煤工作面信息失败：{}", e.getMessage());
            return null;
        }
    }
}
