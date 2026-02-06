package com.yupi.yuaiagent.exception;

import com.yupi.yuaiagent.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 统一处理异常并记录日志
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常 - 请求路径: {}, 错误码: {}, 错误信息: {}",
                request.getRequestURI(), e.getCode(), e.getMessage(), e);
        return new Result<>(e.getCode(), e.getMessage(), null);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数校验异常 - 请求路径: {}, 错误信息: {}", request.getRequestURI(), errorMsg, e);
        return new Result<>(ErrorCode.BAD_REQUEST.getCode(), errorMsg, null);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String errorMsg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数绑定异常 - 请求路径: {}, 错误信息: {}", request.getRequestURI(), errorMsg, e);
        return new Result<>(ErrorCode.BAD_REQUEST.getCode(), errorMsg, null);
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.error("文件上传大小超限 - 请求路径: {}, 错误信息: {}", request.getRequestURI(), e.getMessage(), e);
        return new Result<>(ErrorCode.FILE_SIZE_EXCEEDED.getCode(),
                ErrorCode.FILE_SIZE_EXCEEDED.getMessage(), null);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.error("非法参数异常 - 请求路径: {}, 错误信息: {}", request.getRequestURI(), e.getMessage(), e);
        return new Result<>(ErrorCode.BAD_REQUEST.getCode(), e.getMessage(), null);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常 - 请求路径: {}, 堆栈信息: ", request.getRequestURI(), e);
        return new Result<>(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "系统内部错误，请联系管理员", null);
    }

    /**
     * 处理异步请求超时异常（SSE 超时） SSE 超时是正常行为，不需要返回 JSON 错误
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public void handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e, HttpServletRequest request) {
        log.debug("SSE 连接超时 - 请求路径: {}", request.getRequestURI());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常 - 请求路径: {}, 错误信息: {}", request.getRequestURI(), e.getMessage(), e);
        return new Result<>(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "系统运行异常: " + e.getMessage(), null);
    }

    /**
     * 处理其他所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常 - 请求路径: {}, 异常类型: {}, 错误信息: {}",
                request.getRequestURI(), e.getClass().getName(), e.getMessage(), e);
        return new Result<>(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), null);
    }
}
