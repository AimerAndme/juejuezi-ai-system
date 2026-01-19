package com.yupi.yuaiagent.logging;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * 节点执行拦截器
 */
public class LoggingNodeActionWrapper implements NodeAction {
    private final NodeAction delegate;
    private final String nodeId;
    private final String nodeName;

    public LoggingNodeActionWrapper(NodeAction delegate, String nodeId, String nodeName) {
        this.delegate = delegate;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        NodeExecutionLog nodeExecutionLog = new NodeExecutionLog();
        nodeExecutionLog.setNodeId(nodeId);
        nodeExecutionLog.setNodeName(nodeName);
        nodeExecutionLog.setNodeType(delegate.getClass().getSimpleName());
        nodeExecutionLog.setStartTime(LocalDateTime.now());
        nodeExecutionLog.setExecutionStatus(ExecutionStatus.RUNNING);
        nodeExecutionLog.setBeforState(state);
        ZoneId zoneId = ZoneId.systemDefault();
        try {
            Map<String, Object> result = delegate.apply(state);
            nodeExecutionLog.setEndTime(LocalDateTime.now());
            nodeExecutionLog.setResult(result.toString());
            nodeExecutionLog.setAfterState(result.get("state"));
            nodeExecutionLog.setDuration(nodeExecutionLog.getEndTime().atZone(zoneId).toInstant().toEpochMilli() - nodeExecutionLog.getStartTime().atZone(zoneId).toInstant().toEpochMilli());
            nodeExecutionLog.setExecutionStatus(ExecutionStatus.SUCCESS);
            LogContextHolder.addNodeLog(nodeId, nodeExecutionLog);
            return result;
        } catch (Exception e) {
            nodeExecutionLog.setEndTime(LocalDateTime.now());
            nodeExecutionLog.setDuration(nodeExecutionLog.getEndTime().atZone(zoneId).toInstant().toEpochMilli() - nodeExecutionLog.getStartTime().atZone(zoneId).toInstant().toEpochMilli());
            nodeExecutionLog.setExecutionStatus(ExecutionStatus.FAILED);
            nodeExecutionLog.setErrorMessage(e.getMessage());
            LogContextHolder.addNodeLog(nodeId, nodeExecutionLog);
            throw e;
        }
    }
}
