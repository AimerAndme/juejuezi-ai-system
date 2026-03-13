"""
矿山数据模型
对应Java版本的矿山相关实体类
"""

from decimal import Decimal
from sqlalchemy import Column, String, Integer, Numeric, Date, Text, Boolean, Float
from sqlalchemy.sql import func
from app.models.base import BaseModel


class Borehole(BaseModel):
    """钻孔基本信息"""
    __tablename__ = "borehole"
    
    hole_id = Column(String(64), primary_key=True)  # 钻孔编号
    area_id = Column(String(64), nullable=False, index=True)  # 矿区编码
    x = Column(Numeric(12, 4))  # X坐标
    y = Column(Numeric(12, 4))  # Y坐标
    z = Column(Numeric(12, 4))  # Z坐标
    total_depth = Column(Float)  # 总深度
    drill_purpose = Column(String(255))  # 钻探目的
    drill_date = Column(Date)  # 钻探日期
    status = Column(String(32))  # 状态


class CoalSeam(BaseModel):
    """煤层信息"""
    __tablename__ = "coal_seam"
    
    seam_id = Column(String(64), primary_key=True)  # 煤层编号
    seam_name = Column(String(128), nullable=False)  # 煤层名称
    average_thickness = Column(Numeric(8, 2))  # 平均厚度（m）
    dip_angle = Column(Numeric(5, 1))  # 平均倾角（°）
    roof_lithology = Column(String(255))  # 顶板岩性
    floor_lithology = Column(String(255))  # 底板岩性


class ColumnMetadata(BaseModel):
    """列元数据"""
    __tablename__ = "column_metadata"
    
    column_id = Column(String(64), primary_key=True)
    table_name = Column(String(64), nullable=False, index=True)
    column_name = Column(String(64), nullable=False)
    data_type = Column(String(32))
    description = Column(Text)


class LithologyLog(BaseModel):
    """岩性日志"""
    __tablename__ = "lithology_log"
    
    log_id = Column(String(64), primary_key=True)
    hole_id = Column(String(64), nullable=False, index=True)
    depth_from = Column(Numeric(8, 2))  # 起始深度
    depth_to = Column(Numeric(8, 2))  # 结束深度
    lithology = Column(String(128))  # 岩性
    description = Column(Text)  # 描述


class MineArea(BaseModel):
    """矿区信息"""
    __tablename__ = "mine_area"
    
    area_id = Column(String(64), primary_key=True)
    area_name = Column(String(128), nullable=False)
    location = Column(String(255))
    area_size = Column(Numeric(12, 2))  # 矿区面积
    status = Column(String(32))


class MiningFace(BaseModel):
    """采矿工作面"""
    __tablename__ = "mining_face"
    
    face_id = Column(String(64), primary_key=True)
    area_id = Column(String(64), nullable=False, index=True)
    seam_id = Column(String(64), nullable=False, index=True)
    face_name = Column(String(128), nullable=False)
    start_date = Column(Date)
    end_date = Column(Date)
    status = Column(String(32))


class MonthlyProduction(BaseModel):
    """月度产量"""
    __tablename__ = "monthly_production"
    
    production_id = Column(String(64), primary_key=True)
    area_id = Column(String(64), nullable=False, index=True)
    year_month = Column(String(7), nullable=False)  # YYYY-MM
    production_amount = Column(Numeric(12, 2))  # 产量
    unit = Column(String(16), default="吨")  # 单位


class ReserveBlock(BaseModel):
    """储量块段"""
    __tablename__ = "reserve_block"
    
    block_id = Column(String(64), primary_key=True)
    area_id = Column(String(64), nullable=False, index=True)
    seam_id = Column(String(64), nullable=False, index=True)
    reserve_amount = Column(Numeric(12, 2))  # 储量
    unit = Column(String(16), default="吨")  # 单位


class Roadway(BaseModel):
    """巷道"""
    __tablename__ = "roadway"
    
    roadway_id = Column(String(64), primary_key=True)
    area_id = Column(String(64), nullable=False, index=True)
    roadway_name = Column(String(128), nullable=False)
    roadway_type = Column(String(32))  # 巷道类型
    length = Column(Numeric(8, 2))  # 长度
    status = Column(String(32))


class RoadwayPoint(BaseModel):
    """巷道测点"""
    __tablename__ = "roadway_point"
    
    point_id = Column(String(64), primary_key=True)
    roadway_id = Column(String(64), nullable=False, index=True)
    point_index = Column(Integer)  # 测点序号
    x = Column(Numeric(12, 4))  # X坐标
    y = Column(Numeric(12, 4))  # Y坐标
    z = Column(Numeric(12, 4))  # Z坐标


class SeamIntercept(BaseModel):
    """煤层揭露点"""
    __tablename__ = "seam_intercept"
    
    intercept_id = Column(String(64), primary_key=True)
    hole_id = Column(String(64), nullable=False, index=True)
    seam_id = Column(String(64), nullable=False, index=True)
    depth = Column(Numeric(8, 2))  # 揭露深度
    thickness = Column(Numeric(6, 2))  # 揭露厚度


class SubsidenceObservation(BaseModel):
    """沉降观测点"""
    __tablename__ = "subsidence_observation"
    
    observation_id = Column(String(64), primary_key=True)
    area_id = Column(String(64), nullable=False, index=True)
    observation_date = Column(Date)
    subsidence_amount = Column(Numeric(8, 4))  # 沉降量
    observation_point = Column(String(64))  # 观测点编号


class SurfaceStation(BaseModel):
    """地面站"""
    __tablename__ = "surface_station"
    
    station_id = Column(String(64), primary_key=True)
    area_id = Column(String(64), nullable=False, index=True)
    station_name = Column(String(128), nullable=False)
    x = Column(Numeric(12, 4))  # X坐标
    y = Column(Numeric(12, 4))  # Y坐标
    z = Column(Numeric(12, 4))  # Z坐标


class ThreeQuantities(BaseModel):
    """三量数据（开拓煤量、准备煤量、回采煤量）"""
    __tablename__ = "three_quantities"
    
    quantity_id = Column(String(64), primary_key=True)
    area_id = Column(String(64), nullable=False, index=True)
    quantity_type = Column(String(32))  # 煤量类型
    quantity_amount = Column(Numeric(12, 2))  # 煤量
    unit = Column(String(16), default="吨")  # 单位