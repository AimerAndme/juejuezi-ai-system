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
          :class="[msg.type, aiType]"
        >
          <div class="avatar ai-avatar" :class="aiType">
            <AiAvatarFallback :type="aiType" />
          </div>
          <div class="message-bubble">
            <div class="message-content">
              {{ msg.content }}
              <span
                v-if="
                  connectionStatus === 'connecting' &&
                  index === messages.length - 1
                "
                class="typing-indicator"
                >▋</span
              >
            </div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>

        <!-- 用户消息 -->
        <div v-else class="message user-message" :class="[msg.type]">
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
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
import AiAvatarFallback from './AiAvatarFallback.vue'

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
  }
)

watch(
  () => props.messages.map((m) => m.content).join(''),
  () => {
    scrollToBottom()
  }
)

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 70vh;
  min-height: 600px;
  background: linear-gradient(
    135deg,
    rgba(221, 238, 255, 0.8),
    rgba(173, 216, 230, 0.8)
  );
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  border: 2px solid var(--light-gray-blue);
  box-shadow: 0 8px 32px rgba(173, 216, 230, 0.3);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  padding-bottom: 80px; /* 为输入框留出空间 */
  display: flex;
  flex-direction: column;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 72px; /* 与输入框高度相匹配 */
  /* 波浪线分隔效果 */
  background-image: radial-gradient(
      circle at 100% 150%,
      rgba(221, 238, 255, 0.2) 25%,
      transparent 26%
    ),
    radial-gradient(
      circle at 0 150%,
      rgba(221, 238, 255, 0.2) 25%,
      transparent 26%
    ),
    radial-gradient(
      circle at 100% 0,
      rgba(221, 238, 255, 0.2) 25%,
      transparent 26%
    ),
    radial-gradient(
      circle at 0 0,
      rgba(221, 238, 255, 0.2) 25%,
      transparent 26%
    );
  background-size: 20px 20px;
  background-position: 0 0, 10px 0, 10px -10px, 0px 10px;
}

.message-wrapper {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  width: 100%;
  /* 连续消息之间添加虚线分隔 */
  border-bottom: 1px dashed var(--light-gray-blue);
  padding-bottom: 16px;
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
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 矿山智能体头像样式 */
.ai-avatar.mine {
  background: linear-gradient(135deg, var(--light-orange), var(--orange));
  color: white;
  font-weight: bold;
  border: 2px solid white;
  box-shadow: 0 0 10px rgba(255, 165, 0, 0.5);
}

.user-avatar {
  margin-left: 8px; /* 用户头像在右侧，左边距 */
}

.ai-avatar {
  margin-right: 8px; /* AI头像在左侧，右边距 */
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--light-blue);
  color: white;
  font-weight: bold;
}

.message-bubble {
  padding: 12px;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;
  min-width: 100px; /* 最小宽度 */
  /* 布丁质感 */
  background-color: var(--light-bg);
  border: 1px solid var(--light-gray-blue);
  box-shadow: 0 4px 8px rgba(221, 238, 255, 0.4);
}

.user-message .message-bubble {
  background: linear-gradient(135deg, var(--light-blue), var(--sky-blue));
  color: white;
  border-bottom-right-radius: 4px;
  text-align: left;
  /* 轻微的浅蓝色阴影 */
  box-shadow: 0 4px 8px rgba(221, 238, 255, 0.4);
}

.ai-message .message-bubble {
  background: linear-gradient(135deg, var(--light-purple), var(--light-bg));
  color: #1a2a3a; /* 加深AI消息的字体颜色，提高可读性 */
  border-bottom-left-radius: 4px;
  text-align: left;
  /* 轻微的浅蓝色阴影 */
  box-shadow: 0 4px 8px rgba(221, 238, 255, 0.4);
}

/* 矿山智能体消息样式 */
.ai-message.mine .message-bubble {
  background: linear-gradient(135deg, var(--light-orange), var(--light-bg));
  color: #1a2a3a;
  border-bottom-left-radius: 4px;
  text-align: left;
  box-shadow: 0 4px 8px rgba(255, 165, 0, 0.4);
}

.message-content {
  font-size: 16px;
  line-height: 1.5;
  white-space: pre-wrap;
  font-family: 'Orbitron', sans-serif;
  /* 增加文本阴影以提高在浅色背景上的可读性 */
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
  text-align: right;
}

.chat-input-container {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to right, var(--light-bg), var(--light-blue));
  border-top: 2px dashed var(--light-gray-blue);
  z-index: 100;
  height: 72px; /* 固定高度 */
  box-shadow: 0 -2px 10px rgba(173, 216, 230, 0.2);
}

.chat-input {
  display: flex;
  padding: 16px;
  height: 100%;
  box-sizing: border-box;
  align-items: center;
}

.input-box {
  flex-grow: 1;
  border: 2px solid var(--light-gray-blue);
  border-radius: 20px;
  padding: 10px 16px;
  font-size: 16px;
  resize: none;
  min-height: 20px;
  max-height: 40px; /* 限制高度 */
  outline: none;
  transition: all 0.3s;
  overflow-y: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE & Edge */
  /* 浅灰蓝底色 */
  background-color: var(--light-bg);
  color: #3a5a7a;
  font-family: 'Orbitron', sans-serif;
}

/* 隐藏Webkit浏览器的滚动条 */
.input-box::-webkit-scrollbar {
  display: none;
}

.input-box:focus {
  border-color: var(--light-blue);
  box-shadow: 0 0 10px rgba(173, 216, 230, 0.5);
}

.send-button {
  margin-left: 12px;
  background: linear-gradient(90deg, var(--light-blue), var(--sky-blue));
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0 20px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  height: 40px;
  align-self: center;
  /* 轻微的浅灰色阴影 */
  box-shadow: 0 4px 8px rgba(221, 238, 255, 0.4);
  font-family: 'Orbitron', sans-serif;
}

.send-button:hover:not(:disabled) {
  background: linear-gradient(90deg, var(--sky-blue), var(--light-blue));
  transform: scale(1.03);
  /* 淡蓝色微光闪烁 */
  box-shadow: 0 0 15px rgba(173, 216, 230, 0.7);
  /* 软弹动效 */
  animation: softBounce 0.3s ease-in-out;
}

.typing-indicator {
  display: inline-block;
  animation: blink 0.7s infinite;
  margin-left: 2px;
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

@keyframes softBounce {
  0% {
    transform: scale(1.03);
  }
  50% {
    transform: scale(0.98);
  }
  100% {
    transform: scale(1.03);
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
