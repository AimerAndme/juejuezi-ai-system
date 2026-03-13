"""
API路由注册
统一管理所有API路由
"""

from fastapi import APIRouter
from app.api.v1 import (
    auth,
    conversation,
    document,
    agent,
    file,
    mine,
)

api_router = APIRouter()

api_router.include_router(auth.router, prefix="/auth", tags=["认证"])
api_router.include_router(conversation.router, prefix="/conversation", tags=["对话管理"])
api_router.include_router(document.router, prefix="/document", tags=["文档管理"])
api_router.include_router(agent.router, prefix="/ai", tags=["AI对话"])
api_router.include_router(file.router, prefix="/file", tags=["文件管理"])
api_router.include_router(mine.router, tags=["矿山数据"])
