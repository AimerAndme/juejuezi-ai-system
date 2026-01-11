package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.aspect.ExecutionTimeMonitor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @GetMapping("/fast")
    @ExecutionTimeMonitor(description = "快速响应接口")
    public String fastMethod() {
        return "快速响应完成";
    }

    @GetMapping("/slow")
    @ExecutionTimeMonitor(description = "慢速响应接口")
    public String slowMethod(@RequestParam(defaultValue = "1000") long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "慢速响应完成，睡眠了 " + sleepTime + "ms";
    }

    @GetMapping("/random")
    @ExecutionTimeMonitor
    public String randomTimeMethod() {
        int randomSleep = ThreadLocalRandom.current().nextInt(100, 500);
        try {
            Thread.sleep(randomSleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "随机响应完成，睡眠了 " + randomSleep + "ms";
    }

    @GetMapping("/exception")
    @ExecutionTimeMonitor(description = "异常测试接口")
    public String exceptionMethod() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        throw new RuntimeException("测试异常");
    }
}
