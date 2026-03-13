"""
对话相关数据模型
对应Java版本的对话和用户实体类
"""

from sqlalchemy import Column, String, Integer, Boolean, DateTime, Text
from sqlalchemy.sql import func
from app.models.base import BaseModel


class MiningAgentUser(BaseModel):
    """矿山Agent用户"""
    __tablename__ = "mining_agent_user"
    
    user_id = Column(String(64), primary_key=True)  # UUID/员工编号
    account = Column(String(64), nullable=False, unique=True, index=True)
    password = Column(String(255), nullable=False)  # 加密后的密码
    user_name = Column(String(64), nullable=False)
    user_role = Column(String(32), nullable=False, default="user")  # miner/technician/manager
    phone = Column(String(20))
    create_time = Column(DateTime, nullable=False, server_default=func.now())


class MiningAgentConversation(BaseModel):
    """矿山Agent对话"""
    __tablename__ = "mining_agent_conversation"
    
    conversation_id = Column(String(64), primary_key=True)  # UUID
    user_id = Column(String(64), nullable=False, index=True)
    topic = Column(String(255), default="矿山专家对话")
    status = Column(Integer, nullable=False, default=0)  # 0-进行中, 1-已结束, 2-已归档
    start_time = Column(DateTime, nullable=False, server_default=func.now())
    end_time = Column(DateTime)
    last_msg_time = Column(DateTime, nullable=False, server_default=func.now())
    is_deleted = Column(Boolean, nullable=False, default=False)


class MiningAgentConversationMsg(BaseModel):
    """矿山Agent对话消息"""
    __tablename__ = "mining_agent_conversation_msg"
    
    msg_id = Column(String(64), primary_key=True)  # UUID
    conversation_id = Column(String(64), nullable=False, index=True)
    sender_type = Column(Integer, nullable=False, default=0)  # 0-user, 1-agent, 2-system
    sender_id = Column(String(64), nullable=False)
    msg_type = Column(Integer, nullable=False, default=1)  # 0-system, 1-user, 2-assistant
    msg_content = Column(Text, nullable=False)
    file_meta = Column(Text)  # JSON格式文件元信息
    send_time = Column(DateTime, nullable=False, server_default=func.now())
    is_deleted = Column(Boolean, nullable=False, default=False)