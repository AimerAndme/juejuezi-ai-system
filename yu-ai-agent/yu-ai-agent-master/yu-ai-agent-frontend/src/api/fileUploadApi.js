import axios from 'axios'

// 根据环境变量设置 API 基础 URL
const API_BASE_URL =
  process.env.NODE_ENV === 'production' ? '/api' : 'http://localhost:8123/api'

// 创建axios实例
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 300000, // 5分钟超时，适配大文件上传
})

/**
 * 初始化文件上传
 */
export const initiateUpload = (data) => {
  return request.post('/files/upload/initiate', data)
}

/**
 * 上传文件分片
 */
export const uploadChunk = (formData, onUploadProgress) => {
  return request.post('/files/upload/chunk', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress,
  })
}

/**
 * 查询上传状态
 */
export const getUploadStatus = (fileMd5, userId) => {
  return request.get(`/files/upload/status/${fileMd5}`, {
    params: { userId },
  })
}

/**
 * 完成上传（合并分片）
 */
export const completeUpload = (data) => {
  return request.post('/files/upload/complete', data)
}

export default {
  initiateUpload,
  uploadChunk,
  getUploadStatus,
  completeUpload,
}
