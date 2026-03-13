"""
用户配额模型
对应Java版本的UserQuota实体类
"""

from sqlalchemy import Column, String, Integer
from app.models.base import BaseModel


class UserQuota(BaseModel):
    """用户配额"""
    __tablename__ = "user_quota"
    
    user_id = Column(String(64), primary_key=True)
    quota_qpm = Column(Integer, default=0)  # 每分钟查询次数配额
    quota_tpm = Column(Integer, default=0)  # 每分钟token配额
    quota_concurrent = Column(Integer, default=1)  # 并发请求配额
    user_level = Column(Integer, default=0)  # 用户级别
    status = Column(Integer, default=0)  # 状态
    quota_percent = Column(Integer, default=0)  # 配额使用百分比
    quota_windows = Column(Integer, default=60000)  # 配额窗口时间（毫秒）