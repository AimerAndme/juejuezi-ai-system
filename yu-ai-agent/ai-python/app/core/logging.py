"""
日志配置模块
使用loguru进行结构化日志记录
"""

import sys
import json
from datetime import datetime
from pathlib import Path
from loguru import logger

from app.core.config import settings


def setup_logging():
    """配置日志系统"""
    
    # 移除默认的logger
    logger.remove()
    
    # 控制台日志格式
    console_format = (
        "<green>{time:YYYY-MM-DD HH:mm:ss}</green> | "
        "<level>{level: <8}</level> | "
        "<cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> | "
        "<level>{message}</level>"
    )
    
    # 文件日志格式（JSON格式，便于日志分析）
    file_format = {
        "time": "{time}",
        "level": "{level}",
        "name": "{name}",
        "function": "{function}",
        "line": "{line}",
        "message": "{message}",
        "extra": "{extra}",
    }
    
    # 添加控制台输出
    logger.add(
        sys.stderr,
        format=console_format,
        level=settings.log_level,
        colorize=True,
    )
    
    # 添加文件输出
    if settings.log_file:
        log_path = Path(settings.log_file)
        log_path.parent.mkdir(parents=True, exist_ok=True)
        
        # 结构化日志文件（JSON格式）
        logger.add(
            str(log_path),
            format=lambda record: json.dumps({
                "timestamp": datetime.now().isoformat(),
                "level": record["level"].name,
                "logger": record["name"],
                "function": record["function"],
                "line": record["line"],
                "message": record["message"],
                "extra": record["extra"] if record["extra"] else {},
            }),
            level=settings.log_level,
            rotation="10 MB",  # 按大小轮转
            retention="30 days",  # 保留30天
            compression="zip",  # 压缩旧日志
            serialize=False,  # 我们已经手动序列化为JSON
        )
    
    # 将loguru的logger设置为标准logger的替代
    # 这样其他库可以使用标准logging，但会被loguru处理
    class InterceptHandler:
        def write(self, message):
            if message.strip():
                logger.info(message.strip())
        
        def flush(self):
            pass
    
    # 替换标准输出和错误输出
    sys.stdout = InterceptHandler()
    sys.stderr = InterceptHandler()
    
    logger.info(f"日志系统初始化完成，级别: {settings.log_level}")
    if settings.log_file:
        logger.info(f"日志文件: {settings.log_file}")


# 创建模块级别的logger实例
log = logger


def get_logger(name: str = __name__):
    """获取指定名称的logger"""
    return logger.bind(name=name)