"""
认证相关Pydantic模型
"""

from typing import Optional
from pydantic import BaseModel, Field


class UserLogin(BaseModel):
    """用户登录请求"""
    account: str = Field(..., description="账号")
    password: str = Field(..., description="密码")


class UserRegister(BaseModel):
    """用户注册请求"""
    account: str = Field(..., description="账号")
    password: str = Field(..., description="密码")
    user_name: Optional[str] = Field(None, description="用户名")
    user_role: str = Field(default="user", description="用户角色")
    phone: Optional[str] = Field(None, description="手机号")


class TokenResponse(BaseModel):
    """Token响应"""
    access_token: str = Field(..., description="访问令牌")
    token_type: str = Field(default="bearer", description="令牌类型")
    expires_in: int = Field(..., description="过期时间（秒）")
    user_id: str = Field(..., description="用户ID")
    user_name: Optional[str] = Field(None, description="用户名")
    user_role: Optional[str] = Field(None, description="用户角色")


class UserInfo(BaseModel):
    """用户信息"""
    user_id: str = Field(..., description="用户ID")
    account: str = Field(..., description="账号")
    user_name: Optional[str] = Field(None, description="用户名")
    user_role: Optional[str] = Field(None, description="用户角色")
    phone: Optional[str] = Field(None, description="手机号")
    create_time: Optional[str] = Field(None, description="创建时间")


class ChangePasswordRequest(BaseModel):
    """修改密码请求"""
    old_password: str = Field(..., description="旧密码")
    new_password: str = Field(..., description="新密码")


class UpdateUserRequest(BaseModel):
    """更新用户信息请求"""
    user_name: Optional[str] = Field(None, description="用户名")
    phone: Optional[str] = Field(None, description="手机号")
    user_role: Optional[str] = Field(None, description="用户角色")
