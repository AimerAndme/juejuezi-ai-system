"""
配额服务
对应Java版本的RedisSlidingWindowLimiterService
实现滑动窗口限流
"""

from typing import Dict, Any, Optional
import time
from app.core.redis import get_redis_client
from app.core.logging import get_logger

logger = get_logger(__name__)


class QuotaService:
    """配额服务"""
    
    def __init__(self):
        self.default_window_ms = 60000
        self.default_max_tokens = 50000
    
    async def check_quota(
        self,
        user_id: str,
        tokens: int = 0,
        window_ms: Optional[int] = None,
        max_tokens: Optional[int] = None,
    ) -> Dict[str, Any]:
        """
        检查用户配额
        对应Java版本的checkTokenQuota方法
        """
        window = window_ms or self.default_window_ms
        max_tok = max_tokens or self.default_max_tokens
        
        redis_client = await get_redis_client()
        
        if redis_client is None:
            logger.warning("Redis未配置，跳过限流检查")
            return {"allowed": True, "remaining": max_tok, "reset_time": int(time.time() * 1000) + window}
        
        key = f"quota:{user_id}"
        now = int(time.time() * 1000)
        window_start = now - window
        
        try:
            pipe = redis_client.pipeline()
            pipe.zremrangebyscore(key, 0, window_start)
            pipe.zcard(key)
            pipe.zadd(key, {str(now): now})
            pipe.expire(key, window // 1000)
            results = await pipe.execute()
            
            current_count = results[1]
            
            allowed = current_count < max_tok
            
            return {
                "allowed": allowed,
                "remaining": max(0, max_tok - current_count - 1),
                "current": current_count + 1,
                "max": max_tok,
                "reset_time": now + window,
            }
            
        except Exception as e:
            logger.error(f"限流检查失败: {str(e)}")
            return {"allowed": True, "remaining": max_tok}
    
    async def get_user_quota(self, user_id: str) -> Dict[str, Any]:
        """获取用户配额信息"""
        redis_client = await get_redis_client()
        
        if redis_client is None:
            return {
                "max_requests": self.default_max_tokens,
                "current_requests": 0,
                "remaining": self.default_max_tokens,
            }
        
        key = f"quota:{user_id}"
        now = int(time.time() * 1000)
        window_start = now - self.default_window_ms
        
        try:
            await redis_client.zremrangebyscore(key, 0, window_start)
            current = await redis_client.zcard(key)
            
            return {
                "max_requests": self.default_max_tokens,
                "current_requests": current,
                "remaining": max(0, self.default_max_tokens - current),
            }
        except Exception as e:
            logger.error(f"获取配额失败: {str(e)}")
            return {
                "max_requests": self.default_max_tokens,
                "current_requests": 0,
                "remaining": self.default_max_tokens,
            }
    
    async def reset_quota(self, user_id: str):
        """重置用户配额"""
        redis_client = await get_redis_client()
        
        if redis_client:
            key = f"quota:{user_id}"
            await redis_client.delete(key)
            logger.info(f"用户配额已重置: {user_id}")
