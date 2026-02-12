<template>
  <div class="chat-container">
    <!-- 聊天记录区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="message-wrapper"
      >
        <!-- AI消息 -->
        <div
          v-if="!msg.isUser"
          class="message ai-message"
          :class="[msg.type, aiType, 'fade-in']"
        >
          <div class="avatar ai-avatar" :class="aiType">
            <AiAvatarFallback :type="aiType" />
          </div>
          <div class="message-bubble">
            <div class="message-content">
              <template v-if="msg.type === 'ai-thinking'">
                <span class="thinking-text">{{ msg.content }}</span>
                <span class="thinking-dots">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </span>
              </template>
              <template v-else>
                {{ msg.content }}
                <span
                  v-if="
                    connectionStatus === 'connecting' &&
                    index === messages.length - 1
                  "
                  class="typing-indicator"
                  >▋</span
                >
              </template>
            </div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>

        <!-- 用户消息 -->
        <div v-else class="message user-message" :class="[msg.type, 'fade-in']">
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-status" v-if="msg.status">
              <span v-if="msg.status === 'sending'" class="status-indicator sending">发送中...</span>
              <span v-else-if="msg.status === 'sent'" class="status-indicator sent">已发送</span>
              <span v-else-if="msg.status === 'failed'" class="status-indicator failed">发送失败</span>
            </div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
          <div class="avatar user-avatar">
            <div class="avatar-placeholder">我</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-container">
      <div class="chat-input">
        <textarea
          v-model="inputMessage"
          @keydown.enter.prevent="sendMessage"
          placeholder="请输入消息..."
          class="input-box"
          :disabled="connectionStatus === 'connecting'"
        ></textarea>
        <button
          @click="sendMessage"
          class="send-button"
          :disabled="connectionStatus === 'connecting' || !inputMessage.trim()"
        >
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch, computed } from 'vue'
import AiAvatarFallback from '../common/AiAvatarFallback.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => [],
  },
  connectionStatus: {
    type: String,
    default: 'disconnected',
  },
  aiType: {
    type: String,
    default: 'default', // 'love' 或 'super'
  },
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')
const messagesContainer = ref(null)

// 根据AI类型选择不同头像
const aiAvatar = computed(() => {
  return props.aiType === 'love'
    ? '/ai-love-avatar.png' // 恋爱大师头像
    : '/ai-super-avatar.png' // 超级智能体头像
})

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  emit('send-message', inputMessage.value)
  inputMessage.value = ''
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 自动滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 监听消息变化与内容变化，自动滚动
watch(
  () => props.messages.length,
  () => {
    scrollToBottom()
  },
)

watch(
  () => props.messages.map((m) => m.content).join(''),
  () => {
    scrollToBottom()
  },
)

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 600px;
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  overflow: hidden;
  position: relative;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  padding-bottom: 100px; /* 为输入框留出空间 */
  display: flex;
  flex-direction: column;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 80px; /* 与输入框高度相匹配 */
  background: var(--card-bg);
}

