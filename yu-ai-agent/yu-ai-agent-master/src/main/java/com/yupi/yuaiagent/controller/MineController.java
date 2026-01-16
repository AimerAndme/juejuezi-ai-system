package com.yupi.yuaiagent.controller;

import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import com.yupi.yuaiagent.graph.PreProcessingGraphFactory;
import com.yupi.yuaiagent.service.IMineService;
import com.yupi.yuaiagent.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/mine")
public class MineController {

    private final PreProcessingGraphFactory graphFactory;
    private final IMineService mineService;

    public MineController(PreProcessingGraphFactory graphFactory, IMineService mineService) {
        this.graphFactory = graphFactory;
        this.mineService = mineService;
    }

    @GetMapping(value = "/intent-recognize", produces = "text/event-stream; charset=UTF-8")
    public String intentRecognize(@RequestParam String query) throws GraphStateException {
        return mineService.intentRecognize(query);
    }

    @GetMapping(value = "/chat")
    public Result<?> chat(UserChatVO userChatVO) throws GraphStateException {
        try {
            String chat = mineService.chat(userChatVO);
            log.info(String.valueOf(LocalDateTime.now()));
            return Result.success(chat);

        } catch (Exception e) {
            log.error("服务出错：{}", e.getMessage());
            return Result.fail("服务出错：{}");
        }

    }
}
