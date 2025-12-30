package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.domin.entity.Roadway;
import com.yupi.yuaiagent.service.IRoadwayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data/roadway")
public class RoadwayController {

    private final IRoadwayService service;

    public RoadwayController(IRoadwayService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody Roadway entity) {
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
        Roadway data = service.getById(id);
        res.put("code", data != null ? 200 : 404);
        res.put("message", data != null ? "查询成功" : "未找到");
        res.put("data", data);
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        Map<String, Object> res = new HashMap<>();
        List<Roadway> list = service.getAll();
        res.put("code", 200);
        res.put("message", "查询成功");
        res.put("data", list);
        res.put("total", list.size());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/by-area/{areaId}")
    public ResponseEntity<Map<String, Object>> getByAreaId(@PathVariable("areaId") String areaId) {
        Map<String, Object> res = new HashMap<>();
        List<Roadway> list = service.getByAreaId(areaId);
        res.put("code", 200);
        res.put("message", "查询成功");
        res.put("data", list);
        res.put("total", list.size());
        return ResponseEntity.ok(res);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@RequestBody Roadway entity) {
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
}
