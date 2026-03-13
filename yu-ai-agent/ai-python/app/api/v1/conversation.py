"""
对话管理路由
对应Java版本的ConversationController
"""

from typing import Optional
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, desc

from app.core.database import get_db
from app.models.conversation import MiningAgentConversation, MiningAgentConversationMsg
from app.schemas.conversation import (
    ConversationCreate, ConversationResponse, ConversationListResponse,
    MessageSend, MessageResponse, ConversationDelete
)
from app.schemas.base import ResponseModel

router = APIRouter()


@router.get("/latest/{user_id}", response_model=ResponseModel)
async def get_latest_conversation_with_messages(
    user_id: str,
    db: AsyncSession = Depends(get_db)
):
    """获取用户的最新对话及其消息"""
    result = await db.execute(
        select(MiningAgentConversation)
        .where(MiningAgentConversation.user_id == user_id)
        .where(MiningAgentConversation.is_deleted == False)
        .order_by(desc(MiningAgentConversation.last_msg_time))
        .limit(1)
    )
    conversation = result.scalar_one_or_none()
    
    if not conversation:
        return ResponseModel.error(code=404, message="未找到对话记录", data=None)
    
    msg_result = await db.execute(
        select(MiningAgentConversationMsg)
        .where(MiningAgentConversationMsg.conversation_id == conversation.conversation_id)
        .where(MiningAgentConversationMsg.is_deleted == False)
        .order_by(MiningAgentConversationMsg.send_time)
    )
    messages = msg_result.scalars().all()
    
    data = {
        "conversation": {
            "conversationId": conversation.conversation_id,
            "userId": conversation.user_id,
            "topic": conversation.topic,
            "status": conversation.status,
            "startTime": conversation.start_time.isoformat() if conversation.start_time else None,
            "endTime": conversation.end_time.isoformat() if conversation.end_time else None,
            "lastMsgTime": conversation.last_msg_time.isoformat() if conversation.last_msg_time else None,
        },
        "messages": [
            {
                "msgId": msg.msg_id,
                "conversationId": msg.conversation_id,
                "senderType": msg.sender_type,
                "senderId": msg.sender_id,
                "msgType": msg.msg_type,
                "msgContent": msg.msg_content,
                "sendTime": msg.send_time.isoformat() if msg.send_time else None,
            }
            for msg in messages
        ]
    }
    
    return ResponseModel.success(data)


@router.post("/create", response_model=ResponseModel[ConversationResponse])
async def create_conversation(
    user_id: str,
    db: AsyncSession = Depends(get_db)
):
    """创建新对话"""
    import uuid
    
    new_conversation = MiningAgentConversation(
        conversation_id=str(uuid.uuid4()),
        user_id=user_id,
        topic="矿山专家对话",
        status=0,
    )
    
    db.add(new_conversation)
    await db.commit()
    await db.refresh(new_conversation)
    
    conversation_data = ConversationResponse(
        conversation_id=new_conversation.conversation_id,
        user_id=new_conversation.user_id,
        topic=new_conversation.topic,
        status=new_conversation.status,
        start_time=new_conversation.start_time.isoformat() if new_conversation.start_time else None,
        end_time=new_conversation.end_time.isoformat() if new_conversation.end_time else None,
        last_msg_time=new_conversation.last_msg_time.isoformat() if new_conversation.last_msg_time else None,
    )
    
    return ResponseModel.success(conversation_data)


@router.get("/messages/{conversation_id}", response_model=ResponseModel)
async def get_messages(
    conversation_id: str,
    db: AsyncSession = Depends(get_db)
):
    """根据对话ID查询消息列表"""
    result = await db.execute(
        select(MiningAgentConversationMsg)
        .where(MiningAgentConversationMsg.conversation_id == conversation_id)
        .where(MiningAgentConversationMsg.is_deleted == False)
        .order_by(MiningAgentConversationMsg.send_time)
    )
    messages = result.scalars().all()
    
    messages_data = [
        {
            "msgId": msg.msg_id,
            "conversationId": msg.conversation_id,
            "senderType": msg.sender_type,
            "senderId": msg.sender_id,
            "msgType": msg.msg_type,
            "msgContent": msg.msg_content,
            "sendTime": msg.send_time.isoformat() if msg.send_time else None,
        }
        for msg in messages
    ]
    
    return ResponseModel.success(messages_data)


@router.delete("", response_model=ResponseModel)
async def delete_conversation(
    request: ConversationDelete,
    db: AsyncSession = Depends(get_db)
):
    """删除对话"""
    result = await db.execute(
        select(MiningAgentConversation)
        .where(MiningAgentConversation.conversation_id == request.conversation_id)
        .where(MiningAgentConversation.user_id == request.user_id)
    )
    conversation = result.scalar_one_or_none()
    
    if not conversation:
        return ResponseModel.error(code=404, message="对话不存在")
    
    conversation.is_deleted = True
    await db.commit()
    
    return ResponseModel.success(message="删除成功")


@router.get("/list/{user_id}", response_model=ResponseModel[ConversationListResponse])
async def get_conversation_list(
    user_id: str,
    db: AsyncSession = Depends(get_db)
):
    """获取用户的对话列表"""
    result = await db.execute(
        select(MiningAgentConversation)
        .where(MiningAgentConversation.user_id == user_id)
        .where(MiningAgentConversation.is_deleted == False)
        .order_by(desc(MiningAgentConversation.last_msg_time))
    )
    conversations = result.scalars().all()
    
    conversation_list = [
        ConversationResponse(
            conversation_id=c.conversation_id,
            user_id=c.user_id,
            topic=c.topic,
            status=c.status,
            start_time=c.start_time.isoformat() if c.start_time else None,
            end_time=c.end_time.isoformat() if c.end_time else None,
            last_msg_time=c.last_msg_time.isoformat() if c.last_msg_time else None,
        )
        for c in conversations
    ]
    
    return ResponseModel.success(ConversationListResponse(
        conversations=conversation_list,
        total=len(conversation_list)
    ))
