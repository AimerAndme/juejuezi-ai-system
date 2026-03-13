"""
Pydantic模型定义
用于请求/响应数据验证和序列化
"""

from .base import ResponseModel, PageResponse, ErrorResponse
from .auth import UserLogin, UserRegister, TokenResponse, UserInfo, ChangePasswordRequest, UpdateUserRequest
from .file import FileUploadRequest, FileUploadResponse, FileInfo
from .document import DocumentSearchRequest, DocumentSearchResponse, DocumentChunk
from .conversation import (
    ConversationCreate, ConversationResponse, 
    MessageSend, MessageResponse, ConversationListResponse
)
from .agent import AgentQuery, AgentResponse, StreamChunk

