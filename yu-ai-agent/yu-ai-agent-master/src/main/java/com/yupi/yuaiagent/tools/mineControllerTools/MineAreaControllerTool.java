package com.yupi.yuaiagent.tools.mineControllerTools;

import com.yupi.yuaiagent.domin.entity.MineArea;
import com.yupi.yuaiagent.service.IMineAreaService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

//矿区信息
@Component
@Slf4j
public class MineAreaControllerTool {

    private final IMineAreaService service;

    public MineAreaControllerTool(IMineAreaService service) {
        this.service = service;
    }

    @Tool(name = "addMineArea", description = "矿区信息：通过矿区id,矿区名称，矿区坐标系统，矿区高程基准，增加新的矿区信息，返回：成功数量，-1表示失败！")
    public int add(
            @ToolParam(description = "矿区id") String areaId,
            @ToolParam(description = "矿区名称") String areaName,
            @ToolParam(description = "坐标系") String coordinateSystem,
            @ToolParam(description = "高程基准") BigDecimal datumHeight
    ) {
        MineArea mineArea = new MineArea();
        mineArea.setAreaId(areaId);
        mineArea.setAreaName(areaName);
        mineArea.setCoordinateSystem(coordinateSystem);
        mineArea.setDatumHeight(datumHeight);
        try {
            log.info("Tool:添加矿区信息：{}", mineArea);
            return service.add(mineArea);
        } catch (Exception e) {
            log.error("Tool:添加矿区信息失败：{}", e.getMessage());
            return -1;
        }
    }

    @Tool(name = "getMineAreaById", description = "矿区信息：通过矿区id查询矿区信息，返回：矿区信息")
    public MineArea getById(@ToolParam(description = "矿区id") String id) {
        try {
            log.info("Tool:查询矿区信息：{}", id);
            MineArea data = service.getById(id);
            return data;
        } catch (Exception e) {
            log.error("Tool:查询矿区信息失败：{}", e.getMessage());
            return null;
        }
    }

    @Tool(name = "getAllMineAreas", description = "矿区信息：查询所有矿区信息，返回：所有矿区信息")
    public List<MineArea> getAll() {
        try {
            log.info("Tool:查询所有矿区信息");
            List<MineArea> list = service.getAll();
            return list;
        } catch (Exception e) {
            log.error("Tool:查询所有矿区信息失败：{}", e.getMessage());
            return null;
        }
    }

//    public ResponseEntity<Map<String, Object>> update(@RequestBody MineArea entity) {
//        Map<String, Object> res = new HashMap<>();
//        int result = service.update(entity);
//        res.put("code", result > 0 ? 200 : 500);
//        res.put("message", result > 0 ? "更新成功" : "更新失败");
//        return ResponseEntity.ok(res);
//    }
//
//
//    public ResponseEntity<Map<String, Object>> delete(@PathVariable("id") String id) {
//        Map<String, Object> res = new HashMap<>();
//        int result = service.delete(id);
//        res.put("code", result > 0 ? 200 : 500);
//        res.put("message", result > 0 ? "删除成功" : "删除失败");
//        return ResponseEntity.ok(res);
//    }
}
