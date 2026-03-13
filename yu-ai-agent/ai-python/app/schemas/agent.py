"""
Agent相关Pydantic模型
"""

from typing import Optional, List, Any
from pydantic import BaseModel, Field


class AgentQuery(BaseModel):
    """Agent查询请求"""
    query: str = Field(..., description="用户查询")
    user_id: str = Field(..., description="用户ID")
    conversation_id: Optional[str] = Field(None, description="对话ID")
    user_role: Optional[str] = Field("user", description="用户角色")
    stream: bool = Field(default=False, description="是否流式响应")


class AgentResponse(BaseModel):
    """Agent响应"""
    conversation_id: str = Field(..., description="对话ID")
    response: str = Field(..., description="AI回复")
    chunks: Optional[List[str]] = Field(None, description="引用文档片段")
    sources: Optional[List[dict]] = Field(None, description="引用来源")


class StreamChunk(BaseModel):
    """流式响应块"""
    chunk: str = Field(..., description="内容块")
    done: bool = Field(default=False, description="是否完成")
    conversation_id: Optional[str] = Field(None, description="对话ID")
    sources: Optional[List[dict]] = Field(None, description="引用来源")


class ChatRequest(BaseModel):
    """聊天请求"""
    query: str = Field(..., description="用户查询")
    user_id: str = Field(..., description="用户ID")
    conversation_id: Optional[str] = Field(None, description="对话ID")
    chat_id: Optional[str] = Field(None, description="聊天ID（兼容旧版）")


class ChatResponse(BaseModel):
    """聊天响应"""
    response: str = Field(..., description="AI回复")
    conversation_id: str = Field(..., description="对话ID")
    chunks: Optional[List[str]] = Field(None, description="引用文档")


class LoveAppChatRequest(BaseModel):
    """恋爱大师聊天请求"""
    message: str = Field(..., description="消息内容")
    chat_id: str = Field(..., description="聊天ID")


class LoveAppChatResponse(BaseModel):
    """恋爱大师聊天响应"""
    response: str = Field(..., description="AI回复")
    chat_id: str = Field(..., description="聊天ID")


class MineAgentRequest(BaseModel):
    """矿山专家Agent请求"""
    query: str = Field(..., description="用户查询")
    user_id: str = Field(..., description="用户ID")
    conversation_id: Optional[str] = Field(None, description="对话ID")
    user_role: str = Field(default="user", description="用户角色")


class MineAgentResponse(BaseModel):
    """矿山专家Agent响应"""
    response: str = Field(..., description="AI回复")
    conversation_id: str = Field(..., description="对话ID")
    reference_docs: Optional[List[dict]] = Field(None, description="参考文档")
