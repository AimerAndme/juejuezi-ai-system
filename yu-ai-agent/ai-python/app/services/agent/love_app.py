"""
恋爱大师Agent服务
对应Java版本的LoveApp
"""

import json
from typing import Dict, Any, AsyncGenerator
from langchain.schema import HumanMessage, AIMessage, SystemMessage
from langchain_community.chat_models import ChatOpenAI

from app.core.config import settings
from app.core.logging import get_logger
from app.services.hybrid_search import HybridSearchService

logger = get_logger(__name__)


class LoveAppService:
    """恋爱大师Agent服务"""
    
    SYSTEM_PROMPT = """你是一个温暖、贴心的恋爱咨询师"恋爱大师"。

你的特点：
1. 温暖亲切，用心倾听用户的情感问题
2. 提供有建设性的恋爱建议
3. 理解年轻人的情感困惑
4. 说话风格自然流畅，像朋友聊天

请帮助用户解决恋爱中遇到的问题。"""
    
    def __init__(self):
        self.search_service = HybridSearchService()
        self._llm = None
    
    @property
    def llm(self):
        if self._llm is None:
            self._llm = ChatOpenAI(
                model=settings.dashscope_chat_model,
                openai_api_key=settings.dashscope_api_key,
                openai_api_base="https://dashscope.aliyuncs.com/compatible-mode/v1",
                temperature=0.8,
                streaming=False,
            )
        return self._llm
    
    async def chat(self, message: str, chat_id: str) -> Dict[str, Any]:
        """同步聊天"""
        logger.info(f"恋爱大师聊天，chat_id: {chat_id}")
        
        search_results = await self.search_service.search(
            query=message,
            top_k=3,
        )
        
        context = self._build_context(search_results)
        
        messages = [SystemMessage(content=self.SYSTEM_PROMPT)]
        
        if context:
            messages.append(
                HumanMessage(content=f"参考知识：\n{context}\n\n用户消息：{message}")
            )
        else:
            messages.append(HumanMessage(content=message))
        
        try:
            response = await self.llm.agenerate([messages])
            answer = response.generations[0][0].text
            
            return {
                "response": answer,
                "chat_id": chat_id,
            }
        except Exception as e:
            logger.error(f"生成回答失败: {str(e)}")
            raise
    
    async def chat_stream(self, message: str, chat_id: str) -> AsyncGenerator[str, None]:
        """流式聊天"""
        logger.info(f"恋爱大师流式聊天，chat_id: {chat_id}")
        
        search_results = await self.search_service.search(
            query=message,
            top_k=3,
        )
        
        context = self._build_context(search_results)
        
        messages = [SystemMessage(content=self.SYSTEM_PROMPT)]
        
        if context:
            messages.append(
                HumanMessage(content=f"参考知识：\n{context}\n\n用户消息：{message}")
            )
        else:
            messages.append(HumanMessage(content=message))
        
        try:
            self.llm.streaming = True
            async for chunk in self.llm.agenerate_prompt([messages]):
                if chunk.generations:
                    text = chunk.generations[0][0].text
                    if text:
                        yield text
        except Exception as e:
            logger.error(f"流式生成失败: {str(e)}")
            yield f"抱歉，处理您的请求时发生错误"
    
    def _build_context(self, search_results: list) -> str:
        if not search_results:
            return ""
        
        context_parts = []
        for i, result in enumerate(search_results[:2], 1):
            content = result.get("content", "")
            context_parts.append(f"【参考{i}】{content[:200]}...")
        
        return "\n".join(context_parts)
    
    async def do_chat_with_rag(self, message: str, chat_id: str):
        """RAG增强的聊天（生成器版本）"""
        async for chunk in self.chat_stream(message, chat_id):
            yield chunk
