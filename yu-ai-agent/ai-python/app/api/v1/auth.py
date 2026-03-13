"""
认证路由
对应Java版本的AuthController
"""

from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.database import get_db
from app.core.exceptions import AuthenticationException, BusinessException
from app.models.conversation import MiningAgentUser
from app.schemas.auth import UserLogin, UserRegister, TokenResponse, UserInfo, ChangePasswordRequest
from app.schemas.base import ResponseModel
from app.core.security import verify_password, hash_password, create_access_token

router = APIRouter()


@router.post("/login", response_model=ResponseModel[TokenResponse])
async def login(request: UserLogin, db: AsyncSession = Depends(get_db)):
    """用户登录"""
    result = await db.execute(
        select(MiningAgentUser).where(MiningAgentUser.account == request.account)
    )
    user = result.scalar_one_or_none()
    
    if not user or not verify_password(request.password, user.password):
        raise AuthenticationException(message="账号或密码错误")
    
    access_token = create_access_token(data={"sub": user.user_id})
    
    token_data = TokenResponse(
        access_token=access_token,
        token_type="bearer",
        expires_in=1800,
        user_id=user.user_id,
        user_name=user.user_name,
        user_role=user.user_role,
    )
    
    return ResponseModel.success(token_data)


@router.post("/register", response_model=ResponseModel[UserInfo])
async def register(request: UserRegister, db: AsyncSession = Depends(get_db)):
    """用户注册"""
    result = await db.execute(
        select(MiningAgentUser).where(MiningAgentUser.account == request.account)
    )
    existing_user = result.scalar_one_or_none()
    
    if existing_user:
        raise BusinessException(message="账号已存在")
    
    new_user = MiningAgentUser(
        user_id=request.account,
        account=request.account,
        password=hash_password(request.password),
        user_name=request.user_name or request.account,
        user_role=request.user_role,
        phone=request.phone,
    )
    
    db.add(new_user)
    await db.commit()
    await db.refresh(new_user)
    
    user_info = UserInfo(
        user_id=new_user.user_id,
        account=new_user.account,
        user_name=new_user.user_name,
        user_role=new_user.user_role,
        phone=new_user.phone,
    )
    
    return ResponseModel.success(user_info)


@router.get("/info", response_model=ResponseModel[UserInfo])
async def get_user_info(user_id: str, db: AsyncSession = Depends(get_db)):
    """获取用户信息"""
    result = await db.execute(
        select(MiningAgentUser).where(MiningAgentUser.user_id == user_id)
    )
    user = result.scalar_one_or_none()
    
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    
    user_info = UserInfo(
        user_id=user.user_id,
        account=user.account,
        user_name=user.user_name,
        user_role=user.user_role,
        phone=user.phone,
    )
    
    return ResponseModel.success(user_info)


@router.post("/password", response_model=ResponseModel)
async def change_password(request: ChangePasswordRequest, user_id: str, db: AsyncSession = Depends(get_db)):
    """修改密码"""
    result = await db.execute(
        select(MiningAgentUser).where(MiningAgentUser.user_id == user_id)
    )
    user = result.scalar_one_or_none()
    
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    
    if not verify_password(request.old_password, user.password):
        raise AuthenticationException(message="原密码错误")
    
    user.password = hash_password(request.new_password)
    await db.commit()
    
    return ResponseModel.success(message="密码修改成功")
