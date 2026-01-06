package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.SeamIntercept;
import com.yupi.yuaiagent.service.ISeamInterceptService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

//见煤记录
@Component
@Slf4j
public class SeamInterceptControllerTool {

    private final ISeamInterceptService service;

    public SeamInterceptControllerTool(ISeamInterceptService service) {
        this.service = service;
    }

    @Tool(name = "addSeamIntercept", description = "见煤记录：通过钻孔编号,煤层编号,起始深度,终止深度,真厚度增加新的见煤记录信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "钻孔编号") String holeId,
            @ToolParam(description = "煤层编号") String seamId,
            @ToolParam(description = "起始深度") BigDecimal fromDepth,
            @ToolParam(description = "终止深度") BigDecimal toDepth,
            @ToolParam(description = "真厚度(m)") BigDecimal thickness
    ) {
        SeamIntercept seamIntercept = new SeamIntercept();
        seamIntercept.setHoleId(holeId);
        seamIntercept.setSeamId(seamId);
        seamIntercept.setFromDepth(fromDepth);
        seamIntercept.setToDepth(toDepth);
        seamIntercept.setThickness(thickness);
        try {
            log.info("Tool:添加见煤记录信息：{}", seamIntercept);
            return service.add(seamIntercept);
        } catch (Exception e) {
            log.error("Tool:添加见煤记录信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "getSeamInterceptById", description = "见煤记录：通过记录ID查询见煤记录信息，返回：见煤记录信息")
    public SeamIntercept getById(@ToolParam(description = "记录ID") Long interceptId) {
        try {
            log.info("Tool:查询见煤记录信息：{}", interceptId);
            SeamIntercept data = service.getById(interceptId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询见煤记录信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getAllSeamIntercepts", description = "见煤记录：查询所有见煤记录信息，返回：所有见煤记录信息")
    public List<SeamIntercept> getAll() {
        try {
            log.info("Tool:查询所有见煤记录信息");
            List<SeamIntercept> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有见煤记录信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getSeamInterceptsByHoleId", description = "见煤记录：通过钻孔编号查询见煤记录信息，返回：见煤记录信息列表")
    public List<SeamIntercept> getByHoleId(@ToolParam(description = "钻孔编号") String holeId) {
        try {
            log.info("Tool:按钻孔查询见煤记录信息：{}", holeId);
            List<SeamIntercept> list = service.getByHoleId(holeId);
            return list;
        } catch (Exception e) {
            log.error("Tool:按钻孔查询见煤记录信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "updateSeamIntercept", description = "见煤记录：通过记录ID,钻孔编号,煤层编号,起始深度,终止深度,真厚度更新见煤记录信息，返回：成功数量，-1表示失败！")
    public int update(
            @ToolParam(description = "记录ID") Long interceptId,
            @ToolParam(description = "钻孔编号") String holeId,
            @ToolParam(description = "煤层编号") String seamId,
            @ToolParam(description = "起始深度") BigDecimal fromDepth,
            @ToolParam(description = "终止深度") BigDecimal toDepth,
            @ToolParam(description = "真厚度(m)") BigDecimal thickness
    ) {
        SeamIntercept seamIntercept = new SeamIntercept();
        seamIntercept.setInterceptId(interceptId);
        seamIntercept.setHoleId(holeId);
        seamIntercept.setSeamId(seamId);
        seamIntercept.setFromDepth(fromDepth);
        seamIntercept.setToDepth(toDepth);
        seamIntercept.setThickness(thickness);
        try {
            log.info("Tool:更新见煤记录信息：{}", seamIntercept);
            return service.update(seamIntercept);
        } catch (Exception e) {
            log.error("Tool:更新见煤记录信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "deleteSeamIntercept", description = "见煤记录：通过记录ID删除见煤记录信息，返回：成功数量，-1表示失败！")
    public int delete(@ToolParam(description = "记录ID") Long interceptId) {
        try {
            log.info("Tool:删除见煤记录信息：{}", interceptId);
            return service.delete(interceptId);
        } catch (Exception e) {
            log.error("Tool:删除见煤记录信息失败：{}", e.getMessage());
            return -1;
        }
    }
}
