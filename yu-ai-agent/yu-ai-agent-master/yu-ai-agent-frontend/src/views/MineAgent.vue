<template>
  <div class="mine-agent-container" :class="{ 'dark-mode': isDarkMode }">
    <div class="header">
      <div class="header-left">
        <div class="back-button" @click="goBack">返回</div>
        <h1 class="logo">AI智能体平台</h1>
      </div>
      <div class="header-right">
        <select class="language-select" v-model="currentLanguage">
          <option value="zh-CN">简体中文</option>
          <option value="en-US">English</option>
        </select>
        <a
          href="https://github.com/alibaba/spring-ai-alibaba"
          target="_blank"
          class="github-link"
        >
          <svg width="20" height="20" viewBox="0 0 16 16" fill="currentColor">
            <path
              d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"
            ></path>
          </svg>
        </a>
        <button
          class="theme-toggle"
          @click="toggleTheme"
          :title="isDarkMode ? '切换到浅色模式' : '切换到深色模式'"
        >
          {{ isDarkMode ? '🌞' : '🌙' }}
        </button>
      </div>
    </div>

    <div class="content-wrapper">
      <div class="chat-area">
        <ChatRoom
          :messages="messages"
          :connection-status="connectionStatus"
          ai-type="mine"
          @send-message="sendMessage"
        />
      </div>
    </div>

    <div class="footer-container">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import {
  chatWithMineAgent,
  getLatestConversationWithMessages,
  createConversation,
} from '../api'

// 主题切换
const isDarkMode = ref(false)
const currentLanguage = ref('zh-CN')

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light')
}

// 初始化主题
const initTheme = () => {
  const savedTheme = localStorage.getItem('theme')
  isDarkMode.value = savedTheme === 'dark'
}

// 设置页面标题和元数据
useHead({
  title: 'AI矿山专家 - 鱼皮AI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content:
        'AI矿山专家是鱼皮AI超级智能体应用平台的专业矿业顾问，帮您解答各种矿山技术问题，提供专业建议',
    },
    {
      name: 'keywords',
      content: 'AI矿山专家,矿业顾问,矿山技术,AI聊天,技术问题,鱼皮,AI智能体',
    },
  ],
})

const router = useRouter()
const messages = ref([])
const connectionStatus = ref('disconnected')
const currentConversationId = ref('') // 当前会话 ID

// 添加消息到列表
const addMessage = (content, isUser, type = '') => {
  messages.value.push({
    content,
    isUser,
    type,
    time: new Date().getTime(),
  })
}

// 发送消息
const sendMessage = async (message) => {
  addMessage(message, true, 'user-question')
  connectionStatus.value = 'connecting'

  const userId = localStorage.getItem('userId')
  const userRole = localStorage.getItem('userRole') || 'user'
  console.log(userId, currentConversationId, userRole)
  try {
    const response = await chatWithMineAgent(
      message,
      userId,
      currentConversationId.value,
      userRole
    )
    if (response.data && response.data.code === 200) {
      addMessage(response.data.data, false, 'ai-answer')
    } else {
      addMessage('服务错误，请稍后重试', false, 'ai-error')
    }
  } catch (error) {
    console.error('Chat error:', error)
    addMessage('网络错误，请检查连接', false, 'ai-error')
  }
  connectionStatus.value = 'disconnected'
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 页面加载时恢复消息
onMounted(async () => {
  initTheme()
  console.log('=== onMounted 开始执行 ===')
  const userId = localStorage.getItem('userId')
  console.log('userId:', userId)

  if (userId) {
    try {
      // 查询用户最新的对话和消息
      console.log('开始查询最新对话...')
      const response = await getLatestConversationWithMessages(userId)
      console.log('查询响应:', response)

      if (response.data && response.data.code === 404) {
        // 会话不存在，创建新会话
        console.log('会话不存在，创建新会话')
        const createRes = await createConversation(userId)
        console.log('创建响应:', createRes)
        if (createRes.data && createRes.data.code === 200) {
          currentConversationId.value = createRes.data.data.conversationId
          console.log('保存 conversationId:', currentConversationId.value)
        }
      } else if (
        response.data &&
        response.data.code === 200 &&
        response.data.data
      ) {
        // 恢复历史消息
        console.log('恢复历史消息')
        const { conversation, messages: historicalMessages } =
          response.data.data
        currentConversationId.value = conversation.conversationId
        console.log('保存 conversationId:', currentConversationId.value)

        if (historicalMessages && historicalMessages.length > 0) {
          console.log('恢复', historicalMessages.length, '条消息')
          historicalMessages.forEach((msg) => {
            // 根据 msgType 判断：1=用户消息，2=系统回复
            const isUser = msg.msgType === 1
            addMessage(msg.msgContent, isUser, '')
          })
        }
      }
    } catch (error) {
      console.error('获取对话消息失败:', error)
      // 如果错误是404，也创建新会话
      if (error.response && error.response.status === 404) {
        try {
          console.log('catch 中创建新会话')
          const createRes = await createConversation(userId)
          if (createRes.data && createRes.data.code === 200) {
            currentConversationId.value = createRes.data.data.conversationId
            console.log('保存 conversationId:', currentConversationId.value)
          }
        } catch (createError) {
          console.error('创建会话失败:', createError)
        }
      }
    }
  }

  // 添加欢迎消息
  console.log('添加欢迎消息')
  addMessage(
    '您好，我是AI矿山专家。我可以为您解答各种矿山技术问题，提供专业的矿业建议，请问有什么可以帮助您的吗？',
    false
  )
  console.log('=== onMounted 执行完毕 ===')
})

// 组件销毁前关闭SSE连接
onBeforeUnmount(() => {
  // 已经不需要SSE了
})
</script>

<style scoped>
.mine-agent-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f8f9fa;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', 'PingFang SC', -apple-system, BlinkMacSystemFont,
    sans-serif;
  transition: background-color 0.3s ease;
}

