-- 矿区基本信息
CREATE TABLE mine_area (
    area_id VARCHAR(20) PRIMARY KEY,     -- 矿区编码（如 M01）
    area_name VARCHAR(100) NOT NULL,
    coordinate_system VARCHAR(50),       -- 坐标系
    datum_height NUMERIC(10,3)           -- 高程基准（m）
);

-- 煤层信息
CREATE TABLE coal_seam (
    seam_id VARCHAR(20) PRIMARY KEY,     -- 煤层编号（如 3#）
    seam_name VARCHAR(50),
    average_thickness NUMERIC(6,2),      -- 平均厚度（m）
    dip_angle NUMERIC(5,2),              -- 平均倾角（°）
    roof_lithology VARCHAR(100),         -- 顶板岩性
    floor_lithology VARCHAR(100)         -- 底板岩性
);

-- 钻孔基本信息
-- 钻孔基本信息
CREATE TABLE borehole (
    hole_id VARCHAR(30) PRIMARY KEY,     -- 钻孔编号（如 ZK2025-001）
    area_id VARCHAR(20) REFERENCES mine_area(area_id),
    x NUMERIC(12,3), y NUMERIC(12,3), z NUMERIC(10,3),
    total_depth NUMERIC(8,2) NOT NULL,
    drill_purpose VARCHAR(20),           -- 钻探目的
    drill_date DATE,
    status VARCHAR(10) DEFAULT 'valid'
);

-- 岩性分层
CREATE TABLE lithology_log (
    log_id SERIAL PRIMARY KEY,
    hole_id VARCHAR(30) REFERENCES borehole(hole_id) ON DELETE CASCADE,
    from_depth NUMERIC(8,2),
    to_depth NUMERIC(8,2),
    rock_type VARCHAR(50)
);

-- 见煤记录
CREATE TABLE seam_intercept (
    intercept_id SERIAL PRIMARY KEY,
    hole_id VARCHAR(30) REFERENCES borehole(hole_id) ON DELETE CASCADE,
    seam_id VARCHAR(20) REFERENCES coal_seam(seam_id),
    from_depth NUMERIC(8,2),
    to_depth NUMERIC(8,2),
    thickness NUMERIC(6,2)               -- 真厚度（m）
);
-- 巷道中心线点
CREATE TABLE roadway_point (
    point_id VARCHAR(30) PRIMARY KEY,    -- 点号（如 A101）
    area_id VARCHAR(20) REFERENCES mine_area(area_id),
    x NUMERIC(12,3), y NUMERIC(12,3), z NUMERIC(10,3),
    point_type VARCHAR(20)
);

-- 巷道基本信息
CREATE TABLE roadway (
    roadway_id VARCHAR(30) PRIMARY KEY,
    area_id VARCHAR(20) REFERENCES mine_area(area_id),
    name VARCHAR(100) NOT NULL,
    start_point VARCHAR(30) REFERENCES roadway_point(point_id),
    end_point VARCHAR(30) REFERENCES roadway_point(point_id),
    roadway_type VARCHAR(30),            -- 类型：开拓/准备/回采
    cross_section NUMERIC(6,2),
    status VARCHAR(20) DEFAULT 'design'
);

-- 采煤工作面
CREATE TABLE mining_face (
    face_id VARCHAR(30) PRIMARY KEY,
    area_id VARCHAR(20) REFERENCES mine_area(area_id),
    seam_id VARCHAR(20) REFERENCES coal_seam(seam_id),
    start_date DATE,
    end_date DATE,
    length NUMERIC(8,2),
    status VARCHAR(20) DEFAULT 'active'
);
-- 资源储量块段
CREATE TABLE reserve_block (
    block_id VARCHAR(30) PRIMARY KEY,
    area_id VARCHAR(20) REFERENCES mine_area(area_id),
    seam_id VARCHAR(20) REFERENCES coal_seam(seam_id),
    block_type VARCHAR(20) NOT NULL,     -- 采区 / 工作面
    geological_reserve NUMERIC(12,2),
    recoverable_reserve NUMERIC(12,2),
    recovery_rate NUMERIC(5,2)           -- 设计回采率（%）
);

-- 月度采出量统计
CREATE TABLE monthly_production (
    record_id SERIAL PRIMARY KEY,
    block_id VARCHAR(30) REFERENCES reserve_block(block_id),
    report_month DATE NOT NULL,
    mined_tonnage NUMERIC(12,2),
    loss_tonnage NUMERIC(12,2),
    actual_recovery_rate NUMERIC(5,2)
);

-- “三量”动态台账
CREATE TABLE three_quantities (
    record_id SERIAL PRIMARY KEY,
    area_id VARCHAR(20) REFERENCES mine_area(area_id),
    calc_date DATE NOT NULL,
    development_reserve NUMERIC(12,2),   -- 开拓煤量
    preparation_reserve NUMERIC(12,2),   -- 准备煤量
    mining_reserve NUMERIC(12,2)         -- 回采煤量
);
-- 观测站
CREATE TABLE surface_station (
    station_id VARCHAR(20) PRIMARY KEY,
    area_id VARCHAR(20) REFERENCES mine_area(area_id),
    x NUMERIC(12,3), y NUMERIC(12,3), z_initial NUMERIC(10,3)
);


-- 2. 再创建 subsidence_observation 表
CREATE TABLE subsidence_observation (
    obs_id SERIAL PRIMARY KEY,
    station_id VARCHAR(20) NOT NULL REFERENCES surface_station(station_id) ON DELETE CASCADE,
    obs_date DATE NOT NULL,
    z_initial NUMERIC(10,3) NOT NULL,
    z_current NUMERIC(10,3) NOT NULL,
    subsidence NUMERIC(8,3) GENERATED ALWAYS AS (z_initial - z_current) STORED,
    UNIQUE (station_id, obs_date)
);
