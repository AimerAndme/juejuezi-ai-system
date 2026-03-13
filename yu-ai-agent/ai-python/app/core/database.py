"""
数据库连接和会话管理
使用PostgreSQL数据库 + pg8000驱动
"""

from typing import AsyncGenerator
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine, async_sessionmaker
from sqlalchemy.orm import declarative_base
from app.core.config import settings

# 使用pg8000驱动连接PostgreSQL
database_url = settings.database_url.replace("postgresql://", "postgresql+pg8000://")

engine = create_async_engine(
    database_url,
    echo=settings.debug,
    future=True,
    pool_size=settings.database_pool_size,
    max_overflow=settings.database_max_overflow,
    pool_pre_ping=True,
)

AsyncSessionLocal = async_sessionmaker(
    engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autoflush=False,
)

Base = declarative_base()


async def get_db() -> AsyncGenerator[AsyncSession, None]:
    """获取数据库会话"""
    async with AsyncSessionLocal() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()


async def init_db() -> None:
    """初始化数据库，创建所有表"""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    print("数据库表创建完成")


async def close_db() -> None:
    """关闭数据库连接"""
    await engine.dispose()
    print("数据库连接已关闭")
