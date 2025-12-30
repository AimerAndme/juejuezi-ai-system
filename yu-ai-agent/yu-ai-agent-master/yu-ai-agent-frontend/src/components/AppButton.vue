<template>
  <button
    :class="[
      'app-button',
      `app-button--${variant}`,
      `app-button--${size}`,
      { 'app-button--loading': loading, 'app-button--disabled': disabled },
    ]"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
  >
    <span v-if="loading" class="app-button__spinner"></span>
    <slot>{{ text }}</slot>
  </button>
</template>

<script setup>
defineProps({
  variant: { type: String, default: 'primary' },
  size: { type: String, default: 'medium' },
  text: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
})
defineEmits(['click'])
</script>

<style scoped>
/* CSS 变量定义 */
:root {
  --primary-color: #1677ff;
  --primary-hover: #40a9ff;
  --primary-active: #096dd9;
  --success-color: #52c41a;
  --success-hover: #73d13d;
  --warning-color: #faad14;
  --warning-hover: #ffc53d;
  --error-color: #f5222d;
  --error-hover: #ff4d4f;
  --text-primary: #333333;
  --text-white: #ffffff;
  --bg-tertiary: #f8f9fa;
  --border-light: #e8e8e8;
}

:root.dark-mode {
  --text-primary: #e0e0e0;
  --bg-tertiary: #1a1a1a;
  --border-light: #303030;
}

/* 基础按钮样式 */
.app-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid transparent;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  white-space: nowrap;
  position: relative;
  overflow: hidden;
}

.app-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.app-button:disabled,
.app-button.app-button--disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* 尺寸变体 */
.app-button--small {
  padding: 4px 12px;
  font-size: 12px;
  height: 28px;
}

.app-button--medium {
  padding: 8px 16px;
  font-size: 14px;
  height: 36px;
}

.app-button--large {
  padding: 12px 24px;
  font-size: 16px;
  height: 44px;
}

/* 颜色变体 */
.app-button--primary {
  background-color: var(--primary-color);
  color: var(--text-white);
}

.app-button--primary:hover:not(:disabled) {
  background-color: var(--primary-hover);
}

.app-button--primary:active:not(:disabled) {
  background-color: var(--primary-active);
}

.app-button--success {
  background-color: var(--success-color);
  color: var(--text-white);
}

.app-button--success:hover:not(:disabled) {
  background-color: var(--success-hover);
}

.app-button--warning {
  background-color: var(--warning-color);
  color: var(--text-white);
}

.app-button--warning:hover:not(:disabled) {
  background-color: var(--warning-hover);
}

.app-button--danger {
  background-color: var(--error-color);
  color: var(--text-white);
}

.app-button--danger:hover:not(:disabled) {
  background-color: var(--error-hover);
}

.app-button--secondary {
  background-color: var(--bg-tertiary);
  color: var(--text-primary);
  border-color: var(--border-light);
}

.app-button--secondary:hover:not(:disabled) {
  background-color: var(--border-light);
  opacity: 0.9;
}

.app-button--ghost {
  background-color: transparent;
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.app-button--ghost:hover:not(:disabled) {
  background-color: rgba(22, 119, 255, 0.08);
  border-color: var(--primary-hover);
}

/* 加载动画 */
.app-button--loading {
  pointer-events: none;
}

.app-button__spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: var(--text-white);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .app-button--large {
    padding: 10px 20px;
    height: 40px;
  }
}
</style>
