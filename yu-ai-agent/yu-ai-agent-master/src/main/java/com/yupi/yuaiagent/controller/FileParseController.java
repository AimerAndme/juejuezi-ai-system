package com.yupi.yuaiagent.controller;


import com.yupi.yuaiagent.service.IFileParseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/parse")
@AllArgsConstructor
public class FileParseController {


    private final IFileParseService fileParseService;


    @PostMapping
    public ResponseEntity<String> parseDocument(@RequestParam("file_md5") String fileMd5,
                                                @RequestParam(value = "userId") String userId) {
        try {
            fileParseService.parseAndVectorize(fileMd5, userId);
            return ResponseEntity.ok("文档正在解析中，请稍等！");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("文档解析失败：" + e.getMessage());
        }

    }
}