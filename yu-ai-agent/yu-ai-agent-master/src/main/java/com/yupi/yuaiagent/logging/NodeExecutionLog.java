package com.yupi.yuaiagent.logging;

import com.alibaba.cloud.ai.graph.OverAllState;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节点日志记录实体类
 */
@Data
public class NodeExecutionLog {
    private int index;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private ExecutionStatus executionStatus;
    private String result;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long duration;
    private int tokenUsed;
    private String errorMessage;
    private OverAllState beforState;
    private OverAllState afterState;
}
