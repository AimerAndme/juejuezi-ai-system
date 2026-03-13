"""
文档检索相关Pydantic模型
"""

from typing import Optional, List
from pydantic import BaseModel, Field


class DocumentSearchRequest(BaseModel):
    """文档检索请求"""
    query: str = Field(..., description="检索关键词")
    top_k: int = Field(default=10, description="返回结果数量")
    user_id: str = Field(..., description="用户ID")
    org_tag: Optional[str] = Field(None, description="组织标签")
    is_public: bool = Field(default=False, description="是否包含公开文档")


class DocumentChunk(BaseModel):
    """文档片段"""
    chunk_id: int = Field(..., description="片段ID")
    file_md5: str = Field(..., description="文件MD5")
    content: str = Field(..., description="内容")
    score: float = Field(..., description="相似度分数")
    source: Optional[str] = Field(None, description="来源文件")


class DocumentSearchResponse(BaseModel):
    """文档检索响应"""
    query: str = Field(..., description="检索关键词")
    total: int = Field(..., description="结果总数")
    chunks: List[DocumentChunk] = Field(default_factory=list, description="检索结果")


class HybridSearchRequest(BaseModel):
    """混合检索请求"""
    query: str = Field(..., description="检索关键词")
    top_k: int = Field(default=10, description="返回结果数量")
    user_id: str = Field(..., description="用户ID")
    org_tag: Optional[str] = Field(None, description="组织标签")
    is_public: bool = Field(default=False, description="是否包含公开文档")
    vector_weight: float = Field(default=0.7, description="向量检索权重")
    text_weight: float = Field(default=0.3, description="文本检索权重")


class DocumentAddRequest(BaseModel):
    """添加文档请求"""
    file_md5: str = Field(..., description="文件MD5")
    content: str = Field(..., description="文档内容")
    user_id: str = Field(..., description="用户ID")
    org_tag: Optional[str] = Field(None, description="组织标签")
    is_public: bool = Field(default=False, description="是否公开")
    metadata: Optional[dict] = Field(None, description="元数据")


class DocumentDeleteRequest(BaseModel):
    """删除文档请求"""
    file_md5: str = Field(..., description="文件MD5")
    user_id: str = Field(..., description="用户ID")
