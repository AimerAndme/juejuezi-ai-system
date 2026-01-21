import axios from 'axios'

const API_BASE_URL =
  process.env.NODE_ENV === 'production' ? '/api' : 'http://localhost:8123/api'

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
})

// 通用CRUD工厂函数
const createApi = (endpoint) => ({
  getAll: (page, size) => {
    if (page && size) {
      return request.get(`/data/${endpoint}`, { params: { page, size } })
    }
    return request.get(`/data/${endpoint}`)
  },
  getById: (id) => request.get(`/data/${endpoint}/${id}`),
  add: (data) => request.post(`/data/${endpoint}`, data),
  update: (data) => request.put(`/data/${endpoint}`, data),
  delete: (id) => request.delete(`/data/${endpoint}/${id}`),
})

// 矿区基本信息
export const mineAreaApi = createApi('mine-area')

// 煤层信息
export const coalSeamApi = createApi('coal-seam')

// 钻孔基本信息
export const boreholeApi = {
  ...createApi('borehole'),
  getByAreaId: (areaId, page, size) => {
    if (page && size) {
      return request.get(`/data/borehole/by-area/${areaId}`, {
        params: { page, size },
      })
    }
    return request.get(`/data/borehole/by-area/${areaId}`)
  },
}

// 岩性分层
export const lithologyLogApi = {
  ...createApi('lithology-log'),
  getByHoleId: (holeId) => request.get(`/data/lithology-log/by-hole/${holeId}`),
}

// 见煤记录
export const seamInterceptApi = {
  ...createApi('seam-intercept'),
  getByHoleId: (holeId) =>
    request.get(`/data/seam-intercept/by-hole/${holeId}`),
}

// 巷道中心线点
export const roadwayPointApi = {
  ...createApi('roadway-point'),
  getByAreaId: (areaId) => request.get(`/data/roadway-point/by-area/${areaId}`),
}

// 巷道基本信息
export const roadwayApi = {
  ...createApi('roadway'),
  getByAreaId: (areaId) => request.get(`/data/roadway/by-area/${areaId}`),
}

// 采煤工作面
export const miningFaceApi = {
  ...createApi('mining-face'),
  getByAreaId: (areaId) => request.get(`/data/mining-face/by-area/${areaId}`),
}

// 资源储量块段
export const reserveBlockApi = {
  ...createApi('reserve-block'),
  getByAreaId: (areaId) => request.get(`/data/reserve-block/by-area/${areaId}`),
}

// 月度采出量统计
export const monthlyProductionApi = {
  ...createApi('monthly-production'),
  getByBlockId: (blockId) =>
    request.get(`/data/monthly-production/by-block/${blockId}`),
}

// 三量动态台账
export const threeQuantitiesApi = {
  ...createApi('three-quantities'),
  getByAreaId: (areaId) =>
    request.get(`/data/three-quantities/by-area/${areaId}`),
}

// 观测站
export const surfaceStationApi = {
  ...createApi('surface-station'),
  getByAreaId: (areaId) =>
    request.get(`/data/surface-station/by-area/${areaId}`),
}

// 沉降观测
export const subsidenceObservationApi = {
  ...createApi('subsidence-observation'),
  getByStationId: (stationId) =>
    request.get(`/data/subsidence-observation/by-station/${stationId}`),
}
