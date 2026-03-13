"""
Redis客户端
提供异步Redis连接和操作
"""

import json
from typing import Optional, Any
import redis.asyncio as aioredis

from app.core.config import settings
from app.core.logging import get_logger

logger = get_logger(__name__)

_redis_client: Optional[aioredis.Redis] = None


async def get_redis_client() -> Optional[aioredis.Redis]:
    """获取Redis客户端"""
    global _redis_client
    
    if _redis_client is None:
        try:
            redis_url = str(settings.redis_url) if settings.redis_url else None
            if not redis_url:
                logger.warning("Redis URL未配置")
                return None
            
            _redis_client = await aioredis.from_url(
                redis_url,
                encoding="utf-8",
                decode_responses=True,
                password=settings.redis_password,
            )
            
            await _redis_client.ping()
            logger.info("Redis连接成功")
            
        except Exception as e:
            logger.error(f"Redis连接失败: {str(e)}")
            _redis_client = None
    
    return _redis_client


async def close_redis():
    """关闭Redis连接"""
    global _redis_client
    
    if _redis_client:
        await _redis_client.close()
        _redis_client = None
        logger.info("Redis连接已关闭")


class RedisCache:
    """Redis缓存工具类"""
    
    def __init__(self, client: Optional[aioredis.Redis] = None):
        self.client = client
    
    async def get(self, key: str) -> Optional[Any]:
        """获取值"""
        if not self.client:
            self.client = await get_redis_client()
        
        if not self.client:
            return None
        
        try:
            value = await self.client.get(key)
            if value:
                try:
                    return json.loads(value)
                except json.JSONDecodeError:
                    return value
            return None
        except Exception as e:
            logger.error(f"Redis GET失败: {str(e)}")
            return None
    
    async def set(self, key: str, value: Any, ttl: int = 3600):
        """设置值"""
        if not self.client:
            self.client = await get_redis_client()
        
        if not self.client:
            return False
        
        try:
            if isinstance(value, (dict, list)):
                value = json.dumps(value, ensure_ascii=False)
            
            await self.client.set(key, value, ex=ttl)
            return True
        except Exception as e:
            logger.error(f"Redis SET失败: {str(e)}")
            return False
    
    async def delete(self, key: str):
        """删除键"""
        if not self.client:
            self.client = await get_redis_client()
        
        if not self.client:
            return False
        
        try:
            await self.client.delete(key)
            return True
        except Exception as e:
            logger.error(f"Redis DELETE失败: {str(e)}")
            return False
    
    async def exists(self, key: str) -> bool:
        """检查键是否存在"""
        if not self.client:
            self.client = await get_redis_client()
        
        if not self.client:
            return False
        
        try:
            return await self.client.exists(key) > 0
        except Exception as e:
            logger.error(f"Redis EXISTS失败: {str(e)}")
            return False
