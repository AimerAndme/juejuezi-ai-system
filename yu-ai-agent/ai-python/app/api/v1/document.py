"""
文档检索路由
对应Java版本的DocumentVectorController
"""

from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.services.hybrid_search import HybridSearchService
from app.services.document import DocumentService
from app.schemas.document import (
    DocumentSearchRequest, DocumentSearchResponse, DocumentChunk,
    HybridSearchRequest, DocumentAddRequest, DocumentDeleteRequest
)
from app.schemas.base import ResponseModel

router = APIRouter()


@router.post("/search", response_model=ResponseModel[DocumentSearchResponse])
async def search_documents(
    request: DocumentSearchRequest,
    db: AsyncSession = Depends(get_db)
):
    """文档检索"""
    search_service = HybridSearchService()
    
    results = await search_service.search(
        query=request.query,
        top_k=request.top_k,
        user_id=request.user_id,
        org_tag=request.org_tag,
        is_public=request.is_public,
    )
    
    chunks = [
        DocumentChunk(
            chunk_id=r.get("chunk_id", 0),
            file_md5=r.get("file_md5", ""),
            content=r.get("content", ""),
            score=r.get("score", 0.0),
            source=r.get("source"),
        )
        for r in results
    ]
    
    response = DocumentSearchResponse(
        query=request.query,
        total=len(chunks),
        chunks=chunks,
    )
    
    return ResponseModel.success(response)


@router.post("/hybrid_search", response_model=ResponseModel[DocumentSearchResponse])
async def hybrid_search_documents(
    request: HybridSearchRequest,
    db: AsyncSession = Depends(get_db)
):
    """混合检索（向量 + 文本）"""
    search_service = HybridSearchService()
    
    results = await search_service.hybrid_search(
        query=request.query,
        top_k=request.top_k,
        user_id=request.user_id,
        org_tag=request.org_tag,
        is_public=request.is_public,
        vector_weight=request.vector_weight,
        text_weight=request.text_weight,
    )
    
    chunks = [
        DocumentChunk(
            chunk_id=r.get("chunk_id", 0),
            file_md5=r.get("file_md5", ""),
            content=r.get("content", ""),
            score=r.get("score", 0.0),
            source=r.get("source"),
        )
        for r in results
    ]
    
    response = DocumentSearchResponse(
        query=request.query,
        total=len(chunks),
        chunks=chunks,
    )
    
    return ResponseModel.success(response)


@router.post("/add", response_model=ResponseModel)
async def add_document(
    request: DocumentAddRequest,
    db: AsyncSession = Depends(get_db)
):
    """添加文档"""
    doc_service = DocumentService()
    
    await doc_service.add_document(
        file_md5=request.file_md5,
        content=request.content,
        user_id=request.user_id,
        org_tag=request.org_tag,
        is_public=request.is_public,
        metadata=request.metadata,
    )
    
    return ResponseModel.success(message="文档添加成功")


@router.post("/delete", response_model=ResponseModel)
async def delete_document(
    request: DocumentDeleteRequest,
    db: AsyncSession = Depends(get_db)
):
    """删除文档"""
    doc_service = DocumentService()
    
    await doc_service.delete_document(
        file_md5=request.file_md5,
        user_id=request.user_id,
    )
    
    return ResponseModel.success(message="文档删除成功")


@router.get("/list/{user_id}", response_model=ResponseModel)
async def get_document_list(
    user_id: str,
    org_tag: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    """获取用户的文档列表"""
    doc_service = DocumentService()
    
    documents = await doc_service.get_user_documents(
        user_id=user_id,
        org_tag=org_tag,
    )
    
    return ResponseModel.success(documents)
