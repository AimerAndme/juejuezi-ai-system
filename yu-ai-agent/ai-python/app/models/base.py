"""
基础模型类
包含所有模型共享的字段和方法
"""

from datetime import datetime
from typing import Optional
from sqlalchemy import Column, DateTime, Boolean
from sqlalchemy.sql import func
from app.core.database import Base


class BaseModel(Base):
    """抽象基类，包含通用字段"""
    __abstract__ = True
    
    # 创建时间
    created_at = Column(DateTime, nullable=False, server_default=func.now())
    
    # 更新时间
    updated_at = Column(
        DateTime, 
        nullable=False, 
        server_default=func.now(),
        onupdate=func.now()
    )
    
    # 逻辑删除标记
    is_deleted = Column(Boolean, nullable=False, default=False)
    
    def to_dict(self, exclude: Optional[list] = None) -> dict:
        """
        将模型实例转换为字典
        
        Args:
            exclude: 要排除的字段列表
            
        Returns:
            模型字段的字典表示
        """
        if exclude is None:
            exclude = []
        
        result = {}
        for column in self.__table__.columns:
            if column.name not in exclude:
                result[column.name] = getattr(self, column.name)
        
        return result
    
    @classmethod
    def from_dict(cls, data: dict):
        """
        从字典创建模型实例
        
        Args:
            data: 包含字段值的字典
            
        Returns:
            模型实例
        """
        # 过滤掉不在模型字段中的键
        model_fields = {column.name for column in cls.__table__.columns}
        filtered_data = {
            key: value for key, value in data.items() 
            if key in model_fields
        }
        
        return cls(**filtered_data)