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
      <button
        :class="['tab-button', { active: activeTab === 'images' }]"
        @click="activeTab = 'images'"
      >
        <span class="tab-icon">🖼️</span>
        <span class="tab-text">图片管理</span>
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

      <!-- 图片管理页面 -->
      <div v-show="activeTab === 'images'" class="images-section">
        <div class="header">
          <h1>🖼️ 文件图片管理</h1>
        </div>

        <div class="search-bar">
          <input
            v-model="searchFileMd5"
            type="text"
            placeholder="输入文件MD5搜索..."
            @keyup.enter="handleSearch"
            class="search-input"
          />
          <button @click="handleSearch" class="btn-search">🔍 搜索</button>
          <button
            v-if="currentFileMd5"
            @click="handleRefresh"
            class="btn-refresh"
          >
            🔄 刷新
          </button>
        </div>

        <div v-if="currentFileMd5" class="stats-bar">
          <span
            >文件MD5: <strong>{{ currentFileMd5 }}</strong></span
          >
          <span
            >总图片数: <strong>{{ totalCount }}</strong></span
          >
          <span
            >当前加载: <strong>{{ imagesList.length }}</strong></span
          >
          <button
            @click="handleDeleteAll"
            class="btn-delete-all"
            :disabled="imagesList.length === 0"
          >
            🗑️ 删除该文件所有图片
          </button>
        </div>

        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <div
          v-else-if="imagesList.length === 0 && currentFileMd5"
          class="empty"
        >
          <p>📭 暂无图片数据</p>
        </div>

        <div v-else-if="imagesList.length > 0" class="table-container">
          <table class="images-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>图片路径</th>
                <th>页面编号</th>
                <th>提取坐标</th>
                <th>模型版本</th>
                <th>用户ID</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="image in imagesList" :key="image.id">
                <td>{{ image.id }}</td>
                <td class="image-path">
                  <span :title="image.imagePath">{{
                    formatImagePath(image.imagePath)
                  }}</span>
                </td>
                <td>{{ image.pageNumber || '-' }}</td>
                <td class="bbox-info">
                  <button
                    @click="showBboxInfo(image)"
                    class="btn-view-bbox"
                    title="查看坐标"
                  >
                    📍
                  </button>
                </td>
                <td>{{ image.modelVersion || '-' }}</td>
                <td>{{ image.userId || '-' }}</td>
                <td>{{ formatDate(image.createdAt) }}</td>
                <td class="actions">
                  <button
                    @click="viewImage(image)"
                    class="btn-icon btn-view-image"
                    title="查看图片"
                  >
                    👁️
                  </button>
                  <button
                    @click="handleDelete(image)"
                    class="btn-icon btn-delete"
                    title="删除"
                  >
                    🗑️
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页加载 -->
        <div v-if="imagesList.length > 0 && hasMore" class="pagination">
          <button @click="loadMore" class="btn-load-more" :disabled="loading">
            {{ loading ? '加载中...' : '加载更多' }}
          </button>
        </div>

        <!-- 图片查看对话框 -->
        <div
          v-if="showImageDialog"
          class="dialog-overlay"
          @click="closeImageDialog"
        >
          <div class="dialog-content dialog-large" @click.stop>
            <div class="dialog-header">
              <h3>🖼️ 图片详情</h3>
              <button @click="closeImageDialog" class="btn-close">✕</button>
            </div>
            <div class="dialog-body">
              <div class="info-row">
                <span class="label">图片ID:</span>
                <span class="value">{{ selectedImage?.id }}</span>
              </div>
              <div class="info-row">
                <span class="label">文件MD5:</span>
                <span class="value">{{ selectedImage?.fileMd5 }}</span>
              </div>
              <div class="info-row">
                <span class="label">页面编号:</span>
                <span class="value">{{
                  selectedImage?.pageNumber || '-'
                }}</span>
              </div>
              <div class="image-preview">
                <img
                  :src="selectedImage?.imagePath"
                  :alt="'图片 ' + selectedImage?.id"
                  class="preview-img"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 坐标信息对话框 -->
        <div
          v-if="showBboxDialog"
          class="dialog-overlay"
          @click="closeBboxDialog"
        >
          <div class="dialog-content" @click.stop>
            <div class="dialog-header">
              <h3>📍 提取坐标信息</h3>
              <button @click="closeBboxDialog" class="btn-close">✕</button>
            </div>
            <div class="dialog-body">
              <div class="info-row">
                <span class="label">图片ID:</span>
                <span class="value">{{ selectedImage?.id }}</span>
              </div>
              <div class="bbox-data">
                <pre>{{
                  JSON.stringify(selectedImage?.extractionBbox, null, 2)
                }}</pre>
              </div>
            </div>
          </div>
        </div>

        <!-- 删除确认对话框 -->
        <div
          v-if="showDeleteDialog"
          class="dialog-overlay"
          @click="closeDeleteDialog"
        >
          <div class="dialog-content dialog-small" @click.stop>
            <div class="dialog-header">
              <h3>⚠️ 确认删除</h3>
              <button @click="closeDeleteDialog" class="btn-close">✕</button>
            </div>
            <div class="dialog-body">
              <p>确定要删除此图片吗？</p>
              <p class="warning-text">图片ID: {{ imageToDelete?.id }}</p>
              <div class="dialog-actions">
                <button @click="closeDeleteDialog" class="btn-cancel">
                  取消
                </button>
                <button @click="confirmDelete" class="btn-confirm">
                  确认删除
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 批量删除确认对话框 -->
        <div
          v-if="showDeleteAllDialog"
          class="dialog-overlay"
          @click="closeDeleteAllDialog"
        >
          <div class="dialog-content dialog-small" @click.stop>
            <div class="dialog-header">
              <h3>⚠️ 确认批量删除</h3>
              <button @click="closeDeleteAllDialog" class="btn-close">✕</button>
            </div>
            <div class="dialog-body">
              <p>确定要删除该文件的所有图片吗？</p>
              <p class="warning-text">文件MD5: {{ currentFileMd5 }}</p>
              <p class="warning-text">图片总数: {{ totalCount }}</p>
              <p class="danger-text">⚠️ 此操作不可恢复！</p>
              <div class="dialog-actions">
                <button @click="closeDeleteAllDialog" class="btn-cancel">
                  取消
                </button>
                <button
                  @click="confirmDeleteAll"
                  class="btn-confirm btn-danger"
                >
                  确认删除全部
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 提示消息 -->
        <div v-if="message" :class="['message', messageType]">
          {{ message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import FileUploader from '../components/file/FileUploader.vue'
import FileManager from '../components/file/FileManager.vue'
import {
  getFileExtractedImagesByMd5,
  deleteFileExtractedImageById,
  deleteFileExtractedImagesByMd5,
} from '../api/fileExtractedImagesApi'

const activeTab = ref('upload')
const fileManager = ref(null)

// 图片管理相关数据
const searchFileMd5 = ref('')
const currentFileMd5 = ref('')
const imagesList = ref([])
const totalCount = ref(0)
const currentPage = ref(0)
const pageSize = ref(50)
const hasMore = ref(true)
const loading = ref(false)

// 对话框
const showImageDialog = ref(false)
const showBboxDialog = ref(false)
const showDeleteDialog = ref(false)
const showDeleteAllDialog = ref(false)
const selectedImage = ref(null)
const imageToDelete = ref(null)

// 提示消息
const message = ref('')
const messageType = ref('info')

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

// 搜索图片
const handleSearch = async () => {
  if (!searchFileMd5.value.trim()) {
    showMessage('请输入文件MD5', 'error')
    return
  }
  currentFileMd5.value = searchFileMd5.value.trim()
  imagesList.value = []
  currentPage.value = 0
  hasMore.value = true
  await loadImages()
  await loadImageCount()
}

// 刷新图片列表
const handleRefresh = async () => {
  imagesList.value = []
  currentPage.value = 0
  hasMore.value = true
  await loadImages()
  await loadImageCount()
}

// 加载图片数据
const loadImages = async () => {
  if (!currentFileMd5.value || loading.value) return

  loading.value = true
  try {
    const response = await getFileExtractedImagesByMd5(currentFileMd5.value)
    if (response.data.code === 200) {
      imagesList.value = response.data.data
      totalCount.value = response.data.total
      hasMore.value = false
    } else {
      showMessage('加载失败: ' + response.data.message, 'error')
    }
  } catch (error) {
    console.error('加载图片失败:', error)
    showMessage('加载失败: ' + (error.message || '网络错误'), 'error')
  } finally {
    loading.value = false
  }
}

// 加载图片总数
const loadImageCount = async () => {
  // 图片总数已在 loadImages 中从 API 响应获取
  return
}

// 加载更多图片
const loadMore = () => {
  loadImages()
}

// 查看图片
const viewImage = (image) => {
  selectedImage.value = image
  showImageDialog.value = true
}

const closeImageDialog = () => {
  showImageDialog.value = false
  selectedImage.value = null
}

// 查看坐标信息
const showBboxInfo = (image) => {
  selectedImage.value = image
  showBboxDialog.value = true
}

const closeBboxDialog = () => {
  showBboxDialog.value = false
  selectedImage.value = null
}

// 删除单个图片
const handleDelete = (image) => {
  imageToDelete.value = image
  showDeleteDialog.value = true
}

const closeDeleteDialog = () => {
  showDeleteDialog.value = false
  imageToDelete.value = null
}

const confirmDelete = async () => {
  if (!imageToDelete.value) return

  try {
    const response = await deleteFileExtractedImageById(imageToDelete.value.id)
    if (response.data.code === 200) {
      showMessage('删除成功', 'success')
      // 从列表中移除
      imagesList.value = imagesList.value.filter(
        (img) => img.id !== imageToDelete.value.id
      )
      totalCount.value--
      closeDeleteDialog()
    } else {
      showMessage('删除失败: ' + response.data.message, 'error')
    }
  } catch (error) {
    console.error('删除图片失败:', error)
    showMessage('删除失败: ' + (error.message || '网络错误'), 'error')
  }
}

// 删除全部图片
const handleDeleteAll = () => {
  showDeleteAllDialog.value = true
}

const closeDeleteAllDialog = () => {
  showDeleteAllDialog.value = false
}

const confirmDeleteAll = async () => {
  if (!currentFileMd5.value) return

  try {
    const response = await deleteFileExtractedImagesByMd5(currentFileMd5.value)
    if (response.data.code === 200) {
      showMessage('批量删除成功', 'success')
      imagesList.value = []
      totalCount.value = 0
      hasMore.value = false
      closeDeleteAllDialog()
    } else {
      showMessage('删除失败: ' + response.data.message, 'error')
    }
  } catch (error) {
    console.error('批量删除图片失败:', error)
    showMessage('删除失败: ' + (error.message || '网络错误'), 'error')
  }
}

// 工具函数
const formatImagePath = (path) => {
  if (!path) return '-'
  return path.length > 50 ? path.substring(0, 50) + '...' : path
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const showMessage = (msg, type = 'info') => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

// 从URL参数获取初始值
onMounted(() => {
  const params = new URLSearchParams(window.location.search)
  const tab = params.get('tab')
  const fileMd5 = params.get('fileMd5')

  if (tab === 'images') {
    activeTab.value = 'images'
    if (fileMd5) {
      searchFileMd5.value = fileMd5
      handleSearch()
    }
  }
})
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

/* 图片管理页面样式 */
.images-section {
  animation: fadeIn 0.4s ease-in-out;
}

.images-section .header {
  margin-bottom: 30px;
  text-align: left;
}

.images-section .header h1 {
  color: #333;
  font-size: 24px;
  margin: 0;
  text-shadow: none;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
}

.btn-search,
.btn-refresh {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn-search {
  background: #007bff;
  color: white;
}

.btn-search:hover {
  background: #0056b3;
}

.btn-refresh {
  background: #28a745;
  color: white;
}

.btn-refresh:hover {
  background: #218838;
}

.stats-bar {
  display: flex;
  gap: 20px;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 20px;
}

.stats-bar span {
  font-size: 14px;
  color: #666;
}

.stats-bar strong {
  color: #333;
  font-weight: 600;
}

.btn-delete-all {
  margin-left: auto;
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
}

.btn-delete-all:hover:not(:disabled) {
  background: #c82333;
}

.btn-delete-all:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading {
  text-align: center;
  padding: 60px 0;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.empty {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 16px;
}

.table-container {
  overflow-x: auto;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.images-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1000px;
}

.images-table th,
.images-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.images-table th {
  background: #f8f9fa;
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.images-table tbody tr:hover {
  background: #f8f9ff;
}

.image-path {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bbox-info {
  width: 80px;
}

.btn-view-bbox {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.btn-view-bbox:hover {
  background: #f0f0f0;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 18px;
  border-radius: 8px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-icon:hover {
  background: #f0f0f0;
  transform: scale(1.1);
}

.btn-view-image:hover {
  background: #e3f2fd;
}

.btn-delete:hover {
  background: #ffebee;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}

.btn-load-more {
  padding: 10px 24px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-load-more:hover:not(:disabled) {
  background: #5a6268;
}

.btn-load-more:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 对话框样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
}

.dialog-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: dialogIn 0.3s ease-out;
}

.dialog-large {
  max-width: 800px;
}

.dialog-small {
  max-width: 450px;
}

@keyframes dialogIn {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.dialog-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 20px;
  color: #999;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #f0f0f0;
  color: #333;
}

.dialog-body {
  padding: 24px;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
}

.info-row .label {
  width: 100px;
  font-weight: 600;
  color: #666;
}

.info-row .value {
  flex: 1;
  color: #333;
}

.image-preview {
  margin-top: 20px;
  text-align: center;
}

.preview-img {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.bbox-data {
  margin-top: 20px;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}

.bbox-data pre {
  margin: 0;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  color: #333;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.btn-cancel,
.btn-confirm {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-confirm {
  background: #007bff;
  color: white;
}

.btn-confirm:hover {
  background: #0056b3;
}

.btn-confirm.btn-danger {
  background: #dc3545;
}

.btn-confirm.btn-danger:hover {
  background: #c82333;
}

.warning-text {
  color: #ff5722;
  font-size: 14px;
  margin: 8px 0;
}

.danger-text {
  color: #dc3545;
  font-size: 14px;
  font-weight: 600;
  margin: 8px 0;
}

.message {
  position: fixed;
  top: 80px;
  right: 30px;
  padding: 16px 24px;
  border-radius: 8px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.message.error {
  background: #ffebee;
  color: #c62828;
  border-left: 4px solid #c62828;
}

.message.success {
  background: #e8f5e9;
  color: #2e7d32;
  border-left: 4px solid #2e7d32;
}

.message.info {
  background: #e3f2fd;
  color: #1565c0;
  border-left: 4px solid #1565c0;
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

  .search-bar {
    flex-direction: column;
  }

  .stats-bar {
    flex-wrap: wrap;
    gap: 10px;
  }

  .btn-delete-all {
    margin-left: 0;
    width: 100%;
    text-align: center;
  }

  .table-container {
    margin-bottom: 20px;
  }

  .images-table {
    min-width: 800px;
  }

  .dialog-content {
    width: 95%;
  }

  .message {
    right: 10px;
    left: 10px;
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

  .images-section .header h1 {
    font-size: 20px;
  }

  .info-row {
    flex-direction: column;
  }

  .info-row .label {
    width: 100%;
    margin-bottom: 4px;
  }

  .dialog-actions {
    flex-direction: column;
  }

  .btn-cancel,
  .btn-confirm {
    width: 100%;
  }
}
</style>
