<template>
  <div class="love-master-container" :class="{ 'dark-mode': isDarkMode }">
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
          ai-type="love"
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
import { chatWithLoveApp } from '../api'

// 设置页面标题和元数据
useHead({
  title: 'AI恋爱大师 - 鱼皮AI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content:
        'AI恋爱大师是鱼皮AI超级智能体应用平台的专业情感顾问，帮你解答各种恋爱问题，提供情感建议',
    },
    {
      name: 'keywords',
      content: 'AI恋爱大师,情感顾问,恋爱咨询,AI聊天,情感问题,鱼皮,AI智能体',
    },
  ],
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
const isDarkMode = ref(false)
const currentLanguage = ref('zh-CN')
let eventSource = null

// 主题切换
const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  document.documentElement.classList.toggle('dark-mode', isDarkMode.value)
  localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light')
}

// 初始化主题
const initTheme = () => {
  const savedTheme = localStorage.getItem('theme')
  isDarkMode.value = savedTheme === 'dark'
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark-mode')
  }
}

// 生成随机会话ID
const generateChatId = () => {
  return 'love_' + Math.random().toString(36).substring(2, 10)
}

// 添加消息到列表
const addMessage = (content, isUser) => {
  messages.value.push({
    content,
    isUser,
    time: new Date().getTime(),
  })
}

// 发送消息
const sendMessage = (message) => {
  addMessage(message, true)

  // 连接SSE
  if (eventSource) {
    eventSource.close()
  }

  // 创建一个空的AI回复消息
  const aiMessageIndex = messages.value.length
  addMessage('', false)

  connectionStatus.value = 'connecting'
  eventSource = chatWithLoveApp(message, chatId.value)

  // 监听SSE消息
  eventSource.onmessage = (event) => {
    const data = event.data
    if (data && data !== '[DONE]') {
      // 更新最新的AI消息内容，而不是创建新消息
      if (aiMessageIndex < messages.value.length) {
        messages.value[aiMessageIndex].content += data
      }
    }

    if (data === '[DONE]') {
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }

  // 监听SSE错误
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    eventSource.close()
  }
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 页面加载时添加欢迎消息
onMounted(() => {
  initTheme()
  // 生成聊天ID
  chatId.value = generateChatId()

  // 添加欢迎消息
  addMessage(
    '欢迎来到AI恋爱大师，请告诉我你的恋爱问题，我会尽力给予帮助和建议。',
    false
  )
})

// 组件销毁前关闭SSE连接
onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.love-master-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-light);
  position: relative;
  overflow: hidden;
  font-family: var(--font-family-base);
  transition: background-color 0.3s ease;
}

.love-master-container.dark-mode {
  background: var(--bg-light);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--header-padding-vertical) var(--header-padding-horizontal);
  background: var(--bg-white);
  color: var(--text-primary);
  box-shadow: var(--shadow-light);
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid var(--border-light);
  transition: all 0.3s ease;
  pointer-events: auto;
}

.dark-mode .header {
  background: var(--bg-white);
  border-bottom-color: var(--border-light);
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
  font-size: var(--font-size-small);
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  padding: var(--button-padding-vertical) var(--button-padding-horizontal);
  border-radius: var(--button-border-radius);
  background-color: var(--bg-light);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  pointer-events: auto;
}

.dark-mode .back-button {
  background-color: #3d3d3d;
  color: #e0e0e0;
  border-color: #4d4d4d;
}

.back-button:hover {
  transform: scale(1.05);
  color: var(--primary-color);
  border-color: var(--primary-color);
  background-color: rgba(22, 119, 255, 0.1);
  box-shadow: var(--shadow-medium);
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

.dark-mode .logo {
  color: var(--logo-color);
}

.language-select {
  padding: 6px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-white);
  color: var(--text-primary);
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
  border-color: var(--primary-color);
}

.github-link {
  display: flex;
  align-items: center;
  color: var(--text-secondary);
  transition: all 0.3s ease;
  padding: 6px;
  border-radius: 6px;
  pointer-events: auto;
}

.dark-mode .github-link {
  color: #e0e0e0;
}

.github-link:hover {
  color: var(--primary-color);
  transform: scale(1.05);
}

.theme-toggle {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-white);
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
  box-shadow: var(--shadow-medium);
  border-color: var(--primary-color);
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.chat-area {
  flex: 1;
  padding: var(--chat-area-padding);
  overflow: hidden;
  position: relative;
  min-height: calc(100vh - var(--header-height) - var(--footer-height));
  margin: var(--chat-area-margin-vertical) auto;
  max-width: var(--container-max-width);
  width: 100%;
  background: var(--bg-card);
  border-radius: var(--chat-area-border-radius);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-card);
  transition: all 0.3s ease;
  animation: fadeIn 0.6s ease-in;
}

.dark-mode .chat-area {
  background: var(--bg-card);
  border-color: var(--card-border);
}

.chat-area:hover {
  box-shadow: var(--shadow-card-hover);
}

.dark-mode .chat-area:hover {
  box-shadow: var(--shadow-card-hover);
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

  .chat-id {
    font-size: 12px;
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

  .chat-id {
    display: none;
  }

  .chat-area {
    padding: 8px;
    min-height: calc(100vh - 42px - 150px); /* 再次调整计算值 */
    margin-bottom: 8px;
  }
}
</style>
