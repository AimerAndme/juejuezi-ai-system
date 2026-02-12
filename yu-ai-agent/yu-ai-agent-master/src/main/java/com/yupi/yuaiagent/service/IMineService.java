package com.yupi.yuaiagent.service;

import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import reactor.core.publisher.Flux;

import java.util.Map;


public interface IMineService {

    String intentRecognize(String query) throws GraphStateException;

    String chat(UserChatVO userChatVO) throws GraphStateException;
    Map<String, Object> chatTest(String query) throws GraphStateException;
    Flux<String> chatSee(UserChatVO userChatVO) throws GraphStateException;
}
