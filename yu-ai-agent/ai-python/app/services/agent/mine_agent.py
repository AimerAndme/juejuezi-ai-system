"""
矿山专家Agent服务
对应Java版本的MineService
实现RAG + Agent的智能对话
"""

import json
import uuid
from typing import Dict, Any, Optional, AsyncGenerator
from langchain.schema import HumanMessage, AIMessage, SystemMessage
from langchain_community.chat_models import ChatOpenAI

from app.core.config import settings
from app.core.logging import get_logger
from app.services.document import DocumentService
from app.services.hybrid_search import HybridSearchService

logger = get_logger(__name__)


class MineAgentService:
    """矿山专家Agent服务"""
    
    SYSTEM_PROMPT = """你是一个专业的矿山专家助手，专门为矿山行业提供咨询服务。

你的职责包括：
1. 回答关于采矿工程、地质勘探、矿井安全等方面的问题
2. 分析矿山数据，提供专业的技术建议
3. 帮助用户理解和解决矿山相关的技术问题

请用专业、准确的语言回答用户的问题。如果需要更多上下文信息，请明确指出。"""
    
    def __init__(self):
        self.document_service = DocumentService()
        self.search_service = HybridSearchService()
        self._llm = None
    
    @property
    def llm(self):
        """获取LLM实例"""
        if self._llm is None:
            self._llm = ChatOpenAI(
                model=settings.dashscope_chat_model,
                openai_api_key=settings.dashscope_api_key,
                openai_api_base="https://dashscope.aliyuncs.com/compatible-mode/v1",
                temperature=0.7,
                streaming=False,
            )
        return self._llm
    
    async def chat(
        self,
        query: str,
        user_id: str,
        conversation_id: Optional[str] = None,
        user_role: str = "user",
        stream: bool = False,
    ) -> Dict[str, Any]:
        """
        处理用户聊天请求
        对应Java版本的chat方法
        """
        logger.info(f"处理聊天请求，user_id: {user_id}, query: {query[:50]}...")
        
        if not conversation_id:
            conversation_id = str(uuid.uuid4())
        
        search_results = await self.search_service.hybrid_search(
            query=query,
            top_k=5,
            user_id=user_id,
        )
        
        context = self._build_context(search_results)
        
        messages = [
            SystemMessage(content=self.SYSTEM_PROMPT),
        ]
        
        if context:
            messages.append(
                HumanMessage(content=f"参考文档：\n{context}\n\n用户问题：{query}")
            )
        else:
            messages.append(HumanMessage(content=query))
        
        try:
            response = await self.llm.agenerate([messages])
            answer = response.generations[0][0].text
            
            reference_docs = [
                {
                    "file_md5": r.get("file_md5", ""),
                    "content": r.get("content", "")[:200],
                    "score": r.get("score", 0),
                }
                for r in search_results[:3]
            ]
            
            return {
                "response": answer,
                "conversation_id": conversation_id,
                "reference_docs": reference_docs,
            }
            
        except Exception as e:
            logger.error(f"生成回答失败: {str(e)}")
            raise
    
    async def chat_stream(
        self,
        query: str,
        user_id: str,
        conversation_id: Optional[str] = None,
        user_role: str = "user",
    ) -> AsyncGenerator[str, None]:
        """流式聊天"""
        logger.info(f"处理流式聊天请求，user_id: {user_id}")
        
        if not conversation_id:
            conversation_id = str(uuid.uuid4())
        
        search_results = await self.search_service.hybrid_search(
            query=query,
            top_k=5,
            user_id=user_id,
        )
        
        context = self._build_context(search_results)
        
        messages = [
            SystemMessage(content=self.SYSTEM_PROMPT),
        ]
        
        if context:
            messages.append(
                HumanMessage(content=f"参考文档：\n{context}\n\n用户问题：{query}")
            )
        else:
            messages.append(HumanMessage(content=query))
        
        try:
            self.llm.streaming = True
            async for chunk in self.llm.agenerate_prompt([messages]):
                if chunk.generations:
                    text = chunk.generations[0][0].text
                    if text:
                        yield text
            
        except Exception as e:
            logger.error(f"流式生成失败: {str(e)}")
            yield f"抱歉，处理您的请求时发生错误: {str(e)}"
    
    def _build_context(self, search_results: list) -> str:
        """构建上下文"""
        if not search_results:
            return ""
        
        context_parts = []
        for i, result in enumerate(search_results[:3], 1):
            content = result.get("content", "")
            source = result.get("source", "")
            context_parts.append(f"【参考{i}】{source}\n{content}\n")
        
        return "\n".join(context_parts)
    
    async def intent_recognize(self, query: str) -> str:
        """意图识别"""
        logger.info(f"意图识别，query: {query}")
        
        intent_prompt = f"""请识别用户查询的意图，只返回意图类别，不要其他内容。
        
可能的意图：
- 采矿咨询：关于采矿方法、工艺、设计等问题
- 地质咨询：关于地质勘探、岩石、煤层等问题
- 安全咨询：关于矿山安全、通风、瓦斯等问题
- 数据查询：关于矿山数据、报表、统计等问题
- 闲聊：普通聊天

用户查询：{query}

意图："""
        
        messages = [HumanMessage(content=intent_prompt)]
        
        try:
            response = await self.llm.agenerate([messages])
            intent = response.generations[0][0].text.strip()
            return intent
        except Exception as e:
            logger.error(f"意图识别失败: {str(e)}")
            return "采矿咨询"
