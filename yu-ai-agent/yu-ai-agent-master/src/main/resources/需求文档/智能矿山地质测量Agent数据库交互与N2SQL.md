# 智能矿山地质测量Agent数据库交互与N2SQL实现指南

## 前言

在智能矿山建设浪潮中，AI Agent 正逐步成为地质、测量等专业岗位的“数字助手”。然而，Agent 要真正具备自主决策能力，必须能**安全、准确、高效地访问核心业务数据**。当前多数系统仍将数据查询硬编码于业务逻辑中，导致 Agent 灵活性差、维护成本高。

本书聚焦**矿山地质与测量**这一垂直领域，提供一套**端到端的数据库交互解决方案**：

- 设计贴合《煤矿测量规程》《固体矿产勘查规范》的**核心库表模型**
- 构建覆盖真实业务场景的**自然语言到SQL（N2SQL）映射体系**
- 封装符合 MCP 协议的**标准化工具接口**，供上层 Agent 调用

本指南可作为智能矿山系统开发者的**技术蓝图**，亦可为 AI 工程师提供**领域落地的最佳实践**。

- 设计贴合《煤矿测量规程》《固体矿产勘查规范》的**核心库表模型**
- 构建覆盖真实业务场景的**自然语言到SQL（N2SQL）映射体系**
- 封装符合 MCP 协议的**标准化工具接口**，供上层 Agent 调用

本指南可作为智能矿山系统开发者的**技术蓝图**，亦可为 AI 工程师提供**领域落地的最佳实践**。

------

## 第一章：矿山地质测量核心数据库设计

### 1.1 设计原则

- **聚焦专业核心**：围绕“一张图管理”与“三量动态核算”两大任务
- **贴合生产实际**：表结构源自地测科日常台账、图纸与报表
- **支持Agent交互**：字段命名清晰、外键关联明确、无冗余复杂性
- **可扩展性强**：预留空间几何、时序、向量等扩展字段

### 1.2 核心库表结构

#### 表1：矿区与煤层基础信息

```
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
```

#### 表2：钻孔数据（地质核心）

```
1-- 钻孔基本信息
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
```

#### 表3：巷道与采掘工程（测量核心）

```
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
```

#### 表4：储量与“三量”管理

```
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
```

#### 表5：地表移动观测

```
-- 观测站
CREATE TABLE surface_station (
    station_id VARCHAR(20) PRIMARY KEY,
    area_id VARCHAR(20) REFERENCES mine_area(area_id),
    x NUMERIC(12,3), y NUMERIC(12,3), z_initial NUMERIC(10,3)
);

-- 观测记录
CREATE TABLE subsidence_observation (
    obs_id SERIAL PRIMARY KEY,
    station_id VARCHAR(20) REFERENCES surface_station(station_id),
    obs_date DATE NOT NULL,
    z_current NUMERIC(10,3),
    subsidence NUMERIC(8,3) GENERATED ALWAYS AS (z_initial - z_current) STORED
);
15);
```

------

## 第二章：面向Agent的N2SQL需求设计

### 2.1 总体目标

> 实现“地质测量领域自然语言 ↔ 结构化 SQL 查询”的精准映射，使 AI Agent 能像专业地测工程师一样，通过对话方式获取所需数据。

### 2.2 核心应用场景

| Agent 角色         | 典型问题                               | 对应表                   |
| ------------------ | -------------------------------------- | ------------------------ |
| **储量管理 Agent** | “当前全矿开拓煤量是多少？”             | `three_quantities`       |
| **工程进度 Agent** | “列出所有已完成的准备巷道。”           | `roadway`                |
| **地质解释 Agent** | “ZK2025-001钻孔在3#煤层的厚度是多少？” | `seam_intercept`         |
| **安全监测 Agent** | “G1观测站最近一次的沉降量是多少？”     | `subsidence_observation` |

### 2.3 N2SQL 系统关键能力

