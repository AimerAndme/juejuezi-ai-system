"""
Manus超级Agent服务
对应Java版本的YuManus
支持工具调用的自主Agent
"""

import json
from typing import Dict, Any, List, AsyncGenerator
from langchain.agents import AgentExecutor, create_openai_functions_agent
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_community.chat_models import ChatOpenAI
from langchain.tools import Tool

from app.core.config import settings
from app.core.logging import get_logger

logger = get_logger(__name__)


class ManusAgentService:
    """Manus超级智能体服务"""
    
    def __init__(self):
        self._llm = None
        self._agent_executor = None
        self.tools = self._load_tools()
    
    @property
    def llm(self):
        if self._llm is None:
            self._llm = ChatOpenAI(
                model=settings.dashscope_chat_model,
                openai_api_key=settings.dashscope_api_key,
                openai_api_base="https://dashscope.aliyuncs.com/compatible-mode/v1",
                temperature=0.3,
            )
        return self._llm
    
    def _load_tools(self) -> List[Tool]:
        """加载工具"""
        from app.services.hybrid_search import HybridSearchService
        
        search_service = HybridSearchService()
        
        tools = [
            Tool(
                name="knowledge_search",
                func=self._search_knowledge,
                description="搜索知识库中的相关内容。当用户询问专业问题或有具体问题需要回答时使用。输入应该是搜索关键词。",
            ),
            Tool(
                name="calculator",
                func=self._calculator,
                description="进行数学计算。当用户需要计算数值时使用。",
            ),
        ]
        
        return tools
    
    def _search_knowledge(self, query: str) -> str:
        """搜索知识库"""
        import asyncio
        
        async def _async_search():
            search_service = HybridSearchService()
            results = await search_service.search(query, top_k=3)
            return results
        
        try:
            results = asyncio.run(_async_search())
            if not results:
                return "未找到相关内容"
            
            context = "\n".join([
                f"参考{i+1}: {r.get('content', '')[:200]}"
                for i, r in enumerate(results)
            ])
            return context
        except Exception as e:
            logger.error(f"搜索失败: {str(e)}")
            return "搜索失败"
    
    def _calculator(self, expression: str) -> str:
        """计算器"""
        try:
            allowed_chars = set("0123456789+-*/.() ")
            if not all(c in allowed_chars for c in expression):
                return "不支持的表达式"
            
            result = eval(expression)
            return f"{expression} = {result}"
        except Exception as e:
            return f"计算错误: {str(e)}"
    
    async def chat(self, message: str) -> Dict[str, Any]:
        """处理聊天请求"""
        logger.info(f"Manus聊天，message: {message[:50]}...")
        
        try:
            prompt = ChatPromptTemplate.from_messages([
                ("system", """你是一个超级智能助手Manus，可以自主完成复杂任务。
                
你可以通过调用工具来完成任务：
- knowledge_search: 搜索知识库获取相关信息
- calculator: 进行数学计算

请根据用户的问题，自主决定是否需要使用工具，并给出专业的回答。"""),
                ("human", "{input}"),
                MessagesPlaceholder(variable_name="agent_scratchpad", optional=True),
            ])
            
            agent = create_openai_functions_agent(
                llm=self.llm,
                tools=self.tools,
                prompt=prompt,
            )
            
            agent_executor = AgentExecutor(
                agent=agent,
                tools=self.tools,
                verbose=True,
                max_iterations=10,
            )
            
            response = await agent_executor.ainvoke({"input": message})
            
            return {
                "response": response.get("output", ""),
                "intermediate_steps": str(response.get("intermediate_steps", [])),
            }
            
        except Exception as e:
            logger.error(f"Agent执行失败: {str(e)}")
            return {
                "response": f"处理请求时发生错误: {str(e)}",
                "intermediate_steps": "",
            }
    
    async def run_stream(self, message: str) -> AsyncGenerator[str, None]:
        """流式执行"""
        logger.info(f"Manus流式执行，message: {message[:50]}...")
        
        try:
            prompt = ChatPromptTemplate.from_messages([
                ("system", """你是一个超级智能助手，可以自主完成复杂任务。"""),
                ("human", "{input}"),
            ])
            
            agent = create_openai_functions_agent(
                llm=self.llm,
                tools=self.tools,
                prompt=prompt,
            )
            
            agent_executor = AgentExecutor(
                agent=agent,
                tools=self.tools,
                verbose=True,
            )
            
            async for chunk in agent_executor.astream({"input": message}):
                if "output" in chunk:
                    yield chunk["output"]
                    
        except Exception as e:
            logger.error(f"流式执行失败: {str(e)}")
            yield f"错误: {str(e)}"
