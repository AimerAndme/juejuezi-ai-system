//package com.yupi.yuaiagent.controller;
//
//import com.yupi.yuaiagent.domin.vo.HistogramData;
//import com.yupi.yuaiagent.service.IVisualizationService;
//import lombok.AllArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/visualization")
//@AllArgsConstructor
//// 可视化模块
//public class VisualizationController {
//    private final IVisualizationService visualizationService;
//
//    @GetMapping("/histogram")
//    public ResponseEntity<Map<String, Object>> getHistogramVisualization(
//            @RequestParam("areaId") String areaId,
//            @RequestParam("tableName") String tableName,
//            @RequestParam("columnName") String columnName,
//            @RequestParam(value = "intervalCount", defaultValue = "10") int intervalCount,
//            @RequestParam(value = "minValue", required = false) Double minValue,
//            @RequestParam(value = "maxValue", required = false) Double maxValue
//    ) {
//        Map<String, Object> res = new HashMap<>();
//        try {
//            HistogramData result = visualizationService.getHistogramVisualizationData(areaId,tableName, columnName, intervalCount, minValue, maxValue);
//            res.put("code", !result.getInterval().isEmpty() ? 200 : 500);
//            res.put("message", !result.getInterval().isEmpty() ? "直方图分析成功" : "直方图分析失败");
//            res.put("data", result);
//            return ResponseEntity.ok(res);
//        } catch (Exception e) {
//            res.put("code", 500);
//            res.put("message", e.getMessage());
//            return ResponseEntity.ok(res);
//        }
//    }
//}
