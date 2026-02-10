package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.service.IFileNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/files/notification")
@AllArgsConstructor
@Tag(name = "文件通知管理", description = "文件处理通知相关接口")
public class FileNotificationController {

    private final IFileNotificationService fileNotificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "订阅文件处理通知", description = "通过 SSE 订阅文件处理进度和结果通知")
    public SseEmitter subscribe(
            @Parameter(description = "用户ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "文件MD5", required = true)
            @RequestParam String fileMd5) {
        log.info("用户 {} 订阅文件 {} 的处理通知", userId, fileMd5);
        return fileNotificationService.subscribe(userId, fileMd5);
    }
}
