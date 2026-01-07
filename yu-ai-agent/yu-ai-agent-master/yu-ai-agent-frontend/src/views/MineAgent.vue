<template>
  <div class="mine-agent-container">
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

const currentLanguage = ref('zh-CN')

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
  console.log('=== onMounted 开始执行 ===')
  const userId = localStorage.getItem('userId')
  console.log('userId:', userId)
  let hasHistoricalMessages = false

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
          hasHistoricalMessages = true
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

  // 只有当没有历史消息时才添加欢迎消息
  if (!hasHistoricalMessages) {
    console.log('添加欢迎消息')
    addMessage(
      '您好，我是AI矿山专家。我可以为您解答各种矿山技术问题，提供专业的矿业建议，请问有什么可以帮助您的吗？',
      false
    )
  }
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
  background: var(--bg-color);
  position: relative;
  overflow: hidden;
  font-family: 'Inter', 'PingFang SC', -apple-system, BlinkMacSystemFont,
    sans-serif;
  transition: background-color 0.3s ease;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: var(--card-bg);
  color: var(--text-primary);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 10;
  transition: all 0.3s ease;
  pointer-events: auto;
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
  transition: var(--transition);
  padding: 0.6em 1.2em;
  border-radius: var(--radius-md);
  background-color: var(--bg-light);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  pointer-events: auto;
  font-weight: 600;
}

.back-button:hover {
  background-color: var(--border-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.back-button:before {
  content: '←';
  margin-right: 8px;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: var(--logo-color);
  letter-spacing: 0.5px;
}

.language-select {
  padding: 0.5em 1em;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--card-bg);
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: var(--transition);
  outline: none;
  pointer-events: auto;
  font-weight: 500;
}

.language-select:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.github-link {
  display: flex;
  align-items: center;
  color: var(--text-primary);
  transition: var(--transition);
  padding: 8px;
  border-radius: var(--radius-md);
  pointer-events: auto;
}

.github-link:hover {
  color: var(--primary-color);
  transform: scale(1.05);
  background-color: var(--bg-light);
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.chat-area {
  flex: 1;
  padding: 24px;
  overflow: hidden;
  position: relative;
  min-height: calc(100vh - 80px - 200px);
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  margin: 16px auto;
  max-width: 1800px;
  width: calc(100% - 16px);
  border: 1px solid var(--card-border);
  transition: var(--transition);
  animation: fadeIn 0.6s ease-in;
}

.chat-area:hover {
  box-shadow: var(--shadow-lg);
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

  .logo {
    font-size: 18px;
  }

  .chat-area {
    padding: 16px;
    min-height: calc(100vh - 80px - 180px);
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 10px 12px;
  }

  .back-button {
    font-size: 14px;
    padding: 0.5em 1em;
  }

  .logo {
    font-size: 16px;
  }

  .chat-area {
    padding: 12px;
    min-height: calc(100vh - 80px - 160px);
    margin-bottom: 8px;
  }
}
</style>
