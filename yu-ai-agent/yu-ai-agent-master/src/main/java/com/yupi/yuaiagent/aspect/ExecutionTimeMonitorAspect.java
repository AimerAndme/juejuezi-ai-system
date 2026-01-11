package com.yupi.yuaiagent.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class ExecutionTimeMonitorAspect {

    @Pointcut("@annotation(com.yupi.yuaiagent.aspect.ExecutionTimeMonitor)")
    public void executionTimeMonitorPointcut() {
    }

    @Around("executionTimeMonitorPointcut()")
    public Object monitorExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ExecutionTimeMonitor annotation = method.getAnnotation(ExecutionTimeMonitor.class);
        String description = annotation.description();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String methodInfo = className + "." + methodName;

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            String logMessage = description.isEmpty() ? methodInfo : description;
            log.info("方法执行耗时监控 - 方法: {}, 耗时: {}ms", logMessage, executionTime);
            return result;
        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            String logMessage = description.isEmpty() ? methodInfo : description;
            log.error("方法执行耗时监控 - 方法: {}, 耗时: {}ms, 发生异常: {}", logMessage, executionTime, throwable.getMessage());
            throw throwable;
        }
    }
}
