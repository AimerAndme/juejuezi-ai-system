"""
AI对话路由
对应Java版本的MineController和AiController
支持流式和非流式对话
"""

import json
from typing import Optional
from fastapi import APIRouter, Depends, Query, HTTPException
from fastapi.responses import StreamingResponse
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.exceptions import BusinessException
from app.services.agent.mine_agent import MineAgentService
from app.services.agent.love_app import LoveAppService
from app.services.quota import QuotaService
from app.schemas.agent import (
    AgentQuery, AgentResponse, StreamChunk,
    ChatRequest, ChatResponse, LoveAppChatRequest, LoveAppChatResponse,
    MineAgentRequest, MineAgentResponse
)
from app.schemas.base import ResponseModel

router = APIRouter()


@router.get("/mine/chat", response_model=ResponseModel[MineAgentResponse])
async def mine_agent_chat(
    query: str = Query(..., description="用户查询"),
    user_id: str = Query(..., description="用户ID"),
    conversation_id: Optional[str] = Query(None, description="对话ID"),
    user_role: str = Query("user", description="用户角色"),
    db: AsyncSession = Depends(get_db)
):
    """矿山专家Agent聊天（非流式）"""
    quota_service = QuotaService()
    limit_result = await quota_service.check_quota(user_id)
    
    if not limit_result.get("allowed", True):
        raise BusinessException(message="用户请求过于频繁，请稍后再试")
    
    try:
        agent_service = MineAgentService()
        result = await agent_service.chat(
            query=query,
            user_id=user_id,
            conversation_id=conversation_id,
            user_role=user_role,
            stream=False,
        )
        
        return ResponseModel.success(result)
        
    except Exception as e:
        raise BusinessException(message=f"服务出错: {str(e)}")


@router.get("/mine/chat/sse", response_model=ResponseModel)
async def mine_agent_chat_sse(
    query: str = Query(..., description="用户查询"),
    user_id: str = Query(..., description="用户ID"),
    conversation_id: Optional[str] = Query(None, description="对话ID"),
    user_role: str = Query("user", description="用户角色"),
    db: AsyncSession = Depends(get_db)
):
    """矿山专家Agent聊天（SSE流式）"""
    quota_service = QuotaService()
    limit_result = await quota_service.check_quota(user_id)
    
    if not limit_result.get("allowed", True):
        raise BusinessException(message="用户请求过于频繁，请稍后再试")
    
    async def event_generator():
        agent_service = MineAgentService()
        try:
            async for chunk in agent_service.chat_stream(
                query=query,
                user_id=user_id,
                conversation_id=conversation_id,
                user_role=user_role,
            ):
                yield f"data: {json.dumps({'chunk': chunk}, ensure_ascii=False)}\n\n"
            
            yield f"data: {json.dumps({'done': True}, ensure_ascii=False)}\n\n"
            
        except Exception as e:
            yield f"data: {json.dumps({'error': str(e)}, ensure_ascii=False)}\n\n"
    
    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        }
    )


@router.get("/love_app/chat/sync", response_model=ResponseModel)
async def love_app_chat_sync(
    message: str = Query(..., description="消息内容"),
    chat_id: str = Query(..., description="聊天ID"),
    db: AsyncSession = Depends(get_db)
):
    """恋爱大师同步聊天"""
    try:
        agent_service = LoveAppService()
        result = await agent_service.chat(message, chat_id)
        
        return ResponseModel.success(result)
        
    except Exception as e:
        raise BusinessException(message=f"服务出错: {str(e)}")


@router.get("/love_app/chat/sse", response_model=ResponseModel)
async def love_app_chat_sse(
    message: str = Query(..., description="消息内容"),
    chat_id: str = Query(..., description="聊天ID"),
    db: AsyncSession = Depends(get_db)
):
    """恋爱大师SSE流式聊天"""
    async def event_generator():
        agent_service = LoveAppService()
        try:
            async for chunk in agent_service.chat_stream(message, chat_id):
                yield f"data: {json.dumps({'chunk': chunk}, ensure_ascii=False)}\n\n"
            
            yield f"data: {json.dumps({'done': True}, ensure_ascii=False)}\n\n"
            
        except Exception as e:
            yield f"data: {json.dumps({'error': str(e)}, ensure_ascii=False)}\n\n"
    
    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
    )


@router.get("/love_app/chat/server_sent_event", response_model=ResponseModel)
async def love_app_chat_sse_event(
    message: str = Query(..., description="消息内容"),
    chat_id: str = Query(..., description="聊天ID"),
    db: AsyncSession = Depends(get_db)
):
    """恋爱大师Server-Sent Events流式聊天"""
    return await love_app_chat_sse(message, chat_id, db)


@router.get("/manus/chat", response_model=ResponseModel)
async def manus_chat(
    message: str = Query(..., description="用户消息"),
    db: AsyncSession = Depends(get_db)
):
    """Manus超级智能体聊天"""
    from app.services.agent.manus import ManusAgentService
    
    try:
        agent_service = ManusAgentService()
        result = await agent_service.chat(message)
        
        return ResponseModel.success(result)
        
    except Exception as e:
        raise BusinessException(message=f"服务出错: {str(e)}")
