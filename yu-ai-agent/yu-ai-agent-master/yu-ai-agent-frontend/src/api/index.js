import axios from 'axios'

// 根据环境变量设置 API 基础 URL
const API_BASE_URL =
  process.env.NODE_ENV === 'production'
    ? '/api' // 生产环境使用相对路径，适用于前后端部署在同一域名下
    : 'http://localhost:8123/api' // 开发环境指向本地后端服务

// 创建axios实例
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
})

// 封装SSE连接
export const connectSSE = (url, params, onMessage, onError) => {
  // 构建带参数的URL
  const queryString = Object.keys(params)
    .map(
      (key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`
    )
    .join('&')

  const fullUrl = `${API_BASE_URL}${url}?${queryString}`

  // 创建EventSource
  const eventSource = new EventSource(fullUrl)

  eventSource.onmessage = (event) => {
    let data = event.data

    // 检查是否是特殊标记
    if (data === '[DONE]') {
      if (onMessage) onMessage('[DONE]')
    } else {
      // 处理普通消息
      if (onMessage) onMessage(data)
    }
  }

  eventSource.onerror = (error) => {
    if (onError) onError(error)
    eventSource.close()
  }

  // 返回eventSource实例，以便后续可以关闭连接
  return eventSource
}

// AI恋爱大师聊天
export const chatWithLoveApp = (message, chatId) => {
  return connectSSE('/ai/love_app/chat/sse', { message, chatId })
}

// AI矿山专家聊天
export const chatWithMineAgent = (
  query,
  userId,
  conversationId,
  userRole = 'user'
) => {
  return request.get('/mine/chat', {
    params: {
      query,
      UserId: userId,
      conversationId,
      UserRole: userRole,
    },
  })
}

// AI超级智能体聊天
export const chatWithManus = (message) => {
  return connectSSE('/ai/manus/chat', { message })
}

// 注册
export const register = (account, password, userName, userRole) => {
  return request.post('/auth/register', null, {
    params: { account, password, userName, userRole },
  })
}

// 登录
export const login = (account, password) => {
  return request.post('/auth/login', null, {
    params: { account, password },
  })
}

// 登出
export const logout = (userId) => {
  return request.post('/auth/logout', null, {
    headers: { 'X-User-Id': userId },
  })
}

// 获取当前用户信息
export const getCurrentUser = (userId) => {
  return request.get('/auth/test', {
    headers: { 'X-User-Id': userId },
  })
}

// 获取用户最新对话和消息
export const getLatestConversationWithMessages = (userId) => {
  return request.get(`/conversation/latest/${userId}`)
}

// 获取对话消息列表
export const getConversationMessages = (conversationId) => {
  return request.get(`/conversation/messages/${conversationId}`)
}

// 创建新对话
export const createConversation = (userId) => {
  return request.post('/conversation/create', null, {
    params: { userId },
  })
}

export default request
