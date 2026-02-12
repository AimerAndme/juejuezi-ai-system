<template>
  <button
    :class="[
      'app-button',
      `app-button--${variant}`,
      `app-button--${size}`,
      { 'app-button--loading': loading, 'app-button--disabled': disabled, 'app-button--icon': icon },
    ]"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <span v-if="loading" class="app-button__spinner"></span>
    <span v-else-if="icon" class="app-button__icon">{{ icon }}</span>
    <slot>{{ text }}</slot>
    <span v-if="ripple" class="app-button__ripple" :style="rippleStyle"></span>
  </button>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  variant: { type: String, default: 'primary' },
  size: { type: String, default: 'medium' },
  text: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  icon: { type: String, default: '' },
  ripple: { type: Boolean, default: true },
})

defineEmits(['click'])

const rippleVisible = ref(false)
const rippleStyle = ref({})

const handleMouseEnter = (event) => {
  if (props.disabled || props.loading) return
  
  const button = event.currentTarget
  const rect = button.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  
  rippleStyle.value = {
    left: `${x}px`,
    top: `${y}px`,
    opacity: '0.3',
    transform: 'scale(0)',
  }
  
  rippleVisible.value = true
  
  setTimeout(() => {
    rippleStyle.value = {
      ...rippleStyle.value,
      opacity: '0',
      transform: 'scale(2)',
      transition: 'all 0.6s ease-out',
    }
  }, 10)
}

const handleMouseLeave = () => {
  if (props.disabled || props.loading) return
  
  setTimeout(() => {
    rippleVisible.value = false
    rippleStyle.value = {}
  }, 600)
}
</script>

<style scoped>
/* 基础按钮样式 */
.app-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-normal) cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  text-decoration: none;
}

.app-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.app-button:disabled,
.app-button.app-button--disabled {
  cursor: not-allowed;
  opacity: 0.6;
  transform: none !important;
  box-shadow: none !important;
}

/* 尺寸变体 */
.app-button--small {
  padding: 4px 12px;
  font-size: 12px;
  height: 28px;
  border-radius: var(--radius-sm);
}

.app-button--medium {
  padding: 8px 16px;
  font-size: 14px;
  height: 36px;
  border-radius: var(--radius-md);
}

.app-button--large {
  padding: 12px 24px;
  font-size: 16px;
  height: 44px;
  border-radius: var(--radius-lg);
}

.app-button--icon {
  padding: 8px;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
}

.app-button--icon.app-button--small {
  width: 28px;
  height: 28px;
}

.app-button--icon.app-button--large {
  width: 44px;
  height: 44px;
}

/* 颜色变体 */
.app-button--primary {
  background-color: var(--primary-color);
  color: white;
  box-shadow: var(--shadow-sm);
}

.app-button--primary:hover:not(:disabled) {
  background-color: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.app-button--primary:active:not(:disabled) {
  background-color: var(--primary-active);
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

.app-button--success {
  background-color: var(--color-success);
  color: white;
  box-shadow: var(--shadow-sm);
}

.app-button--success:hover:not(:disabled) {
  background-color: var(--color-success-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.app-button--warning {
  background-color: var(--color-warning);
  color: white;
  box-shadow: var(--shadow-sm);
}

.app-button--warning:hover:not(:disabled) {
  background-color: var(--color-warning-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.app-button--danger {
  background-color: var(--color-danger);
  color: white;
  box-shadow: var(--shadow-sm);
}

.app-button--danger:hover:not(:disabled) {
  background-color: var(--color-danger-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.app-button--info {
  background-color: var(--color-info);
  color: white;
  box-shadow: var(--shadow-sm);
}

.app-button--info:hover:not(:disabled) {
  background-color: var(--color-info-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.app-button--secondary {
  background-color: var(--color-bg-secondary);
  color: var(--color-text-primary);
  border-color: var(--color-border-primary);
  box-shadow: var(--shadow-sm);
}

.app-button--secondary:hover:not(:disabled) {
  background-color: var(--color-bg-tertiary);
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.app-button--outline {
  background-color: transparent;
  color: var(--color-primary);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.app-button--outline:hover:not(:disabled) {
  background-color: var(--color-primary-light);
  border-color: var(--color-primary-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.app-button--text {
  background-color: transparent;
  color: var(--color-primary);
  border-color: transparent;
  box-shadow: none;
}

.app-button--text:hover:not(:disabled) {
  background-color: var(--color-primary-light);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

/* 图标样式 */
.app-button__icon {
  font-size: 16px;
  line-height: 1;
}

.app-button--small .app-button__icon {
  font-size: 12px;
}

.app-button--large .app-button__icon {
  font-size: 18px;
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
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.app-button--secondary .app-button__spinner,
.app-button--outline .app-button__spinner,
.app-button--text .app-button__spinner {
  border-color: rgba(0, 0, 0, 0.3);
  border-top-color: var(--color-text-primary);
}

.dark-mode .app-button--secondary .app-button__spinner,
.dark-mode .app-button--outline .app-button__spinner,
.dark-mode .app-button--text .app-button__spinner {
  border-color: rgba(255, 255, 255, 0.3);
  border-top-color: var(--color-text-primary);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 波纹效果 */
.app-button__ripple {
  position: absolute;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  transform: scale(0);
  animation: ripple-animation 0.6s ease-out;
  pointer-events: none;
  width: 20px;
  height: 20px;
  margin-left: -10px;
  margin-top: -10px;
}

.app-button--secondary .app-button__ripple {
  background-color: rgba(0, 0, 0, 0.1);
}

.app-button--outline .app-button__ripple {
  background-color: rgba(22, 119, 255, 0.2);
}

.app-button--text .app-button__ripple {
  background-color: rgba(22, 119, 255, 0.2);
}

.dark-mode .app-button__ripple {
  background-color: rgba(255, 255, 255, 0.3);
}

.dark-mode .app-button--secondary .app-button__ripple {
  background-color: rgba(255, 255, 255, 0.1);
}

@keyframes ripple-animation {
  to {
    transform: scale(4);
    opacity: 0;
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .app-button--large {
    padding: 10px 20px;
    height: 40px;
  }
  
  .app-button {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .app-button {
    padding: 6px 12px;
    font-size: 12px;
  }
  
  .app-button--large {
    padding: 8px 16px;
    height: 36px;
  }
}
</style>
