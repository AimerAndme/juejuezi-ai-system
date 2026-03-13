"""
文件管理路由
对应Java版本的FileUploadController和FileManageController
"""

import os
import hashlib
from typing import Optional
from fastapi import APIRouter, Depends, UploadFile, File, Query, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.config import settings
from app.core.exceptions import BusinessException
from app.models.document import FileUpload
from app.schemas.file import (
    FileUploadRequest, FileUploadResponse, FileInfo,
    FileMergeRequest, FileParseRequest, FileDeleteRequest
)
from app.schemas.base import ResponseModel

router = APIRouter()


@router.post("/upload", response_model=ResponseModel[FileUploadResponse])
async def upload_chunk(
    file: UploadFile = File(...),
    chunk_index: int = Query(..., description="分片索引"),
    total_chunks: int = Query(..., description="总分片数"),
    file_md5: str = Query(..., description="文件MD5"),
    user_id: str = Query(..., description="用户ID"),
    org_tag: Optional[str] = Query(None, description="组织标签"),
    is_public: bool = Query(False, description="是否公开"),
    db: AsyncSession = Depends(get_db)
):
    """文件分片上传"""
    try:
        temp_dir = settings.upload_temp_dir
        os.makedirs(temp_dir, exist_ok=True)
        
        chunk_path = os.path.join(temp_dir, f"{file_md5}_chunk_{chunk_index}")
        
        content = await file.read()
        with open(chunk_path, "wb") as f:
            f.write(content)
        
        uploaded = chunk_index == total_chunks - 1
        
        return ResponseModel.success(FileUploadResponse(
            file_md5=file_md5,
            chunk_index=chunk_index,
            uploaded=uploaded,
        ))
        
    except Exception as e:
        raise BusinessException(message=f"文件上传失败: {str(e)}")


@router.post("/merge", response_model=ResponseModel[FileInfo])
async def merge_file(
    request: FileMergeRequest,
    db: AsyncSession = Depends(get_db)
):
    """合并文件分片"""
    try:
        temp_dir = settings.upload_temp_dir
        final_dir = settings.upload_final_dir
        os.makedirs(final_dir, exist_ok=True)
        
        final_path = os.path.join(final_dir, f"{request.file_md5}_{request.file_name}")
        
        with open(final_path, "wb") as outfile:
            for i in range(100):
                chunk_path = os.path.join(temp_dir, f"{request.file_md5}_chunk_{i}")
                if os.path.exists(chunk_path):
                    with open(chunk_path, "rb") as infile:
                        outfile.write(infile.read())
                    os.remove(chunk_path)
                else:
                    break
        
        file_upload = FileUpload(
            file_md5=request.file_md5,
            file_name=request.file_name,
            total_size=request.total_size,
            status=1,
            user_id=request.user_id,
            org_tag=request.org_tag,
            is_public=request.is_public,
        )
        
        db.add(file_upload)
        await db.commit()
        await db.refresh(file_upload)
        
        return ResponseModel.success(FileInfo(
            file_md5=file_upload.file_md5,
            file_name=file_upload.file_name,
            total_size=file_upload.total_size,
            status=file_upload.status,
            user_id=file_upload.user_id,
            org_tag=file_upload.org_tag,
            is_public=file_upload.is_public,
        ))
        
    except Exception as e:
        raise BusinessException(message=f"文件合并失败: {str(e)}")


@router.post("/parse", response_model=ResponseModel)
async def parse_file(
    request: FileParseRequest,
    db: AsyncSession = Depends(get_db)
):
    """解析文件（向量化）"""
    from app.services.file_parser import FileParserService
    
    try:
        parser = FileParserService()
        total_chunks = await parser.parse_file(request.file_md5, request.user_id)
        
        return ResponseModel.success({
            "file_md5": request.file_md5,
            "total_chunks": total_chunks,
            "status": "processing",
        })
        
    except Exception as e:
        raise BusinessException(message=f"文件解析失败: {str(e)}")


@router.delete("", response_model=ResponseModel)
async def delete_file(
    request: FileDeleteRequest,
    db: AsyncSession = Depends(get_db)
):
    """删除文件"""
    try:
        result = await db.execute(
            f"SELECT * FROM file_upload WHERE file_md5 = '{request.file_md5}' AND user_id = '{request.user_id}'"
        )
        file_upload = result.fetchone()
        
        if not file_upload:
            raise BusinessException(message="文件不存在")
        
        await db.execute(
            f"DELETE FROM file_upload WHERE file_md5 = '{request.file_md5}'"
        )
        await db.commit()
        
        return ResponseModel.success(message="文件删除成功")
        
    except Exception as e:
        raise BusinessException(message=f"删除失败: {str(e)}")


@router.get("/list/{user_id}", response_model=ResponseModel)
async def get_file_list(
    user_id: str,
    org_tag: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    """获取用户文件列表"""
    try:
        from sqlalchemy import select
        
        query = select(FileUpload).where(FileUpload.user_id == user_id)
        
        if org_tag:
            query = query.where(FileUpload.org_tag == org_tag)
        
        result = await db.execute(query.order_by(FileUpload.created_at.desc()))
        files = result.scalars().all()
        
        file_list = [
            FileInfo(
                file_md5=f.file_md5,
                file_name=f.file_name,
                total_size=f.total_size,
                status=f.status,
                user_id=f.user_id,
                org_tag=f.org_tag,
                is_public=f.is_public,
                created_at=f.created_at.isoformat() if f.created_at else None,
            )
            for f in files
        ]
        
        return ResponseModel.success(file_list)
        
    except Exception as e:
        raise BusinessException(message=f"获取文件列表失败: {str(e)}")
