"""
自定义中间件
包括请求日志、异常处理等
"""

import time
import json
from typing import Callable
from fastapi import Request, Response
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware

from app.core.logging import get_logger

logger = get_logger(__name__)


class RequestLoggingMiddleware(BaseHTTPMiddleware):
    """请求日志中间件"""
    
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        # 记录请求开始时间
        start_time = time.time()
        
        # 获取请求信息
        request_id = request.headers.get("X-Request-ID", "N/A")
        client_ip = request.client.host if request.client else "unknown"
        user_agent = request.headers.get("User-Agent", "unknown")
        
        # 记录请求信息
        logger.info(
            "请求开始",
            extra={
                "request_id": request_id,
                "method": request.method,
                "url": str(request.url),
                "client_ip": client_ip,
                "user_agent": user_agent,
            }
        )
        
        try:
            # 处理请求
            response = await call_next(request)
            
            # 计算处理时间
            process_time = time.time() - start_time
            
            # 记录响应信息
            logger.info(
                "请求完成",
                extra={
                    "request_id": request_id,
                    "method": request.method,
                    "url": str(request.url),
                    "status_code": response.status_code,
                    "process_time": f"{process_time:.3f}s",
                }
            )
            
            # 添加响应头
            response.headers["X-Process-Time"] = f"{process_time:.3f}"
            response.headers["X-Request-ID"] = request_id
            
            return response
            
        except Exception as exc:
            # 计算处理时间
            process_time = time.time() - start_time
            
            # 记录异常信息
            logger.error(
                "请求异常",
                extra={
                    "request_id": request_id,
                    "method": request.method,
                    "url": str(request.url),
                    "exception": str(exc),
                    "process_time": f"{process_time:.3f}s",
                },
                exc_info=True,
            )
            
            # 返回统一的错误响应
            return JSONResponse(
                status_code=500,
                content={
                    "code": 500,
                    "message": "内部服务器错误",
                    "data": None,
                }
            )


class AuthenticationMiddleware(BaseHTTPMiddleware):
    """认证中间件（基础版本）"""
    
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        # 这里可以添加JWT验证逻辑
        # 目前仅传递用户ID
        
        # 从header获取用户ID
        user_id = request.headers.get("X-User-Id")
        if user_id:
            # 将用户ID存储在请求状态中
            request.state.user_id = user_id
        
        # 继续处理请求
        return await call_next(request)


class RateLimitMiddleware(BaseHTTPMiddleware):
    """限流中间件（基础版本）"""
    
    def __init__(self, app, max_requests: int = 100, window_seconds: int = 60):
        super().__init__(app)
        self.max_requests = max_requests
        self.window_seconds = window_seconds
        # 在实际实现中，这里应该使用Redis等分布式存储
        self.request_counts = {}
    
    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        # 获取客户端标识（可以使用IP、用户ID或API Key）
        client_id = request.headers.get("X-User-Id") or request.client.host
        
        if not client_id:
            # 如果没有客户端标识，继续处理
            return await call_next(request)
        
        # 检查限流（简化版本，生产环境应使用Redis）
        current_time = time.time()
        window_start = current_time - self.window_seconds
        
        # 清理过期的请求记录
        if client_id in self.request_counts:
            self.request_counts[client_id] = [
                t for t in self.request_counts[client_id] 
                if t > window_start
            ]
        
        # 检查是否超过限制
        if client_id in self.request_counts:
            request_count = len(self.request_counts[client_id])
            if request_count >= self.max_requests:
                logger.warning(
                    "请求被限流",
                    extra={
                        "client_id": client_id,
                        "request_count": request_count,
                        "max_requests": self.max_requests,
                    }
                )
                return JSONResponse(
                    status_code=429,
                    content={
                        "code": 429,
                        "message": "请求过于频繁，请稍后再试",
                        "data": None,
                    },
                    headers={
                        "X-RateLimit-Limit": str(self.max_requests),
                        "X-RateLimit-Remaining": "0",
                        "X-RateLimit-Reset": str(int(window_start + self.window_seconds)),
                    }
                )
        
        # 记录本次请求
        if client_id not in self.request_counts:
            self.request_counts[client_id] = []
        self.request_counts[client_id].append(current_time)
        
        # 继续处理请求
        response = await call_next(request)
        
        # 添加限流头部信息
        remaining = self.max_requests - len(self.request_counts[client_id])
        response.headers["X-RateLimit-Limit"] = str(self.max_requests)
        response.headers["X-RateLimit-Remaining"] = str(max(0, remaining))
        response.headers["X-RateLimit-Reset"] = str(int(window_start + self.window_seconds))
        
        return response