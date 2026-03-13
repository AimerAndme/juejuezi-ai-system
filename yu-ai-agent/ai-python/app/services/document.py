"""
文档服务
处理文档的添加、删除、检索等操作
"""

import hashlib
from typing import List, Dict, Any, Optional
from langchain.text_splitter import RecursiveCharacterTextSplitter

from app.core.config import settings
from app.core.logging import get_logger
from app.services.hybrid_search import HybridSearchService

logger = get_logger(__name__)


class DocumentService:
    """文档服务"""
    
    def __init__(self):
        self.search_service = HybridSearchService()
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=settings.parsing_chunk_size,
            chunk_overlap=settings.parsing_overlap_size,
        )
    
    async def add_document(
        self,
        file_md5: str,
        content: str,
        user_id: str,
        org_tag: Optional[str] = None,
        is_public: bool = False,
        metadata: Optional[Dict] = None,
    ):
        """添加文档到向量存储"""
        try:
            logger.info(f"添加文档，file_md5: {file_md5}")
            
            chunks = self.text_splitter.split_text(content)
            
            for i, chunk in enumerate(chunks):
                doc = {
                    "fileMd5": file_md5,
                    "chunkId": i,
                    "textContent": chunk,
                    "userId": user_id,
                    "orgTag": org_tag or "",
                    "isPublic": is_public,
                    "metadata": metadata or {},
                }
                
                await self._index_document(doc)
            
            logger.info(f"文档添加成功，共 {len(chunks)} 个chunk")
            
        except Exception as e:
            logger.error(f"添加文档失败: {str(e)}")
            raise
    
    async def _index_document(self, doc: Dict[str, Any]):
        """索引文档到Elasticsearch"""
        try:
            doc_id = f"{doc['fileMd5']}_{doc['chunkId']}"
            
            await self.search_service.es_client.index(
                index=HybridSearchService.INDEX_NAME,
                id=doc_id,
                document=doc,
            )
            
        except Exception as e:
            logger.error(f"索引文档失败: {str(e)}")
            raise
    
    async def delete_document(self, file_md5: str, user_id: str):
        """删除用户的文档"""
        try:
            logger.info(f"删除文档，file_md5: {file_md5}, user_id: {user_id}")
            
            query = {
                "query": {
                    "bool": {
                        "must": [
                            {"term": {"fileMd5": file_md5}},
                            {"term": {"userId": user_id}}
                        ]
                    }
                }
            }
            
            await self.search_service.es_client.delete_by_query(
                index=HybridSearchService.INDEX_NAME,
                body=query,
            )
            
            logger.info(f"文档删除成功: {file_md5}")
            
        except Exception as e:
            logger.error(f"删除文档失败: {str(e)}")
            raise
    
    async def get_user_documents(
        self,
        user_id: str,
        org_tag: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        """获取用户的文档列表"""
        try:
            query = {
                "query": {
                    "bool": {
                        "should": [
                            {"term": {"userId": user_id}},
                            {"term": {"isPublic": True}},
                        ]
                    }
                },
                "aggs": {
                    "files": {
                        "terms": {
                            "field": "fileMd5",
                            "size": 100,
                        }
                    }
                },
                "size": 0,
            }
            
            if org_tag:
                query["query"]["bool"]["should"].append(
                    {"term": {"orgTag": org_tag}}
                )
            
            response = await self.search_service.es_client.search(
                index=HybridSearchService.INDEX_NAME,
                body=query,
            )
            
            buckets = response["aggregations"]["files"]["buckets"]
            
            documents = []
            for bucket in buckets:
                documents.append({
                    "file_md5": bucket["key"],
                    "chunk_count": bucket["doc_count"],
                })
            
            return documents
            
        except Exception as e:
            logger.error(f"获取用户文档列表失败: {str(e)}")
            return []
    
    async def search_documents(
        self,
        query: str,
        user_id: str,
        top_k: int = 10,
        org_tag: Optional[str] = None,
        is_public: bool = False,
    ) -> List[Dict[str, Any]]:
        """搜索文档"""
        return await self.search_service.hybrid_search(
            query=query,
            top_k=top_k,
            user_id=user_id,
            org_tag=org_tag,
            is_public=is_public,
        )