.mine-agent-container.dark-mode {
  background: #1a1a1a;
  color: #e0e0e0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 32px;
  background: #ffffff;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 10;
  transition: all 0.3s ease;
  pointer-events: auto;
}

.dark-mode .header {
  background: #2d2d2d;
  color: #e0e0e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-button {
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  padding: 8px 16px;
  border-radius: 8px;
  background-color: #f0f0f0;
  color: #333;
  border: 1px solid #e0e0e0;
  pointer-events: auto;
}

.dark-mode .back-button {
  background-color: #3d3d3d;
  color: #e0e0e0;
  border-color: #4d4d4d;
}

.back-button:hover {
  transform: scale(1.05);
  background-color: #e8e8e8;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dark-mode .back-button:hover {
  background-color: #4d4d4d;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

.back-button:before {
  content: '←';
  margin-right: 8px;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #00d1b2;
  letter-spacing: 0.5px;
}

.dark-mode .logo {
  color: #00d1b2;
}

.language-select {
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #ffffff;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
  pointer-events: auto;
}

.dark-mode .language-select {
  background: #3d3d3d;
  color: #e0e0e0;
  border-color: #4d4d4d;
}

.language-select:hover {
  border-color: #1677ff;
}

.github-link {
  display: flex;
  align-items: center;
  color: #333;
  transition: all 0.3s ease;
  padding: 6px;
  border-radius: 6px;
  pointer-events: auto;
}

.dark-mode .github-link {
  color: #e0e0e0;
}

.github-link:hover {
  color: #1677ff;
  transform: scale(1.05);
}

.theme-toggle {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #ffffff;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: auto;
}

.dark-mode .theme-toggle {
  background: #3d3d3d;
  border-color: #4d4d4d;
}

.theme-toggle:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dark-mode .theme-toggle:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.chat-area {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  position: relative;
  min-height: calc(100vh - 56px - 180px);
  margin-bottom: 16px;
  background: rgba(147, 210, 184, 0.1);
  border-radius: 16px;
  margin: 16px;
  border: 1px solid rgba(147, 210, 184, 0.2);
  transition: all 0.3s ease;
  animation: fadeIn 0.6s ease-in;
}

.dark-mode .chat-area {
  background: rgba(147, 210, 184, 0.05);
  border-color: rgba(147, 210, 184, 0.15);
}

.chat-area:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.dark-mode .chat-area:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.footer-container {
  margin-top: auto;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .header {
    padding: 12px 16px;
  }

  .title {
    font-size: 18px;
  }

  .chat-area {
    padding: 12px;
    min-height: calc(100vh - 48px - 160px); /* 调整计算值 */
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 10px 12px;
  }

  .back-button {
    font-size: 14px;
  }

  .title {
    font-size: 16px;
  }

  .chat-area {
    padding: 8px;
    min-height: calc(100vh - 42px - 150px); /* 再次调整计算值 */
    margin-bottom: 8px;
  }
}
</style>
