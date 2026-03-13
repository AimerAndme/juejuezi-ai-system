"""
数据库初始化脚本
创建所有必要的表
"""

import asyncio
from sqlalchemy import text
from app.core.database import engine, Base
from app.models import (
    BaseModel, DocumentVector, FileUpload, FileExtractedImages, ChunkInfo,
    MiningAgentConversation, MiningAgentConversationMsg, MiningAgentUser,
    Borehole, CoalSeam, ColumnMetadata, LithologyLog, MineArea,
    MiningFace, MonthlyProduction, ReserveBlock, Roadway,
    RoadwayPoint, SeamIntercept, SubsidenceObservation,
    SurfaceStation, ThreeQuantities, UserQuota
)
from app.core.logging import get_logger

logger = get_logger(__name__)


async def create_tables():
    """创建所有表"""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    logger.info("所有表创建成功")


async def drop_tables():
    """删除所有表"""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)
    logger.info("所有表删除成功")


async def init_sample_data():
    """初始化示例数据"""
    from app.core.database import AsyncSessionLocal
    from app.models.conversation import MiningAgentUser
    from app.core.security import hash_password
    
    async with AsyncSessionLocal() as session:
        test_user = MiningAgentUser(
            user_id="test001",
            account="test",
            password=hash_password("test123"),
            user_name="测试用户",
            user_role="user",
        )
        session.add(test_user)
        await session.commit()
    
    logger.info("示例数据初始化成功")


if __name__ == "__main__":
    import sys
    
    if len(sys.argv) > 1:
        command = sys.argv[1]
        if command == "create":
            asyncio.run(create_tables())
        elif command == "drop":
            asyncio.run(drop_tables())
        elif command == "init":
            asyncio.run(create_tables())
            asyncio.run(init_sample_data())
        else:
            print(f"未知命令: {command}")
    else:
        print("用法: python -m app.scripts.init_db create|drop|init")
