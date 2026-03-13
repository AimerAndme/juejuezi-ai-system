"""
矿山数据路由
对应Java版本的各类矿山数据Controller
"""

from typing import Optional, List
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.core.database import get_db
from app.schemas.base import ResponseModel, PageResponse
from app.models.mine import (
    Borehole, CoalSeam, MineArea, MiningFace, Roadway,
    RoadwayPoint, SeamIntercept, SubsidenceObservation,
    SurfaceStation, ThreeQuantities, LithologyLog, ReserveBlock, MonthlyProduction
)
from app.models.conversation import MiningAgentUser

router = APIRouter()


@router.get("/user/list", response_model=ResponseModel)
async def get_user_list(
    db: AsyncSession = Depends(get_db)
):
    """获取用户列表"""
    result = await db.execute(select(MiningAgentUser))
    users = result.scalars().all()
    
    user_list = [
        {
            "userId": u.user_id,
            "account": u.account,
            "userName": u.user_name,
            "userRole": u.user_role,
            "phone": u.phone,
        }
        for u in users
    ]
    
    return ResponseModel.success(user_list)


@router.get("/area/list", response_model=ResponseModel)
async def get_mine_area_list(
    db: AsyncSession = Depends(get_db)
):
    """获取矿区列表"""
    result = await db.execute(select(MineArea))
    areas = result.scalars().all()
    
    area_list = [
        {
            "areaId": a.area_id,
            "areaName": a.area_name,
            "location": a.location,
            "areaSize": float(a.area_size) if a.area_size else 0,
            "status": a.status,
        }
        for a in areas
    ]
    
    return ResponseModel.success(area_list)


@router.get("/borehole/list", response_model=ResponseModel)
async def get_borehole_list(
    area_id: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    """获取钻孔列表"""
    query = select(Borehole)
    if area_id:
        query = query.where(Borehole.area_id == area_id)
    
    result = await db.execute(query)
    boreholes = result.scalars().all()
    
    borehole_list = [
        {
            "holeId": b.hole_id,
            "areaId": b.area_id,
            "x": float(b.x) if b.x else None,
            "y": float(b.y) if b.y else None,
            "z": float(b.z) if b.z else None,
            "totalDepth": b.total_depth,
            "drillPurpose": b.drill_purpose,
            "drillDate": b.drill_date.isoformat() if b.drill_date else None,
            "status": b.status,
        }
        for b in boreholes
    ]
    
    return ResponseModel.success(borehole_list)


@router.get("/coal_seam/list", response_model=ResponseModel)
async def get_coal_seam_list(
    db: AsyncSession = Depends(get_db)
):
    """获取煤层列表"""
    result = await db.execute(select(CoalSeam))
    seams = result.scalars().all()
    
    seam_list = [
        {
            "seamId": s.seam_id,
            "seamName": s.seam_name,
            "averageThickness": float(s.average_thickness) if s.average_thickness else None,
            "dipAngle": float(s.dip_angle) if s.dip_angle else None,
            "roofLithology": s.roof_lithology,
            "floorLithology": s.floor_lithology,
        }
        for s in seams
    ]
    
    return ResponseModel.success(seam_list)


@router.get("/roadway/list", response_model=ResponseModel)
async def get_roadway_list(
    area_id: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    """获取巷道列表"""
    query = select(Roadway)
    if area_id:
        query = query.where(Roadway.area_id == area_id)
    
    result = await db.execute(query)
    roadways = result.scalars().all()
    
    roadway_list = [
        {
            "roadwayId": r.roadway_id,
            "areaId": r.area_id,
            "roadwayName": r.roadway_name,
            "roadwayType": r.roadway_type,
            "length": float(r.length) if r.length else None,
            "status": r.status,
        }
        for r in roadways
    ]
    
    return ResponseModel.success(roadway_list)


@router.get("/mining_face/list", response_model=ResponseModel)
async def get_mining_face_list(
    area_id: Optional[str] = Query(None),
    seam_id: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    """获取采煤工作面列表"""
    query = select(MiningFace)
    if area_id:
        query = query.where(MiningFace.area_id == area_id)
    if seam_id:
        query = query.where(MiningFace.seam_id == seam_id)
    
    result = await db.execute(query)
    faces = result.scalars().all()
    
    face_list = [
        {
            "faceId": f.face_id,
            "areaId": f.area_id,
            "seamId": f.seam_id,
            "faceName": f.face_name,
            "startDate": f.start_date.isoformat() if f.start_date else None,
            "endDate": f.end_date.isoformat() if f.end_date else None,
            "status": f.status,
        }
        for f in faces
    ]
    
    return ResponseModel.success(face_list)


@router.get("/surface_station/list", response_model=ResponseModel)
async def get_surface_station_list(
    area_id: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    """获取地面站列表"""
    query = select(SurfaceStation)
    if area_id:
        query = query.where(SurfaceStation.area_id == area_id)
    
    result = await db.execute(query)
    stations = result.scalars().all()
    
    station_list = [
        {
            "stationId": s.station_id,
            "areaId": s.area_id,
            "stationName": s.station_name,
            "x": float(s.x) if s.x else None,
            "y": float(s.y) if s.y else None,
            "z": float(s.z) if s.z else None,
        }
        for s in stations
    ]
    
    return ResponseModel.success(station_list)


@router.get("/three_quantities/list", response_model=ResponseModel)
async def get_three_quantities_list(
    area_id: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    """获取三量数据列表"""
    query = select(ThreeQuantities)
    if area_id:
        query = query.where(ThreeQuantities.area_id == area_id)
    
    result = await db.execute(query)
    quantities = result.scalars().all()
    
    quantity_list = [
        {
            "quantityId": q.quantity_id,
            "areaId": q.area_id,
            "quantityType": q.quantity_type,
            "quantityAmount": float(q.quantity_amount) if q.quantity_amount else None,
            "unit": q.unit,
        }
        for q in quantities
    ]
    
    return ResponseModel.success(quantity_list)


@router.get("/monthly_production/list", response_model=ResponseModel)
async def get_monthly_production_list(
    area_id: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    """获取月度产量列表"""
    query = select(MonthlyProduction)
    if area_id:
        query = query.where(MonthlyProduction.area_id == area_id)
    
    result = await db.execute(query)
    productions = result.scalars().all()
    
    production_list = [
        {
            "productionId": p.production_id,
            "areaId": p.area_id,
            "yearMonth": p.year_month,
            "productionAmount": float(p.production_amount) if p.production_amount else None,
            "unit": p.unit,
        }
        for p in productions
    ]
    
    return ResponseModel.success(production_list)


@router.get("/reserve_block/list", response_model=ResponseModel)
async def get_reserve_block_list(
    area_id: Optional[str] = Query(None),
    seam_id: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db)
):
    """获取储量块段列表"""
    query = select(ReserveBlock)
    if area_id:
        query = query.where(ReserveBlock.area_id == area_id)
    if seam_id:
        query = query.where(ReserveBlock.seam_id == seam_id)
    
    result = await db.execute(query)
    blocks = result.scalars().all()
    
    block_list = [
        {
            "blockId": b.block_id,
            "areaId": b.area_id,
            "seamId": b.seam_id,
            "reserveAmount": float(b.reserve_amount) if b.reserve_amount else None,
            "unit": b.unit,
        }
        for b in blocks
    ]
    
    return ResponseModel.success(block_list)
