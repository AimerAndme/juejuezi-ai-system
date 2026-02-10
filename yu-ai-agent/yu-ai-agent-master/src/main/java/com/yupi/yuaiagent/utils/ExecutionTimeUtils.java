package com.yupi.yuaiagent.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 执行时间监控工具类
 * 用于监控普通类方法的执行时间，替代Spring AOP的@ExecutionTimeMonitor注解
 */
@Slf4j
public class ExecutionTimeUtils {

    /**
     * 监控有返回值方法的执行时间
     *
     * @param methodName 方法名称
     * @param supplier   方法执行逻辑
     * @param <T>        返回值类型
     * @return 方法执行结果
     */
    public static <T> T monitorExecutionTime(String methodName, Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        try {
            return supplier.get();
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.info("{} 执行时间: {}ms", methodName, executionTime);
        }
    }

    /**
     * 监控无返回值方法的执行时间
     *
     * @param methodName 方法名称
     * @param runnable   方法执行逻辑
     */
    public static void monitorExecutionTime(String methodName, Runnable runnable) {
        long startTime = System.currentTimeMillis();
        try {
            runnable.run();
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.info("{} 执行时间: {}ms", methodName, executionTime);
        }
    }

    /**
     * 监控有返回值方法的执行时间（带详细信息）
     *
     * @param methodName 方法名称
     * @param details    详细信息
     * @param supplier   方法执行逻辑
     * @param <T>        返回值类型
     * @return 方法执行结果
     */
    public static <T> T monitorExecutionTime(String methodName, String details, Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        try {
            return supplier.get();
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.info("{} 执行时间: {}ms, 详情: {}", methodName, executionTime, details);
        }
    }

    /**
     * 监控无返回值方法的执行时间（带详细信息）
     *
     * @param methodName 方法名称
     * @param details    详细信息
     * @param runnable   方法执行逻辑
     */
    public static void monitorExecutionTime(String methodName, String details, Runnable runnable) {
        long startTime = System.currentTimeMillis();
        try {
            runnable.run();
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.info("{} 执行时间: {}ms, 详情: {}", methodName, executionTime, details);
        }
    }
}
