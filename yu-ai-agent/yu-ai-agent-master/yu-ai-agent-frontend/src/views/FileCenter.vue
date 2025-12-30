<template>
  <div class="file-center">
    <div class="header">
      <h1>📁 文件中心</h1>
      <p class="subtitle">统一的文件上传与管理平台</p>
    </div>

    <!-- Tab 切换 -->
    <div class="tabs">
      <button
        :class="['tab-button', { active: activeTab === 'upload' }]"
        @click="activeTab = 'upload'"
      >
        <span class="tab-icon">⬆️</span>
        <span class="tab-text">文件上传</span>
      </button>
      <button
        :class="['tab-button', { active: activeTab === 'manage' }]"
        @click="activeTab = 'manage'"
      >
        <span class="tab-icon">📋</span>
        <span class="tab-text">文件管理</span>
      </button>
    </div>

    <!-- Tab 内容 -->
    <div class="tab-content">
      <!-- 上传页面 -->
      <div v-show="activeTab === 'upload'" class="upload-section">
        <FileUploader @upload-success="handleUploadSuccess" />
      </div>

      <!-- 管理页面 -->
      <div v-show="activeTab === 'manage'" class="manage-section">
        <FileManager ref="fileManager" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import FileUploader from '../components/FileUploader.vue'
import FileManager from '../components/FileManager.vue'

const activeTab = ref('upload')
const fileManager = ref(null)

// 上传成功后切换到管理页面并刷新列表
const handleUploadSuccess = () => {
  activeTab.value = 'manage'
  // 延迟刷新以确保组件已经渲染
  setTimeout(() => {
    if (fileManager.value && fileManager.value.refreshList) {
      fileManager.value.refreshList()
    }
  }, 100)
}
</script>

<style scoped>
.file-center {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
  color: white;
}

.header h1 {
  font-size: 3rem;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  font-weight: 700;
}

.subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  letter-spacing: 1px;
}

.tabs {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.tab-button {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 40px;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 50px;
  color: white;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.tab-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
}

.tab-button.active {
  background: linear-gradient(135deg, #a8e6cf, #56c596);
  border-color: #56c596;
  box-shadow: 0 5px 25px rgba(86, 197, 150, 0.4);
  transform: translateY(-3px);
}

.tab-icon {
  font-size: 1.5rem;
}

.tab-text {
  letter-spacing: 0.5px;
}

.tab-content {
  max-width: 1400px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.2);
  min-height: 600px;
}

.upload-section,
.manage-section {
  animation: fadeIn 0.4s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header h1 {
    font-size: 2.2rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .tabs {
    gap: 10px;
  }

  .tab-button {
    padding: 12px 30px;
    font-size: 0.9rem;
  }

  .tab-icon {
    font-size: 1.3rem;
  }

  .tab-content {
    padding: 25px 20px;
  }
}

@media (max-width: 480px) {
  .file-center {
    padding: 20px 10px;
  }

  .header h1 {
    font-size: 1.8rem;
  }

  .subtitle {
    font-size: 0.9rem;
  }

  .tab-button {
    padding: 10px 20px;
    font-size: 0.85rem;
  }

  .tab-content {
    padding: 20px 15px;
  }
}
</style>
