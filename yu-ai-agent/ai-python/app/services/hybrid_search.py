"""
混合搜索服务
对应Java版本的HybridSearchService
实现向量检索 + 文本检索的混合搜索
"""

import json
import hashlib
from typing import List, Dict, Any, Optional
from elasticsearch import AsyncElasticsearch
from langchain_elasticsearch import ElasticsearchEmbeddings
from langchain_community.embeddings import DashScopeEmbeddings
from langchain.schema import Document

from app.core.config import settings
from app.core.logging import get_logger

logger = get_logger(__name__)


class HybridSearchService:
    """混合搜索服务"""
    
    INDEX_NAME = "knowledge_base"
    DOC_VERSION_PREFIX = "doc:version:"
    SEARCH_CACHE_PREFIX = "search:"
    CACHE_TTL_SECONDS = 30 * 60
    
    def __init__(self):
        self._es_client = None
        self._embeddings = None
    
    @property
    def es_client(self) -> AsyncElasticsearch:
        """获取Elasticsearch客户端"""
        if self._es_client is None:
            self._es_client = AsyncElasticsearch(
                hosts=[{
                    "host": settings.elasticsearch_host,
                    "port": settings.elasticsearch_port,
                    "scheme": settings.elasticsearch_scheme if settings.elasticsearch_scheme == "https" else "http"
                }],
                basic_auth=(
                    settings.elasticsearch_username, 
                    settings.elasticsearch_password
                ) if settings.elasticsearch_username else None,
                verify_certs=settings.elasticsearch_ssl_verify,
            )
        return self._es_client
    
    @property
    def embeddings(self):
        """获取嵌入模型"""
        if self._embeddings is None:
            self._embeddings = DashScopeEmbeddings(
                model=settings.dashscope_embedding_model,
                dashscope_api_key=settings.dashscope_api_key,
            )
        return self._embeddings
    
    async def close(self):
        """关闭客户端连接"""
        if self._es_client:
            await self._es_client.close()
    
    async def search(
        self,
        query: str,
        top_k: int = 10,
        user_id: Optional[str] = None,
        org_tag: Optional[str] = None,
        is_public: bool = False,
    ) -> List[Dict[str, Any]]:
        """
        基础搜索方法
        对应Java版本的search方法
        """
        try:
            logger.debug(f"开始混合检索，查询: {query}, topK: {top_k}")
            
            query_vector = await self._embeddings.aembed_query(query)
            
            if not query_vector:
                logger.warning("向量生成失败，仅使用文本匹配进行搜索")
                return await self._text_only_search(query, top_k)
            
            recall_k = top_k * 30
            
            response = await self.es_client.search(
                index=self.INDEX_NAME,
                body={
                    "knn": {
                        "field": "vector",
                        "query_vector": query_vector,
                        "k": recall_k,
                        "num_candidates": recall_k,
                    },
                    "query": {
                        "match": {
                            "textContent": query
                        }
                    },
                    "rescore": {
                        "window_size": recall_k,
                        "query": {
                            "query_weight": 0.5,
                            "rescore_query_weight": 0.5,
                            "rescore_query": {
                                "match": {
                                    "textContent": {
                                        "query": query,
                                        "operator": "and"
                                    }
                                }
                            }
                        }
                    },
                    "size": top_k,
                }
            )
            
            results = []
            for hit in response["hits"]["hits"]:
                source = hit.get("_source", {})
                results.append({
                    "file_md5": source.get("fileMd5", ""),
                    "chunk_id": source.get("chunkId", 0),
                    "content": source.get("textContent", ""),
                    "score": hit.get("_score", 0.0),
                    "source": source.get("fileName", ""),
                    "user_id": source.get("userId", ""),
                    "org_tag": source.get("orgTag", ""),
                    "is_public": source.get("isPublic", False),
                })
            
            logger.debug(f"检索完成，返回 {len(results)} 个结果")
            return results
            
        except Exception as e:
            logger.error(f"搜索失败: {str(e)}")
            try:
                logger.info("尝试使用纯文本搜索作为后备方案")
                return await self._text_only_search(query, top_k)
            except Exception as fallback_error:
                logger.error(f"后备搜索也失败: {str(fallback_error)}")
                return []
    
    async def hybrid_search(
        self,
        query: str,
        top_k: int = 10,
        user_id: Optional[str] = None,
        org_tag: Optional[str] = None,
        is_public: bool = False,
        vector_weight: float = 0.7,
        text_weight: float = 0.3,
    ) -> List[Dict[str, Any]]:
        """
        混合检索（向量 + 文本）
        对应Java版本的optimizedSearch方法
        支持权限过滤
        """
        try:
            logger.debug(
                f"优化版混合检索，查询: {query}, topK: {top_k}, "
                f"向量权重: {vector_weight}, 文本权重: {text_weight}"
            )
            
            query_vector = await self._embeddings.aembed_query(query)
            
            if not query_vector:
                logger.warning("向量生成失败，仅使用文本匹配进行搜索")
                return await self._text_only_search(query, top_k)
            
            recall_k = top_k * 30
            
            bool_query = {
                "must": [
                    {"match": {"textContent": query}}
                ]
            }
            
            filter_conditions = []
            
            if user_id:
                filter_conditions.append({"term": {"userId": user_id}})
            
            if is_public:
                filter_conditions.append({"term": {"isPublic": True}})
            
            if org_tag:
                filter_conditions.append({"term": {"orgTag": org_tag}})
            
            if filter_conditions:
                bool_query["filter"] = filter_conditions
            
            response = await self.es_client.search(
                index=self.INDEX_NAME,
                body={
                    "knn": {
                        "field": "vector",
                        "query_vector": query_vector,
                        "k": recall_k,
                        "num_candidates": recall_k,
                    },
                    "query": {"bool": bool_query},
                    "rescore": {
                        "window_size": recall_k,
                        "query": {
                            "query_weight": vector_weight,
                            "rescore_query_weight": text_weight,
                            "rescore_query": {
                                "match": {
                                    "textContent": {
                                        "query": query,
                                        "operator": "and"
                                    }
                                }
                            }
                        }
                    },
                    "size": top_k,
                }
            )
            
            results = []
            for hit in response["hits"]["hits"]:
                source = hit.get("_source", {})
                results.append({
                    "file_md5": source.get("fileMd5", ""),
                    "chunk_id": source.get("chunkId", 0),
                    "content": source.get("textContent", ""),
                    "score": hit.get("_score", 0.0),
                    "source": source.get("fileName", ""),
                    "user_id": source.get("userId", ""),
                    "org_tag": source.get("orgTag", ""),
                    "is_public": source.get("isPublic", False),
                })
            
            logger.debug(f"检索完成，返回 {len(results)} 个结果")
            return results
            
        except Exception as e:
            logger.error(f"混合搜索失败: {str(e)}")
            try:
                return await self._text_only_search(query, top_k)
            except Exception as fallback_error:
                logger.error(f"后备搜索也失败: {str(fallback_error)}")
                return []
    
    async def _text_only_search(self, query: str, top_k: int) -> List[Dict[str, Any]]:
        """纯文本搜索"""
        response = await self.es_client.search(
            index=self.INDEX_NAME,
            body={
                "query": {
                    "match": {
                        "textContent": query
                    }
                },
                "size": top_k,
            }
        )
        
        results = []
        for hit in response["hits"]["hits"]:
            source = hit.get("_source", {})
            results.append({
                "file_md5": source.get("fileMd5", ""),
                "chunk_id": source.get("chunkId", 0),
                "content": source.get("textContent", ""),
                "score": hit.get("_score", 0.0),
                "source": source.get("fileName", ""),
            })
        
        return results
    
    async def search_with_cache(
        self,
        query: str,
        top_k: int = 10,
        strategy: int = 0,
        min_score: float = 0.0,
    ) -> List[Dict[str, Any]]:
        """带缓存的搜索"""
        normalized_query = self._normalize_query(query)
        cache_key = f"{self.SEARCH_CACHE_PREFIX}{normalized_query}:{top_k}:{strategy}:{min_score}"
        
        logger.debug(f"搜索查询（带缓存）: {query}, 标准化后: {normalized_query}, 缓存key: {cache_key}")
        
        try:
            from app.core.redis import get_redis_client
            redis_client = await get_redis_client()
            
            if redis_client is None:
                logger.warning("Redis未配置，跳过缓存，直接执行搜索")
                return await self.hybrid_search(query, top_k)
            
            cached = await redis_client.get(cache_key)
            if cached:
                logger.debug(f"缓存命中: {query}")
                return json.loads(cached)
            
            logger.debug(f"未命中缓存，执行检索: {query}")
            results = await self.hybrid_search(query, top_k)
            
            await redis_client.setex(
                cache_key,
                self.CACHE_TTL_SECONDS,
                json.dumps(results, ensure_ascii=False)
            )
            
            return results
            
        except Exception as e:
            logger.error(f"缓存操作失败: {str(e)}")
            return await self.hybrid_search(query, top_k)
    
    def _normalize_query(self, query: str) -> str:
        """标准化查询用于缓存键"""
        return hashlib.md5(query.lower().strip().encode()).hexdigest()
    
    async def invalidate_document_cache(self, doc_id: str):
        """使文档缓存失效"""
        try:
            from app.core.redis import get_redis_client
            redis_client = await get_redis_client()
            
            if redis_client is None:
                logger.warning("Redis未配置，无法使缓存失效")
                return
            
            version_key = f"{self.DOC_VERSION_PREFIX}{doc_id}"
            current_version = await redis_client.get(version_key) or "v1"
            
            version_num = int(current_version[1:])
            new_version = f"v{version_num + 1}"
            
            await redis_client.set(version_key, new_version)
            logger.info(f"文档缓存已失效: {doc_id} -> {new_version}")
            
        except Exception as e:
            logger.error(f"使文档缓存失败: {str(e)}")
