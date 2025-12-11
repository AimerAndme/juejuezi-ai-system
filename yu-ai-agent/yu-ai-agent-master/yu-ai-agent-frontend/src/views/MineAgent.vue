<template>
  <div class="mine-agent-container">
    <div class="header">
      <div class="back-button" @click="goBack">返回</div>
      <h1 class="title">AI矿山专家</h1>
      <div class="placeholder"></div>
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
  background: linear-gradient(135deg, var(--light-blue), var(--sky-blue));
  position: relative;
  overflow: hidden;
}

.header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(90deg, var(--light-orange), var(--orange));
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 2px dashed var(--light-gray-blue);
}

.back-button {
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.3s;
  justify-self: start;
  padding: 8px 16px;
  border-radius: 20px;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  /* 轻微的浅灰色阴影 */
  box-shadow: 0 2px 4px rgba(221, 238, 255, 0.4);
}

.back-button:hover {
  opacity: 0.8;
  transform: scale(1.03);
  box-shadow: 0 0 10px rgba(255, 165, 0, 0.7);
  /* 橙色微光闪烁 */
  animation: orangeGlowFlash 0.5s ease-in-out;
}

.back-button:before {
  content: '←';
  margin-right: 8px;
}

.title {
  font-size: 20px;
  font-weight: bold;
  margin: 0;
  text-align: center;
  justify-self: center;
  background: linear-gradient(45deg, var(--light-orange), #1a2a3a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-family: 'Orbitron', sans-serif;
  /* 添加文本阴影以提高可读性 */
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
}

.placeholder {
  width: 1px;
  justify-self: end;
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
  /* 设置最小高度确保内容显示正常 */
  min-height: calc(100vh - 56px - 180px); /* 100vh减去头部高度和页脚高度 */
  margin-bottom: 16px; /* 为页脚留出空间 */
  background: rgba(221, 238, 255, 0.3);
  border-radius: 16px;
  margin: 16px;
  border: 2px dashed var(--light-gray-blue);
  backdrop-filter: blur(5px);
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

@keyframes orangeGlowFlash {
  0% {
    box-shadow: 0 0 10px rgba(255, 165, 0, 0.7);
  }
  50% {
    box-shadow: 0 0 15px rgba(255, 165, 0, 0.9);
  }
  100% {
    box-shadow: 0 0 10px rgba(255, 165, 0, 0.7);
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
