package com.yupi.yuaiagent.service;

import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import reactor.core.publisher.Flux;


public interface IMineService {

    String intentRecognize(String query) throws GraphStateException;

    String chat(String query) throws GraphStateException;
}
