import axios from 'axios'

// 根据环境变量设置 API 基础 URL
const API_BASE_URL =
  process.env.NODE_ENV === 'production' ? '/api' : 'http://localhost:8123/api'

// 创建axios实例
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
})

/**
 * 获取文件列表
 */
export const getFileList = (userId) => {
  return request.get('/files/manage/list', {
    params: { userId },
  })
}

/**
 * 获取所有文件列表（管理员）
 */
export const getAllFileList = () => {
  return request.get('/files/manage/list/all')
}

/**
 * 删除文件
 */
export const deleteFile = (fileMd5, userId) => {
  return request.delete(`/files/manage/${fileMd5}`, {
    params: { userId },
  })
}

/**
 * 更新文件名
 */
export const updateFileName = (fileMd5, fileName, userId) => {
  return request.put(`/files/manage/${fileMd5}/name`, null, {
    params: { fileName, userId },
  })
}

/**
 * 更新文件公开状态
 */
export const updateIsPublic = (fileMd5, isPublic, userId) => {
  return request.put(`/files/manage/${fileMd5}/public`, null, {
    params: { isPublic, userId },
  })
}

/**
 * 获取下载信息
 */
export const getDownloadInfo = (fileMd5, userId) => {
  return request.get(`/files/manage/${fileMd5}/download`, {
    params: { userId },
  })
}

/**
 * 文档解析接口
 */
export const parseDocument = (fileMd5, userId) => {
  return request.post('/parse', null, {
    params: { file_md5: fileMd5, userId },
  })
}
