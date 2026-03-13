"""
文档相关数据模型
对应Java版本的文档实体类
"""

from typing import Optional, Dict, Any
from sqlalchemy import Column, Integer, String, BigInteger, Boolean, DateTime, Text, JSON
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from app.models.base import BaseModel


class FileUpload(BaseModel):
    """文件上传记录"""
    __tablename__ = "file_upload"
    
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    file_md5 = Column(String(32), nullable=False, unique=True, index=True)
    file_name = Column(String(255), nullable=False)
    total_size = Column(BigInteger, nullable=False)  # 文件总大小（字节）
    status = Column(Integer, nullable=False, default=0)  # 上传状态
    user_id = Column(String(64), nullable=False, index=True)
    org_tag = Column(String(64), index=True)  # 组织标签
    is_public = Column(Boolean, default=False)
    created_at = Column(DateTime, nullable=False, server_default=func.now())
    merged_at = Column(DateTime)  # 文件合并完成时间


class DocumentVector(BaseModel):
    """文档向量存储"""
    __tablename__ = "document_vector"
    
    vector_id = Column(BigInteger, primary_key=True, autoincrement=True)
    file_md5 = Column(String(32), nullable=False, index=True)
    chunk_id = Column(Integer, nullable=False)  # 文本分块序号
    text_content = Column(Text, nullable=False)
    model_version = Column(String(64))  # 向量模型版本
    user_id = Column(String(64), nullable=False, index=True)
    org_tag = Column(String(64), index=True)  # 组织标签
    is_public = Column(Boolean, default=False)
    
    # 注意：实际向量存储可能在单独的向量数据库中（如PgVector）
    # 这里只存储元数据，向量存储在专门的向量表中


class ChunkInfo(BaseModel):
    """文件分片信息"""
    __tablename__ = "chunk_info"
    
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    file_md5 = Column(String(32), nullable=False, index=True)
    chunk_index = Column(Integer, nullable=False)  # 分片索引
    chunk_md5 = Column(String(32), nullable=False)  # 分片MD5
    storage_path = Column(String(512), nullable=False)  # 存储路径


class FileExtractedImages(BaseModel):
    """文件提取图片"""
    __tablename__ = "file_extracted_images"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String(64), nullable=False, index=True)
    file_md5 = Column(String(32), nullable=False, index=True)
    image_path = Column(String(512), nullable=False)  # 图片存储路径
    page_number = Column(Integer)  # 在原文件中的页码
    extraction_bbox = Column(JSON)  # 图片坐标信息
    vl_model_name = Column(String(128))  # VLM模型名称
    vl_model_version = Column(String(64))  # VLM模型版本
    vl_result_text = Column(Text)  # VLM识别文本
    vl_result_json = Column(JSON)  # VLM结构化结果
    vl_processing_status = Column(String(32), default="pending")  # 处理状态
    vl_processed_at = Column(DateTime)  # VLM处理时间
    created_at = Column(DateTime, nullable=False, server_default=func.now())
    updated_at = Column(DateTime, nullable=False, server_default=func.now(), onupdate=func.now())