.message-wrapper {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.message {
  display: flex;
  align-items: flex-start;
  max-width: 85%;
  margin-bottom: 8px;
}

.user-message {
  margin-left: auto; /* 用户消息靠右 */
  flex-direction: row; /* 正常顺序，先气泡后头像 */
}

.ai-message {
  margin-right: auto; /* AI消息靠左 */
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

/* 矿山智能体头像样式 */
.ai-avatar.mine {
  background: var(--primary-color);
  color: white;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.user-avatar {
  margin-left: 12px; /* 用户头像在右侧，左边距 */
}

.ai-avatar {
  margin-right: 12px; /* AI头像在左侧，右边距 */
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-light);
  color: var(--text-primary);
  font-weight: 600;
}

.message-bubble {
  padding: 16px 20px;
  border-radius: var(--radius-lg);
  position: relative;
  word-wrap: break-word;
  min-width: 100px;
  max-width: 100%;
  background-color: var(--bg-light);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal) ease;
}

.message-bubble:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.user-message .message-bubble {
  background: var(--primary-color);
  color: white;
  border-bottom-right-radius: var(--radius-sm);
  text-align: left;
  box-shadow: var(--shadow-md);
  border-top-right-radius: var(--radius-sm);
}

.ai-message .message-bubble {
  background: var(--bg-light);
  color: var(--text-primary);
  border-bottom-left-radius: var(--radius-sm);
  text-align: left;
  box-shadow: var(--shadow-sm);
  border-top-left-radius: var(--radius-sm);
}

/* 矿山智能体消息样式 */
.ai-message.mine .message-bubble {
  background: var(--bg-light);
  color: var(--text-primary);
  border-bottom-left-radius: var(--radius-sm);
  text-align: left;
  box-shadow: var(--shadow-sm);
  border-top-left-radius: var(--radius-sm);
}

/* 消息状态指示器 */
.message-status {
  margin-top: 4px;
  font-size: 12px;
}

.status-indicator {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 500;
}

.status-indicator.sending {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.status-indicator.sent {
  background: rgba(82, 196, 26, 0.2);
  color: white;
}

.status-indicator.failed {
  background: rgba(255, 77, 79, 0.2);
  color: white;
}

.message-content {
  font-size: 16px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.message-time {
  font-size: 12px;
  opacity: 0.6;
  margin-top: 6px;
  text-align: right;
  color: var(--text-secondary);
}

.chat-input-container {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--card-bg);
  border-top: 1px solid var(--border-color);
  z-index: 100;
  height: 80px; /* 固定高度 */
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.chat-input {
  display: flex;
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  align-items: center;
  gap: 12px;
}

.input-box {
  flex-grow: 1;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 12px 16px;
  font-size: 16px;
  resize: none;
  min-height: 44px;
  max-height: 80px;
  outline: none;
  transition: var(--transition);
  overflow-y: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE & Edge */
  background-color: var(--bg-light);
  color: var(--text-primary);
}

/* 隐藏Webkit浏览器的滚动条 */
.input-box::-webkit-scrollbar {
  display: none;
}

.input-box:focus {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.send-button {
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-lg);
  padding: 0 24px;
  font-size: 16px;
  cursor: pointer;
  transition: var(--transition);
  height: 44px;
  align-self: center;
  font-weight: 600;
  box-shadow: var(--shadow-sm);
}

.send-button:hover:not(:disabled) {
  background: var(--primary-hover);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.typing-indicator {
  display: inline-block;
  animation: blink 0.7s infinite;
  margin-left: 2px;
  color: var(--text-secondary);
}

@keyframes blink {
  0% {
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}

.input-box:disabled,
.send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message {
    max-width: 95%;
  }

  .message-content {
    font-size: 15px;
  }

  .chat-input {
    padding: 12px;
  }

  .input-box {
    padding: 8px 12px;
  }

  .send-button {
    padding: 0 15px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .avatar {
    width: 32px;
    height: 32px;
  }

  .message-bubble {
    padding: 10px;
  }

  .message-content {
    font-size: 14px;
  }

  .chat-input-container {
    height: 64px;
  }

  .chat-messages {
    bottom: 64px;
  }
}

/* 新增：不同类型消息的样式 */
.ai-answer {
  animation: fadeIn 0.3s ease-in-out;
}

.ai-final {
  /* 最终回答，可以有不同的样式，例如边框高亮等 */
}

.ai-error {
  opacity: 0.7;
}

.ai-thinking {
  opacity: 0.8;
}

.thinking-text {
  color: var(--text-secondary);
  font-style: italic;
}

.thinking-dots {
  display: inline-flex;
  gap: 4px;
  margin-left: 8px;
}

.thinking-dots .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--primary-color);
  animation: thinkingDot 1.4s infinite;
}

.thinking-dots .dot:nth-child(1) {
  animation-delay: 0s;
}

.thinking-dots .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-dots .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes thinkingDot {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.3;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.user-question {
  /* 用户提问的特殊样式 */
}

/* 连续消息气泡样式 */
.ai-message + .ai-message {
  margin-top: 4px;
}

.ai-message + .ai-message .avatar {
  visibility: hidden;
}

.ai-message + .ai-message .message-bubble {
  border-top-left-radius: 10px;
}
</style>
