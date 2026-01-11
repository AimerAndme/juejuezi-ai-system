package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.domin.entity.FileExtractedImages;
import com.yupi.yuaiagent.service.IFileExtractedImagesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data/file-extracted-images")
public class FileExtractedImagesController {

    private final IFileExtractedImagesService service;

    public FileExtractedImagesController(IFileExtractedImagesService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody FileExtractedImages entity) {
        Map<String, Object> res = new HashMap<>();
        try {
            int result = service.addFileExtractedImages(entity);
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
    public ResponseEntity<Map<String, Object>> getById(@PathVariable("id") Integer id) {
        Map<String, Object> res = new HashMap<>();
        FileExtractedImages data = service.getFileExtractedImagesById(id);
        res.put("code", data != null ? 200 : 404);
        res.put("message", data != null ? "查询成功" : "未找到");
        res.put("data", data);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/by-file-md5/{fileMd5}")
    public ResponseEntity<Map<String, Object>> getByFileMd5(@PathVariable("fileMd5") String fileMd5) {
        Map<String, Object> res = new HashMap<>();
        List<FileExtractedImages> list = service.getFileExtractedImagesByFileMd5(fileMd5);
        res.put("code", 200);
        res.put("message", "查询成功");
        res.put("data", list);
        res.put("total", list.size());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/by-user-id/{userId}")
    public ResponseEntity<Map<String, Object>> getByUserId(@PathVariable("userId") Integer userId) {
        Map<String, Object> res = new HashMap<>();
        List<FileExtractedImages> list = service.getFileExtractedImagesByUserId(userId);
        res.put("code", 200);
        res.put("message", "查询成功");
        res.put("data", list);
        res.put("total", list.size());
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        Map<String, Object> res = new HashMap<>();
        List<FileExtractedImages> list = service.getAllFileExtractedImages();
        res.put("code", 200);
        res.put("message", "查询成功");
        res.put("data", list);
        res.put("total", list.size());
        return ResponseEntity.ok(res);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@RequestBody FileExtractedImages entity) {
        Map<String, Object> res = new HashMap<>();
        try {
            int result = service.updateFileExtractedImages(entity);
            res.put("code", result > 0 ? 200 : 500);
            res.put("message", result > 0 ? "更新成功" : "更新失败");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("code", 500);
            res.put("message", e.getMessage());
            return ResponseEntity.ok(res);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteById(@PathVariable("id") Integer id) {
        Map<String, Object> res = new HashMap<>();
        int result = service.deleteFileExtractedImagesById(id);
        res.put("code", result > 0 ? 200 : 500);
        res.put("message", result > 0 ? "删除成功" : "删除失败");
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/by-file-md5/{fileMd5}")
    public ResponseEntity<Map<String, Object>> deleteByFileMd5(@PathVariable("fileMd5") String fileMd5) {
        Map<String, Object> res = new HashMap<>();
        int result = service.deleteFileExtractedImagesByFileMd5(fileMd5);
        res.put("code", result > 0 ? 200 : 500);
        res.put("message", result > 0 ? "删除成功" : "删除失败");
        return ResponseEntity.ok(res);
    }
}