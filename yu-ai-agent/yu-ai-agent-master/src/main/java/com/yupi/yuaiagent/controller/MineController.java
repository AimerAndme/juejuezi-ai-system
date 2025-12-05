package com.yupi.yuaiagent.controller;

import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.yupi.yuaiagent.graph.PreProcessingGraphFactory;
import com.yupi.yuaiagent.service.IMineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String chat(@RequestParam String query) throws GraphStateException {
        return mineService.chat(query);
    }
}
