<template>
  <div class="file-process-progress">
    <div
      v-if="!isProcessing && !isCompleted && !isFailed"
      class="waiting-status"
    >
      <div class="status-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <circle cx="12" cy="12" r="10" stroke-width="2" />
          <path d="M12 6v6l4 2" stroke-width="2" stroke-linecap="round" />
        </svg>
      </div>
      <p>等待处理中...</p>
    </div>

    <div v-else class="progress-content">
      <div class="progress-bar-section">
        <div class="progress-info">
          <span class="status-text">{{ statusText }}</span>
          <span class="progress-percent">{{ progress }}%</span>
        </div>
        <div class="progress-bar">
          <div
            class="progress-fill"
            :class="progressClass"
            :style="{ width: progress + '%' }"
          ></div>
        </div>
      </div>

      <div v-if="currentMessage" class="current-message">
        <span class="message-icon">ℹ️</span>
        <span>{{ currentMessage }}</span>
      </div>

      <div v-if="isProcessing" class="processing-steps">
        <div
          v-for="(step, index) in processingSteps"
          :key="index"
          class="step-item"
          :class="{
            'step-active': step.active,
            'step-completed': step.completed,
          }"
        >
          <div class="step-icon">
            <svg
              v-if="step.completed"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
            >
              <path
                d="M20 6L9 17l-5-5"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <svg
              v-else-if="step.active"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
            >
              <circle cx="12" cy="12" r="10" stroke-width="2" />
              <path d="M12 6v6l4 2" stroke-width="2" stroke-linecap="round" />
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <circle cx="12" cy="12" r="10" stroke-width="2" />
            </svg>
          </div>
          <span class="step-label">{{ step.label }}</span>
        </div>
      </div>

      <div v-if="isCompleted" class="success-section">
        <div class="success-icon">✅</div>
        <p class="success-text">文件处理成功！</p>
        <p class="success-detail">{{ fileName }} 已成功解析并完成向量化</p>
      </div>

      <div v-if="isFailed" class="error-section">
        <div class="error-icon">❌</div>
        <p class="error-text">文件处理失败</p>
        <p class="error-message">{{ currentMessage }}</p>

        <div
          v-if="errorTextList && errorTextList.length > 0"
          class="error-details"
        >
          <h4>文本解析错误 ({{ errorTextList.length }} 页)</h4>
          <div class="error-list">
            <div
              v-for="(error, index) in errorTextList"
              :key="index"
              class="error-item"
            >
              <span class="error-page">第 {{ Object.keys(error)[0] }} 页:</span>
              <span class="error-desc">{{ Object.values(error)[0] }}</span>
            </div>
          </div>
        </div>

        <div
          v-if="errorImageList && errorImageList.length > 0"
          class="error-details"
        >
          <h4>图片解析错误 ({{ errorImageList.length }} 页)</h4>
          <div class="error-list">
            <div
              v-for="(error, index) in errorImageList"
              :key="index"
              class="error-item"
            >
              <span class="error-page">第 {{ Object.keys(error)[0] }} 页:</span>
              <span class="error-desc">{{ Object.values(error)[0] }}</span>
            </div>
          </div>
        </div>

        <button @click="$emit('retry')" class="btn-retry">重试</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  fileName: {
    type: String,
    default: '',
  },
  progress: {
    type: Number,
    default: 0,
  },
  status: {
    type: String,
    default: 'PENDING',
  },
  message: {
    type: String,
    default: '',
  },
  errorTextList: {
    type: Array,
    default: () => [],
  },
  errorImageList: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['retry'])

const isProcessing = computed(() => props.status === 'PROCESSING')
const isCompleted = computed(() => props.status === 'SUCCESS')
const isFailed = computed(() => props.status === 'FAILED')

const statusText = computed(() => {
  switch (props.status) {
    case 'PROCESSING':
      return '处理中'
    case 'SUCCESS':
      return '处理完成'
    case 'FAILED':
      return '处理失败'
    default:
      return '等待处理'
  }
})

const progressClass = computed(() => {
  if (isCompleted.value) return 'progress-success'
  if (isFailed.value) return 'progress-error'
  return 'progress-processing'
})

const currentMessage = computed(() => props.message)

const processingSteps = computed(() => {
  const steps = [
    {
      label: '文件下载',
      completed: props.progress > 0,
      active: props.progress === 0,
    },
    {
      label: '文件解析',
      completed: props.progress >= 50,
      active: props.progress > 0 && props.progress < 50,
    },
    {
      label: '向量化处理',
      completed: props.progress >= 80,
      active: props.progress >= 50 && props.progress < 80,
    },
    { label: '完成', completed: props.progress === 100, active: false },
  ]
  return steps
})
</script>

<style scoped>
.file-process-progress {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.waiting-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  color: #909399;
}

.waiting-status .status-icon {
  width: 40px;
  height: 40px;
  margin-bottom: 10px;
  color: #c0c4cc;
}

.waiting-status p {
  margin: 0;
  font-size: 13px;
}

.progress-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-bar-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.status-text {
  color: #606266;
  font-weight: 500;
}

.progress-percent {
  color: #409eff;
  font-weight: bold;
}

.progress-bar {
  height: 6px;
  background: #e4e7ed;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.progress-processing {
  background: linear-gradient(90deg, #409eff, #66b1ff);
  animation: progress-shine 2s infinite;
}

.progress-success {
  background: linear-gradient(90deg, #67c23a, #85ce61);
}

.progress-error {
  background: linear-gradient(90deg, #f56c6c, #f78989);
}

@keyframes progress-shine {
  0% {
    opacity: 0.8;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0.8;
  }
}

.current-message {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px;
  background: #f0f9ff;
  border: 1px solid #c6e2ff;
  border-radius: 4px;
  font-size: 13px;
  color: #409eff;
}

.message-icon {
  font-size: 14px;
}

.processing-steps {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
  transition: all 0.3s;
}

.step-item.step-active {
  color: #409eff;
  font-weight: 500;
}

.step-item.step-completed {
  color: #67c23a;
}

.step-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.step-icon svg {
  width: 100%;
  height: 100%;
}

.success-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #f0f9ff;
  border: 1px solid #c6e2ff;
  border-radius: 4px;
}

.success-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.success-text {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: bold;
  color: #67c23a;
}

.success-detail {
  margin: 0;
  font-size: 13px;
  color: #606266;
  text-align: center;
}

.error-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 15px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 4px;
}

.error-icon {
  font-size: 36px;
  text-align: center;
  margin-bottom: 8px;
}

.error-text {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
  text-align: center;
}

.error-message {
  margin: 0;
  font-size: 13px;
  color: #f56c6c;
  text-align: center;
  padding: 8px;
  background: #fff;
  border-radius: 4px;
}

.error-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-radius: 4px;
}

.error-details h4 {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #303133;
  font-weight: 600;
}

.error-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 120px;
  overflow-y: auto;
}

.error-item {
  display: flex;
  gap: 6px;
  padding: 6px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
}

.error-page {
  color: #909399;
  flex-shrink: 0;
  font-weight: 500;
}

.error-desc {
  color: #f56c6c;
  word-break: break-word;
}

.btn-retry {
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.3s;
  align-self: center;
}

.btn-retry:hover {
  background: #66b1ff;
}
</style>
