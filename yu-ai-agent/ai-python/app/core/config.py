"""
应用配置管理
基于Pydantic v1
"""

import os
from typing import Optional, List
from pydantic import BaseSettings, Field


class Settings(BaseSettings):
    """应用配置"""
    
    app_name: str = "yu-ai-agent-python"
    app_version: str = "0.1.0"
    debug: bool = True
    environment: str = "development"
    
    host: str = "0.0.0.0"
    port: int = 8123
    api_prefix: str = "/api"
    log_level: str = "INFO"
    
    database_url: str = "postgresql://postgres:postgres@localhost:5432/sdagent"
    database_pool_size: int = 10
    database_max_overflow: int = 20
    
    elasticsearch_host: str = "192.168.152.129"
    elasticsearch_port: int = 9200
    elasticsearch_scheme: str = "http"
    elasticsearch_username: str = "elastic"
    elasticsearch_password: str = "PaiSmart2025"
    elasticsearch_ssl_verify: bool = False
    
    redis_url: str = "redis://8.137.79.174:6379/1"
    redis_password: Optional[str] = "redis123"
    
    rabbitmq_url: str = "amqp://zsr:123456@127.0.0.1:5672/"
    
    dashscope_api_key: str = "sk-da831ca6d01e4b8aaa04c1705d35f990"
    dashscope_chat_model: str = "qwen-plus"
    dashscope_embedding_model: str = "text-embedding-v4"
    
    ollama_base_url: str = "http://localhost:11434"
    ollama_chat_model: str = "deepseek-r1:1.5b"
    
    upload_chunk_size: int = 20971520
    upload_temp_dir: str = "E:/编程学习/项目/yv-ai/upload/temp"
    upload_final_dir: str = "E:/编程学习/项目/yv-ai/upload/files"
    
    parsing_chunk_size: int = 500
    parsing_overlap_size: int = 100
    
    secret_key: str = "your-secret-key-change-in-production"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 30
    
    quota_default_window_ms: int = 60000
    quota_default_max_tokens: int = 10000
    
    cors_origins: List[str] = [
        "http://localhost:5173",
        "http://localhost:3000",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:3000",
    ]
    
    log_file: str = "E:/编程学习/项目/yv-ai/logs/python-app.log"
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False


settings = Settings()