1. **领域词典**：内置煤层名（`3#`）、巷道类型（`开拓巷道`）、钻孔前缀（`ZK`）等专业术语。
2. **Schema Linking**：在 Prompt 中注入表结构摘要，引导 LLM 正确映射字段。
3. **安全沙箱**：仅允许 `SELECT`；自动添加 `LIMIT 100`；字段白名单控制。
4. **上下文感知**：支持指代消解（如“它的设计回采率”中的“它”指代工作面）。
5. **错误反馈**：对模糊问题返回澄清请求，而非强行生成错误 SQL。

------

## 第三章：交付物：Prompt、测试用例与MCP工具

### 3.1 N2SQL 系统 Prompt 模板

```
你是一名矿山地质与测量领域的 AI 助手，能将用户的自然语言问题精准转换为 SQL 查询语句。
请严格遵守以下规则：

### 核心原则
1. **只使用下方提供的表和字段**，严禁虚构不存在的表或列。
2. **仅生成 SELECT 语句**，禁止 INSERT/UPDATE/DELETE/DROP 等操作。
3. **所有坐标单位为米（m），储量单位为吨（t），角度为度（°）**。
4. **日期格式统一为 'YYYY-MM-DD'**。
5. 若问题模糊或存在歧义，请返回 JSON：{"clarification_needed": true, "message": "请明确..."}。

### 数据库 Schema（仅限以下内容）
【矿区】mine_area(area_id, area_name)
【煤层】coal_seam(seam_id, seam_name, average_thickness, dip_angle)
【钻孔】borehole(hole_id, area_id, x, y, z, total_depth, drill_purpose, drill_date)
【岩性分层】lithology_log(hole_id, from_depth, to_depth, rock_type)
【见煤记录】seam_intercept(hole_id, seam_id, from_depth, to_depth, thickness)
【巷道】roadway(roadway_id, name, start_point, end_point, roadway_type, status)
【巷道点】roadway_point(point_id, x, y, z)
【工作面】mining_face(face_id, seam_id, start_date, end_date, status)
【储量块段】reserve_block(block_id, seam_id, block_type, recoverable_reserve, recovery_rate)
【月度产量】monthly_production(block_id, report_month, mined_tonnage, actual_recovery_rate)
【三量台账】three_quantities(calc_date, development_reserve, preparation_reserve, mining_reserve)
【地表观测】subsidence_observation(station_id, obs_date, subsidence)

### 输出格式
- 若可生成 SQL：直接输出纯 SQL 语句，不要解释。
- 若需澄清：输出标准 JSON。

### 示例
用户：3#煤层当前可采储量是多少？
SQL：SELECT SUM(recoverable_reserve) FROM reserve_block WHERE seam_id = '3#' AND block_type = '采区';

用户：上个月采了多少煤？
JSON：{"clarification_needed": true, "message": "请指定是哪个工作面或采区？例如“1301工作面”或“全矿”。"}
```

### 3.2 测试用例集（节选）

| 自然语言                             | 期望 SQL                                                     |
| ------------------------------------ | ------------------------------------------------------------ |
| 当前全矿开拓煤量是多少？             | `SELECT development_reserve FROM three_quantities WHERE area_id = 'M01' ORDER BY calc_date DESC LIMIT 1;` |
| 列出所有已完成的准备巷道。           | `SELECT name, roadway_id FROM roadway WHERE roadway_type = '准备巷道' AND status = 'completed';` |
| ZK2025-001钻孔在3#煤层的厚度是多少？ | `SELECT thickness FROM seam_intercept WHERE hole_id = 'ZK2025-001' AND seam_id = '3#';` |
| G1观测站最近一次的沉降量是多少？     | `SELECT subsidence FROM subsidence_observation WHERE station_id = 'G1' ORDER BY obs_date DESC LIMIT 1;` |

> **完整20条用例见附录A**

### 3.3 MCP 工具接口定义

**工具名称**：`query_geological_data`
 **描述**：根据自然语言问题，查询矿山地质与测量数据库，返回结构化结果。

**参数（JSON Schema）**：

```
{
  "type": "object",
  "properties": {
    "question": {
      "type": "string",
      "description": "用户提出的自然语言问题"
    }
  },
  "required": ["question"]
}
```

**返回值**：

- 成功：`{ "status": "success", "data": [...] }`
- 需澄清：`{ "status": "clarification_needed", "message": "..." }`
- 错误：`{ "status": "error", "reason": "..." }`

