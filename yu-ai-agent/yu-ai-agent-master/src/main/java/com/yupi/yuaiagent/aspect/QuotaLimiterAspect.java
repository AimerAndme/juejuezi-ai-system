package com.yupi.yuaiagent.aspect;

import com.yupi.yuaiagent.domin.entity.UserQuota;
import com.yupi.yuaiagent.domin.vo.UserChatVO;
import com.yupi.yuaiagent.logging.LogContextHolder;
import com.yupi.yuaiagent.logging.NodeExecutionLog;
import com.yupi.yuaiagent.logging.StructuredLogBuilder;
import com.yupi.yuaiagent.mapper.UserQuotaServiceMapper;
import com.yupi.yuaiagent.service.impl.UserQuotaService;
import com.yupi.yuaiagent.service.quota.LimitResult;
import com.yupi.yuaiagent.service.quota.RedisSlidingWindowLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
@Slf4j
public class QuotaLimiterAspect {
    private final RedisSlidingWindowLimiterService redisSlidingWindowLimiterService;
    private final UserQuotaServiceMapper userQuotaServiceMapper;
    private final UserQuotaService userQuotaService;

    public QuotaLimiterAspect(RedisSlidingWindowLimiterService redisSlidingWindowLimiterService, UserQuotaServiceMapper userQuotaServiceMapper, UserQuotaService userQuotaService) {
        this.redisSlidingWindowLimiterService = redisSlidingWindowLimiterService;

        this.userQuotaServiceMapper = userQuotaServiceMapper;
        this.userQuotaService = userQuotaService;
    }

    @Pointcut("@annotation(com.yupi.yuaiagent.aspect.EnableQuotaLimiter))")
    public void quotaLimiterPointcut() {
    }

    @Around("quotaLimiterPointcut()")
    public Object quotaLimiterAround(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        UserChatVO userInfo = (UserChatVO) args[0];
        String userId = userInfo.getUserId();
        log.info("User ID: {}", userId);
        UserQuota userQuota = userQuotaServiceMapper.selectByUserId(userId);
        // 如果用户不存在，则进行初始化
        if (userQuota == null) {
            log.info("用户不存在，进行初始化");
            userQuota = new UserQuota();
            userQuota.setUserId(userId);
            userQuota.setQuotaQpm(1000);
            userQuota.setQuotaTpm(50000);
            userQuota.setQuotaConcurrent(3);
            userQuota.setQuotaPercent(90);
            userQuota.setQuotaWindows(60000);
            userQuotaServiceMapper.insert(userQuota);
        }
        // 检查用户令牌 quota
        LimitResult limitResult = redisSlidingWindowLimiterService.checkTokenQuota(userId, 0, userQuota.getQuotaWindows(), userQuota.getQuotaTpm(), userQuota.getQuotaPercent());
        if (!limitResult.isAllowed()) {
            log.info("用户限流");
            throw new RuntimeException("User request frequency exceeds limit");
        }
        try {
            Object proceed = joinPoint.proceed();
            //获取graph的执行流程跟踪日志
            try {
                Map<String, NodeExecutionLog> allNodeLogs = LogContextHolder.getAllNodeLogs();
                String jsonLog = StructuredLogBuilder.buildJsonLog(allNodeLogs);
                int totalTokens = LogContextHolder.getTotalTokens();
                redisSlidingWindowLimiterService.updateTokenQuota(userId, totalTokens);
                log.info("Graph Execution Logs: {}", jsonLog);
            } finally {
                LogContextHolder.clear();
            }
            return proceed;
        } catch (Throwable e) {
            log.info("Graph Execution Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
