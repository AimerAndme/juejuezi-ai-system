package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.domin.entity.Borehole;
import com.yupi.yuaiagent.domin.vo.HistogramData;
import com.yupi.yuaiagent.service.IBoreholeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data/borehole")
public class BoreholeController {

    private final IBoreholeService service;

    public BoreholeController(IBoreholeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody Borehole entity) {
        Map<String, Object> res = new HashMap<>();
        try {
            int result = service.add(entity);
            res.put("code", result > 0 ? 200 : 500);
            res.put("message", result > 0 ? "添加成功" : "添加失败");
            res.put("data", entity);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("code", 500);
            res.put("message", e.getMessage());
            return ResponseEntity.ok(res);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable("id") String id) {
        Map<String, Object> res = new HashMap<>();
        Borehole data = service.getById(id);
        res.put("code", data != null ? 200 : 404);
        res.put("message", data != null ? "查询成功" : "未找到");
        res.put("data", data);
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> res = new HashMap<>();
        try {
            // 计算偏移量
            int offset = (page - 1) * size;
            List<Borehole> list = service.getAll(offset, size);
            int total = service.getTotalCount();
            res.put("code", 200);
            res.put("message", "查询成功");
            res.put("data", list);
            res.put("total", total);
            res.put("page", page);
            res.put("size", size);
            res.put("pages", (total + size - 1) / size);
        } catch (Exception e) {
            res.put("code", 500);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/by-area/{areaId}")
    public ResponseEntity<Map<String, Object>> getByAreaId(
            @PathVariable("areaId") String areaId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> res = new HashMap<>();
        try {
            // 计算偏移量
            int offset = (page - 1) * size;
            List<Borehole> list = service.getByAreaId(areaId, offset, size);
            int total = service.getTotalCountByAreaId(areaId);
            res.put("code", 200);
            res.put("message", "查询成功");
            res.put("data", list);
            res.put("total", total);
            res.put("page", page);
            res.put("size", size);
            res.put("pages", (total + size - 1) / size);
        } catch (Exception e) {
            res.put("code", 500);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@RequestBody Borehole entity) {
        Map<String, Object> res = new HashMap<>();
        int result = service.update(entity);
        res.put("code", result > 0 ? 200 : 500);
        res.put("message", result > 0 ? "更新成功" : "更新失败");
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable("id") String id) {
        Map<String, Object> res = new HashMap<>();
        int result = service.delete(id);
        res.put("code", result > 0 ? 200 : 500);
        res.put("message", result > 0 ? "删除成功" : "删除失败");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/histogram")
    public ResponseEntity<Map<String, Object>> getHistogramVisualization(
            @RequestParam("areaId") String areaId,
            @RequestParam(value = "intervalCount", defaultValue = "10") int intervalCount,
            @RequestParam(value = "minValue", required = false) Double minValue,
            @RequestParam(value = "maxValue", required = false) Double maxValue
    ) {
        Map<String, Object> res = new HashMap<>();
        try {
            HistogramData result = service.getHistogramVisualizationData(areaId, intervalCount, minValue, maxValue);
            res.put("code", !result.getInterval().isEmpty() ? 200 : 500);
            res.put("message", !result.getInterval().isEmpty() ? "直方图分析成功" : "直方图分析失败");
            res.put("data", result);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("code", 500);
            res.put("message", e.getMessage());
            return ResponseEntity.ok(res);
        }
    }
}