**调用示例**：

```
{
  "tool_name": "query_geological_data",
  "arguments": {
    "question": "3#煤层当前可采储量是多少？"
  }
}
```

------

## 第四章：实施路线图

| 阶段        | 目标              | 交付物                             |
| ----------- | ----------------- | ---------------------------------- |
| **Phase 1** | 支撑基础问答      | N2SQL 引擎 + 20个高频查询模板      |
| **Phase 2** | 支持多轮对话      | 指代消解 + 上下文记忆 + 错误恢复   |
| **Phase 3** | 集成到Agent工作流 | MCP 工具封装 + LangGraph/Dify 接入 |

> **最终价值**：让 AI Agent 成为地测工程师的“数字助手”——它负责查数据，人负责做判断。

------

## 附录A：完整N2SQL测试用例集

```
"当前全矿开拓煤量是多少？","SELECT development_reserve FROM three_quantities WHERE area_id = 'M01' ORDER BY calc_date DESC LIMIT 1;"
"3#煤层的可采储量还有多少？","SELECT SUM(recoverable_reserve) FROM reserve_block WHERE seam_id = '3#' AND block_type = '采区';"
"1301工作面上个月的实际回采率是多少？","SELECT actual_recovery_rate FROM monthly_production WHERE block_id = '1301工作面' AND report_month = '2025-05-01';"
"列出所有已完成的准备巷道。","SELECT name, roadway_id FROM roadway WHERE roadway_type = '准备巷道' AND status = 'completed';"
"+1200运输大巷的起点坐标是多少？","SELECT p.x, p.y, p.z FROM roadway r JOIN roadway_point p ON r.start_point = p.point_id WHERE r.name = '+1200运输大巷';"
"ZK2025-001钻孔在3#煤层的厚度是多少？","SELECT thickness FROM seam_intercept WHERE hole_id = 'ZK2025-001' AND seam_id = '3#';"
"找出所有见煤深度大于200米的钻孔。","SELECT DISTINCT hole_id FROM seam_intercept WHERE from_depth > 200;"
"列出ZK2025-001钻孔从100m到150m之间的岩性。","SELECT rock_type FROM lithology_log WHERE hole_id = 'ZK2025-001' AND from_depth >= 100 AND to_depth <= 150;"
"G1观测站最近一次的沉降量是多少？","SELECT subsidence FROM subsidence_observation WHERE station_id = 'G1' ORDER BY obs_date DESC LIMIT 1;"
"6月份所有观测站的最大沉降量是多少？","SELECT MAX(subsidence) FROM subsidence_observation WHERE EXTRACT(MONTH FROM obs_date) = 6 AND EXTRACT(YEAR FROM obs_date) = 2025;"
"15-1#煤层的平均倾角是多少？","SELECT dip_angle FROM coal_seam WHERE seam_id = '15-1#';"
"13采区的地质储量是多少？","SELECT geological_reserve FROM reserve_block WHERE block_id = '13采区';"
"哪些工作面正在开采？","SELECT face_id, seam_id FROM mining_face WHERE status = 'active';"
"ZK2025-001钻孔的孔口高程是多少？","SELECT z FROM borehole WHERE hole_id = 'ZK2025-001';"
"全矿回采煤量趋势（近3个月）","SELECT calc_date, mining_reserve FROM three_quantities WHERE area_id = 'M01' ORDER BY calc_date DESC LIMIT 3;"
"1301工作面的设计回采率是多少？","SELECT recovery_rate FROM reserve_block WHERE block_id = '1301工作面';"
"列出所有勘探目的的钻孔。","SELECT hole_id, drill_date FROM borehole WHERE drill_purpose = '勘探';"
"细砂岩出现在哪些钻孔中？","SELECT DISTINCT hole_id FROM lithology_log WHERE rock_type = '细砂岩';"
"上个月全矿采出量合计？","SELECT SUM(mined_tonnage) FROM monthly_production WHERE report_month = '2025-05-01';"
"本月新验收的巷道有哪些？","SELECT name FROM roadway WHERE status = 'completed'; -- 注：需扩展验收日期字段"
```