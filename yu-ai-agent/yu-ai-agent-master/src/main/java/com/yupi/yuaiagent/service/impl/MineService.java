package com.yupi.yuaiagent.service.impl;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.yupi.yuaiagent.graph.PreProcessingGraphFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MineService implements com.yupi.yuaiagent.service.IMineService {
    private final PreProcessingGraphFactory preProcessingGraphFactory;

    public MineService(PreProcessingGraphFactory preProcessingGraphFactory) {
        this.preProcessingGraphFactory = preProcessingGraphFactory;
    }

    @Override
    public String intentRecognize(String query) throws GraphStateException {
        CompiledGraph graph = preProcessingGraphFactory.getInstance();
        Optional<OverAllState> call = graph.call(Map.of("query", query));
        Map<String, Object> stringObjectMap = call.map(OverAllState::data).orElse(Map.of());
        return Optional.ofNullable(stringObjectMap.get("recognizeResult"))
                // 校验是List类型
                .filter(obj -> obj instanceof List<?>)
                .map(obj -> (List<?>) obj)
                // 校验List非空
                .filter(list -> !list.isEmpty())
                // 取第一个元素
                .map(list -> list.get(0))
                // 校验是String类型
                .filter(obj -> obj instanceof String)
                .map(obj -> (String) obj)
                // 没有的话给默认值（比如空字符串）
                .orElse("");
    }
}
