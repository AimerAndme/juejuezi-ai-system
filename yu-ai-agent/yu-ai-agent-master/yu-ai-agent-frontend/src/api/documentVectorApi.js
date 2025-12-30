import axios from 'axios'

// 根据环境变量设置 API 基础 URL
const API_BASE_URL =
  process.env.NODE_ENV === 'production' ? '/api' : 'http://localhost:8123/api'

// 创建 axios 实例
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
})

/**
 * 查询用户所有向量
 */
export const getAllVectorsByUser = (userId) => {
  return request.get('/vectors/manage/list', {
    params: { userId },
  })
}

/**
 * 按文件查询文档向量列表
 */
export const getVectorsByFile = (fileMd5, userId) => {
  return request.get('/vectors/manage/list/file', {
    params: { fileMd5, userId },
  })
}

/**
 * 删除单个向量
 */
export const deleteVector = (vectorId, userId) => {
  return request.delete(`/vectors/manage/${vectorId}`, {
    params: { userId },
  })
}

/**
 * 删除某文件的全部向量（可选）
 */
export const deleteVectorsByFile = (fileMd5, userId) => {
  return request.delete(`/vectors/manage/file/${fileMd5}`, {
    params: { userId },
  })
}
