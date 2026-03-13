"""
数据模型定义
对应Java版本的Entity类
"""

from .base import BaseModel
from .document import DocumentVector, FileUpload, FileExtractedImages, ChunkInfo
from .conversation import MiningAgentConversation, MiningAgentConversationMsg, MiningAgentUser
from .mine import (
    Borehole, CoalSeam, ColumnMetadata, LithologyLog, MineArea,
    MiningFace, MonthlyProduction, ReserveBlock, Roadway,
    RoadwayPoint, SeamIntercept, SubsidenceObservation,
    SurfaceStation, ThreeQuantities
)
from .quota import UserQuota

__all__ = [
    "BaseModel",
    "DocumentVector",
    "FileUpload", 
    "FileExtractedImages",
    "ChunkInfo",
    "MiningAgentConversation",
    "MiningAgentConversationMsg",
    "MiningAgentUser",
    "Borehole",
    "CoalSeam",
    "ColumnMetadata",
    "LithologyLog",
    "MineArea",
    "MiningFace",
    "MonthlyProduction",
    "ReserveBlock",
    "Roadway",
    "RoadwayPoint",
    "SeamIntercept",
    "SubsidenceObservation",
    "SurfaceStation",
    "ThreeQuantities",
    "UserQuota",
]