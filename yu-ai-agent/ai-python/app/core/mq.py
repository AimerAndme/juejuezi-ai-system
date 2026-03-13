"""
消息队列服务
提供RabbitMQ异步消息队列功能
"""

import json
from typing import Optional, Callable, Any
import aio_pika
from aio_pika import ExchangeType

from app.core.config import settings
from app.core.logging import get_logger

logger = get_logger(__name__)


class MessageQueueService:
    """消息队列服务"""
    
    def __init__(self):
        self._connection = None
        self._channel = None
        self._exchange = None
    
    async def connect(self):
        """建立连接"""
        try:
            self._connection = await aio_pika.connect_robust(
                str(settings.rabbitmq_url),
            )
            self._channel = await self._connection.channel()
            self._exchange = await self._channel.declare_exchange(
                "yuaiagent",
                ExchangeType.TOPIC,
                durable=True,
            )
            logger.info("RabbitMQ连接成功")
        except Exception as e:
            logger.error(f"RabbitMQ连接失败: {str(e)}")
    
    async def close(self):
        """关闭连接"""
        if self._connection:
            await self._connection.close()
            logger.info("RabbitMQ连接已关闭")
    
    async def publish(self, routing_key: str, message: dict):
        """发布消息"""
        if not self._exchange:
            await self.connect()
        
        message_body = json.dumps(message, ensure_ascii=False).encode()
        
        await self._exchange.publish(
            aio_pika.Message(
                body=message_body,
                content_type="application/json",
            ),
            routing_key=routing_key,
        )
        
        logger.debug(f"消息已发布: {routing_key}")
    
    async def subscribe(self, routing_key: str, callback: Callable):
        """订阅消息"""
        if not self._channel:
            await self.connect()
        
        queue = await self._channel.declare_queue(
            f"queue_{routing_key}",
            durable=True,
        )
        
        await queue.bind(self._exchange, routing_key=routing_key)
        
        async with queue.iterator() as queue_iter:
            async for message in queue_iter:
                async with message.process():
                    try:
                        data = json.loads(message.body.decode())
                        await callback(data)
                    except Exception as e:
                        logger.error(f"处理消息失败: {str(e)}")


_mq_service: Optional[MessageQueueService] = None


async def get_mq_service() -> MessageQueueService:
    """获取消息队列服务实例"""
    global _mq_service
    
    if _mq_service is None:
        _mq_service = MessageQueueService()
        await _mq_service.connect()
    
    return _mq_service
