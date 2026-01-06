package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.LithologyLog;
import com.yupi.yuaiagent.service.ILithologyLogService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

//岩性分层
@Component
@Slf4j
public class LithologyLogControllerTool {

    private final ILithologyLogService service;

    public LithologyLogControllerTool(ILithologyLogService service) {
        this.service = service;
    }

    @Tool(name = "addLithologyLog", description = "岩性分层信息：通过钻孔编号,起始深度,终止深度,岩石类型增加新的岩性分层信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "钻孔编号") String holeId,
            @ToolParam(description = "起始深度") BigDecimal fromDepth,
            @ToolParam(description = "终止深度") BigDecimal toDepth,
            @ToolParam(description = "岩石类型") String rockType
    ) {
        LithologyLog lithologyLog = new LithologyLog();
        lithologyLog.setHoleId(holeId);
        lithologyLog.setFromDepth(fromDepth);
        lithologyLog.setToDepth(toDepth);
        lithologyLog.setRockType(rockType);
        try {
            log.info("Tool:添加岩性分层信息：{}", lithologyLog);
            return service.add(lithologyLog);
        } catch (Exception e) {
            log.error("Tool:添加岩性分层信息失败：{}", e.getMessage());
            return -1;
        }
    }


    @Tool(name = "getLithologyLogById", description = "岩性分层信息：通过记录ID查询岩性分层信息，返回：岩性分层信息")
    public LithologyLog getById(@ToolParam(description = "记录ID") Long logId) {
        try {
            log.info("Tool:查询岩性分层信息：{}", logId);
            LithologyLog data = service.getById(logId);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询岩性分层信息失败：{}", e.getMessage());
            return null;
        }
    }


    @Tool(name = "getAllLithologyLogs", description = "岩性分层信息：查询所有岩性分层信息，返回：所有岩性分层信息")
    public List<LithologyLog> getAll() {
        try {
            log.info("Tool:查询所有岩性分层信息");
            List<LithologyLog> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有岩性分层信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getLithologyLogsByHoleId", description = "岩性分层信息：通过钻孔编号查询岩性分层信息，返回：岩性分层信息列表")
    public List<LithologyLog> getByHoleId(@ToolParam(description = "钻孔编号") String holeId) {
        try {
            log.info("Tool:通过钻孔编号查询岩性分层信息：{}", holeId);
            List<LithologyLog> list = service.getByHoleId(holeId);
            return list;
        } catch (Exception e) {
            log.error("Tool:通过钻孔编号查询岩性分层信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "updateLithologyLog", description = "岩性分层信息：通过记录ID,钻孔编号,起始深度,终止深度,岩石类型更新岩性分层信息，返回：成功数量，-1表示失败！")
    public int update(
            @ToolParam(description = "记录ID") Long logId,
            @ToolParam(description = "钻孔编号") String holeId,
            @ToolParam(description = "起始深度") BigDecimal fromDepth,
            @ToolParam(description = "终止深度") BigDecimal toDepth,
            @ToolParam(description = "岩石类型") String rockType
    ) {
        LithologyLog lithologyLog = new LithologyLog();
        lithologyLog.setLogId(logId);
        lithologyLog.setHoleId(holeId);
        lithologyLog.setFromDepth(fromDepth);
        lithologyLog.setToDepth(toDepth);
        lithologyLog.setRockType(rockType);
        try {
            log.info("Tool:更新岩性分层信息：{}", lithologyLog);
            return service.update(lithologyLog);
        } catch (Exception e) {
            log.error("Tool:更新岩性分层信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "deleteLithologyLog", description = "岩性分层信息：通过记录ID删除岩性分层信息，返回：成功数量，-1表示失败！")
    public int delete(@ToolParam(description = "记录ID") Long logId) {
        try {
            log.info("Tool:删除岩性分层信息：{}", logId);
            return service.delete(logId);
        } catch (Exception e) {
            log.error("Tool:删除岩性分层信息失败：{}", e.getMessage());
            return -1;
        }
    }
}