"""
Elasticsearch索引初始化脚本
对应Java版本的EsIndex相关功能
"""

from elasticsearch import Elasticsearch
from app.core.config import settings
from app.core.logging import get_logger

logger = get_logger(__name__)

INDEX_NAME = "knowledge_base"

MAPPING = {
    "mappings": {
        "properties": {
            "fileMd5": {"type": "keyword"},
            "chunkId": {"type": "integer"},
            "textContent": {
                "type": "text",
                "analyzer": "ik_max_word",
                "search_analyzer": "ik_smart"
            },
            "userId": {"type": "keyword"},
            "orgTag": {"type": "keyword"},
            "isPublic": {"type": "boolean"},
            "fileName": {"type": "keyword"},
            "vector": {
                "type": "dense_vector",
                "dims": 1536,
                "index": True,
                "similarity": "cosine"
            },
            "createdAt": {"type": "date"},
            "updatedAt": {"type": "date"},
        }
    },
    "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0,
        "analysis": {
            "analyzer": {
                "ik_max_word": {
                    "type": "standard"
                },
                "ik_smart": {
                    "type": "standard"
                }
            }
        }
    }
}


def create_index():
    """创建索引"""
    try:
        es = Elasticsearch(
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
        
        if es.indices.exists(index=INDEX_NAME):
            logger.info(f"索引 {INDEX_NAME} 已存在")
            return
        
        es.indices.create(index=INDEX_NAME, body=MAPPING)
        logger.info(f"索引 {INDEX_NAME} 创建成功")
        
    except Exception as e:
        logger.error(f"创建索引失败: {str(e)}")
        raise


def delete_index():
    """删除索引"""
    try:
        es = Elasticsearch(
            hosts=[{
                "host": settings.elasticsearch_host,
                "port": settings.elasticsearch_port,
                "scheme": settings.elasticsearch_scheme if settings.elasticsearch_scheme == "https" else "http"
            }],
            basic_auth=(
                settings.elasticsearch_username,
                settings.elasticsearch_password
            ) if settings.elasticsearch_username else None,
        )
        
        if es.indices.exists(index=INDEX_NAME):
            es.indices.delete(index=INDEX_NAME)
            logger.info(f"索引 {INDEX_NAME} 已删除")
        else:
            logger.info(f"索引 {INDEX_NAME} 不存在")
            
    except Exception as e:
        logger.error(f"删除索引失败: {str(e)}")
        raise


if __name__ == "__main__":
    import sys
    
    if len(sys.argv) > 1:
        command = sys.argv[1]
        if command == "create":
            create_index()
        elif command == "delete":
            delete_index()
        else:
            print(f"未知命令: {command}")
    else:
        print("用法: python -m app.scripts.es_index create|delete")
