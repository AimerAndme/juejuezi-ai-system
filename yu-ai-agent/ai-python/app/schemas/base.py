"""
基础响应模型
统一API响应格式
"""

from typing import TypeVar, Generic, Optional, Any
from pydantic import BaseModel, Field

T = TypeVar("T")


class ResponseModel(BaseModel, Generic[T]):
    """统一响应模型
    
    对应Java版本的CommonResult
    """
    code: int = Field(default=200, description="响应码")
    message: str = Field(default="success", description="响应消息")
    data: Optional[T] = Field(default=None, description="响应数据")
    
    class Config:
        arbitrary_types_allowed = True
    
    @classmethod
    def success(cls, data: Any = None, message: str = "success") -> "ResponseModel":
        """成功响应"""
        return cls(code=200, message=message, data=data)
    
    @classmethod
    def error(cls, code: int = 500, message: str = "error", data: Any = None) -> "ResponseModel":
        """错误响应"""
        return cls(code=code, message=message, data=data)


class PageResponse(BaseModel, Generic[T]):
    """分页响应模型
    
    对应Java版本的PageResult
    """
    records: list = Field(default_factory=list, description="数据列表")
    total: int = Field(default=0, description="总记录数")
    size: int = Field(default=10, description="每页大小")
    current: int = Field(default=1, description="当前页码")
    pages: int = Field(default=0, description="总页数")
    
    @classmethod
    def create(cls, records: list, total: int, current: int = 1, size: int = 10) -> "PageResponse":
        """创建分页响应"""
        pages = (total + size - 1) // size if size > 0 else 0
        return cls(records=records, total=total, size=size, current=current, pages=pages)


class ErrorResponse(BaseModel):
    """错误响应模型"""
    code: int = Field(description="错误码")
    message: str = Field(description="错误消息")
    data: Optional[Any] = Field(default=None, description="错误详情")
    trace: Optional[str] = Field(default=None, description="堆栈跟踪（仅调试模式）")


class StreamResponse(BaseModel):
    """流式响应模型"""
    chunk: str = Field(description="数据块")
    done: bool = Field(default=False, description="是否完成")
