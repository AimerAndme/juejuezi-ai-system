"""
文件上传相关Pydantic模型
"""

from typing import Optional
from pydantic import BaseModel, Field


class FileUploadRequest(BaseModel):
    """文件上传请求"""
    file_name: str = Field(..., description="文件名")
    file_size: int = Field(..., description="文件大小")
    chunk_index: int = Field(..., description="分片索引")
    total_chunks: int = Field(..., description="总分片数")
    file_md5: str = Field(..., description="文件MD5")
    user_id: str = Field(..., description="用户ID")
    org_tag: Optional[str] = Field(None, description="组织标签")
    is_public: bool = Field(default=False, description="是否公开")


class FileUploadResponse(BaseModel):
    """文件上传响应"""
    file_md5: str = Field(..., description="文件MD5")
    chunk_index: int = Field(..., description="分片索引")
    uploaded: bool = Field(..., description="是否上传完成")
    upload_url: Optional[str] = Field(None, description="上传URL")


class FileInfo(BaseModel):
    """文件信息"""
    file_md5: str = Field(..., description="文件MD5")
    file_name: str = Field(..., description="文件名")
    total_size: int = Field(..., description="文件大小")
    status: int = Field(..., description="上传状态")
    user_id: str = Field(..., description="用户ID")
    org_tag: Optional[str] = Field(None, description="组织标签")
    is_public: bool = Field(default=False, description="是否公开")
    created_at: Optional[str] = Field(None, description="创建时间")
    merged_at: Optional[str] = Field(None, description="合并完成时间")


class FileMergeRequest(BaseModel):
    """文件合并请求"""
    file_md5: str = Field(..., description="文件MD5")
    file_name: str = Field(..., description="文件名")
    total_size: int = Field(..., description="文件大小")
    user_id: str = Field(..., description="用户ID")
    org_tag: Optional[str] = Field(None, description="组织标签")
    is_public: bool = Field(default=False, description="是否公开")


class FileParseRequest(BaseModel):
    """文件解析请求"""
    file_md5: str = Field(..., description="文件MD5")
    user_id: str = Field(..., description="用户ID")
    parsing_strategy: Optional[str] = Field("semantic", description="解析策略")


class FileParseResponse(BaseModel):
    """文件解析响应"""
    file_md5: str = Field(..., description="文件MD5")
    total_chunks: int = Field(..., description="总分块数")
    status: str = Field(..., description="处理状态")


class FileDeleteRequest(BaseModel):
    """文件删除请求"""
    file_md5: str = Field(..., description="文件MD5")
    user_id: str = Field(..., description="用户ID")
