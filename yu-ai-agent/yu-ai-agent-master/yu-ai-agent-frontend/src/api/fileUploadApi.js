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

/**
 * 订阅文件处理通知（SSE）
 */
export const subscribeFileProcess = (
  userId,
  fileMd5,
  onMessage,
  onError,
  onComplete,
) => {
  const url = `${API_BASE_URL}/files/notification/subscribe?userId=${encodeURIComponent(userId)}&fileMd5=${encodeURIComponent(fileMd5)}`
  console.log('[SSE] 开始订阅:', url)

  const eventSource = new EventSource(url)

  eventSource.addEventListener('connected', (event) => {
    console.log('[SSE] 连接成功:', event.data)
  })

  eventSource.addEventListener('notification', (event) => {
    try {
      const notification = JSON.parse(event.data)
      console.log('[SSE] 收到通知:', notification)
      if (onMessage) {
        onMessage(notification)
      }

      if (
        notification.status === 'SUCCESS' ||
        notification.status === 'FAILED'
      ) {
        if (onComplete) {
          onComplete(notification)
        }
        eventSource.close()
      }
    } catch (error) {
      console.error('[SSE] 解析通知失败:', error)
    }
  })

  eventSource.onerror = (error) => {
    console.error('[SSE] 连接错误:', error)

    if (eventSource.readyState === EventSource.CLOSED) {
      console.log('[SSE] 连接已关闭')
    } else if (eventSource.readyState === EventSource.CONNECTING) {
      console.log('[SSE] 正在重连...')
    } else {
      console.error('[SSE] 未知错误状态:', eventSource.readyState)
      if (onError) {
        onError(new Error('SSE 连接失败'))
      }
      eventSource.close()
    }
  }

  return eventSource
}

export default {
  initiateUpload,
  uploadChunk,
  getUploadStatus,
  completeUpload,
  subscribeFileProcess,
}
