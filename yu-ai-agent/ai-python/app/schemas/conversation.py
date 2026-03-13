"""
对话相关Pydantic模型
"""

from typing import Optional, List
from pydantic import BaseModel, Field


class ConversationCreate(BaseModel):
    """创建对话请求"""
    user_id: str = Field(..., description="用户ID")
    topic: Optional[str] = Field(None, description="对话主题")


class ConversationResponse(BaseModel):
    """对话响应"""
    conversation_id: str = Field(..., description="对话ID")
    user_id: str = Field(..., description="用户ID")
    topic: str = Field(..., description="对话主题")
    status: int = Field(..., description="状态")
    start_time: Optional[str] = Field(None, description="开始时间")
    end_time: Optional[str] = Field(None, description="结束时间")
    last_msg_time: Optional[str] = Field(None, description="最后消息时间")


class ConversationListResponse(BaseModel):
    """对话列表响应"""
    conversations: List[ConversationResponse] = Field(default_factory=list, description="对话列表")
    total: int = Field(default=0, description="总数")


class MessageSend(BaseModel):
    """发送消息请求"""
    conversation_id: Optional[str] = Field(None, description="对话ID（新建时为空）")
    content: str = Field(..., description="消息内容")
    msg_type: int = Field(default=1, description="消息类型")
    user_id: str = Field(..., description="用户ID")
    user_role: Optional[str] = Field("user", description="用户角色")


class MessageResponse(BaseModel):
    """消息响应"""
    msg_id: str = Field(..., description="消息ID")
    conversation_id: str = Field(..., description="对话ID")
    sender_type: int = Field(..., description="发送者类型")
    sender_id: str = Field(..., description="发送者ID")
    msg_type: int = Field(..., description="消息类型")
    content: str = Field(..., description="消息内容")
    send_time: Optional[str] = Field(None, description="发送时间")


class ConversationDelete(BaseModel):
    """删除对话请求"""
    conversation_id: str = Field(..., description="对话ID")
    user_id: str = Field(..., description="用户ID")


class ConversationEnd(BaseModel):
    """结束对话请求"""
    conversation_id: str = Field(..., description="对话ID")
    user_id: str = Field(..., description="用户ID")
