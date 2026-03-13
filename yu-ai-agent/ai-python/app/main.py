"""
FastAPI主应用
保持与Java Spring版本API兼容
"""

from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.trustedhost import TrustedHostMiddleware
import uvicorn

from app.core.config import settings
from app.core.database import init_db, close_db
from app.api.v1 import api_router
from app.core.logging import setup_logging
from app.core.middleware import RequestLoggingMiddleware


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    应用生命周期管理
    - 启动时初始化
    - 关闭时清理资源
    """
    # 启动时
    setup_logging()
    print(f"启动 {settings.app_name} v{settings.app_version}")
    print(f"环境: {settings.environment}")
    print(f"调试模式: {settings.debug}")
    
    # 初始化数据库
    if settings.environment != "testing":
        await init_db()
    
    yield
    
    # 关闭时
    await close_db()
    print("应用关闭完成")


# 创建FastAPI应用实例
app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description="AI Agent后端 - Python重构版本，保持与Java Spring API兼容",
    docs_url="/docs" if settings.debug else None,
    redoc_url="/redoc" if settings.debug else None,
    openapi_url="/openapi.json" if settings.debug else None,
    lifespan=lifespan,
)

# 添加中间件
# CORS中间件
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 可信主机中间件
app.add_middleware(
    TrustedHostMiddleware,
    allowed_hosts=["*"] if settings.debug else settings.cors_origins,
)

# 请求日志中间件
app.add_middleware(RequestLoggingMiddleware)

# 注册API路由
app.include_router(api_router, prefix=settings.api_prefix)


@app.get("/")
async def root():
    """健康检查端点"""
    return {
        "code": 200,
        "message": f"{settings.app_name} 运行正常",
        "data": {
            "app": settings.app_name,
            "version": settings.app_version,
            "environment": settings.environment,
        }
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    from datetime import datetime
    return {
        "code": 200,
        "message": "服务正常",
        "data": {
            "status": "healthy",
            "timestamp": datetime.now().isoformat()
        }
    }


if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=settings.debug,
        log_level=settings.log_level.lower(),
    )
