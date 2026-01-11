<template>
  <div class="image-manage">
    <h2>🖼️ 文件图片管理</h2>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-item">
        <label>文件 MD5：</label>
        <input
          v-model="filterFileMd5"
          placeholder="输入文件MD5筛选（可选）"
          @input="applyFilter"
        />
      </div>
      <button @click="refreshList" class="btn-refresh">🔄 刷新列表</button>
      <button
        v-if="selectedImages.length > 0"
        @click="openBatchDeleteDialog"
        class="btn-batch-delete"
      >
        🗑️ 批量删除 ({{ selectedImages.length }})
      </button>
      <button
        v-if="filterFileMd5.trim()"
        @click="openDeleteFileImagesDialog"
        class="btn-delete-file"
      >
        🗑️ 删除该文件所有图片
      </button>
      <button @click="goBack" class="btn-back">⬅ 返回</button>
    </div>

    <!-- 列表 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="filteredImageList.length === 0" class="empty">
      <div class="empty-icon">📭</div>
      <p>暂无图片数据</p>
    </div>

    <div v-else class="image-list">
      <div class="list-info">
        <div class="info-left">
          <label class="select-all">
            <input
              type="checkbox"
              :checked="isAllSelected"
              @change="toggleSelectAll"
            />
            <span>全选</span>
          </label>
          <span class="count-text">
            共 <strong>{{ filteredImageList.length }}</strong> 张图片
            <span v-if="selectedImages.length > 0">
              （已选 <strong>{{ selectedImages.length }}</strong> 张）
            </span>
          </span>
        </div>
        <button
          v-if="selectedImages.length > 0"
          @click="clearSelection"
          class="btn-clear"
        >
          ✕ 清空选择
        </button>
      </div>
      <table>
        <thead>
          <tr>
            <th width="50">选择</th>
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
          <tr v-for="image in filteredImageList" :key="image.id">
            <td class="checkbox-cell">
              <input
                type="checkbox"
                :checked="isSelected(image.id)"
                @change="toggleSelect(image.id)"
              />
            </td>
            <td>{{ image.id }}</td>
            <td class="image-path">
              <span :title="image.imagePath">
                {{ formatImagePath(image.imagePath) }}
              </span>
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
                class="btn-icon btn-view-image"
                @click="viewImage(image)"
                title="查看图片"
              >
                👁️
              </button>
              <button
                class="btn-icon btn-delete"
                @click="openDeleteDialog(image)"
                title="删除"
              >
                🗑️
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 消息提示 -->
    <transition name="message">
      <div v-if="errorMessage" class="message error-message">
        <span class="message-icon">⚠️</span>
        {{ errorMessage }}
      </div>
    </transition>

    <transition name="message">
      <div v-if="successMessage" class="message success-message">
        <span class="message-icon">✓</span>
        {{ successMessage }}
      </div>
    </transition>

    <!-- 删除确认对话框 -->
    <transition name="dialog">
      <div
        v-if="showDeleteDialog"
        class="dialog-overlay"
        @click="closeDeleteDialog"
      >
        <div class="dialog" @click.stop>
          <div class="dialog-header">
            <h3>⚠️ 确认删除</h3>
          </div>
          <div class="dialog-body">
            <p>确定要删除图片吗？</p>
            <p class="image-info">图片 ID: {{ imageToDelete?.id }}</p>
            <p class="warning-text">此操作无法撤销</p>
          </div>
          <div class="dialog-actions">
            <button @click="closeDeleteDialog" class="btn-dialog btn-cancel">
              取消
            </button>
            <button @click="confirmDelete" class="btn-dialog btn-confirm">
              确定删除
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 批量删除确认对话框 -->
    <transition name="dialog">
      <div
        v-if="showBatchDeleteDialog"
        class="dialog-overlay"
        @click="closeBatchDeleteDialog"
      >
        <div class="dialog" @click.stop>
          <div class="dialog-header">
            <h3>⚠️ 确认批量删除</h3>
          </div>
          <div class="dialog-body">
            <p>确定要删除选中的图片吗？</p>
            <p class="image-info">已选中 {{ selectedImages.length }} 张图片</p>
            <p class="warning-text">此操作无法撤销</p>
          </div>
          <div class="dialog-actions">
            <button
              @click="closeBatchDeleteDialog"
              class="btn-dialog btn-cancel"
            >
              取消
            </button>
            <button @click="confirmBatchDelete" class="btn-dialog btn-confirm">
              确定删除
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 删除文件所有图片确认对话框 -->
    <transition name="dialog">
      <div
        v-if="showDeleteFileDialog"
        class="dialog-overlay"
        @click="closeDeleteFileDialog"
      >
        <div class="dialog" @click.stop>
          <div class="dialog-header">
            <h3>⚠️ 确认删除文件所有图片</h3>
          </div>
          <div class="dialog-body">
            <p>确定要删除该文件的所有图片吗？</p>
            <p class="image-info">文件MD5: {{ filterFileMd5 }}</p>
            <p class="image-info">
              图片数量: {{ filteredImageList.length }} 张
            </p>
            <p class="warning-text danger">⚠️ 此操作不可恢复！</p>
          </div>
          <div class="dialog-actions">
            <button
              @click="closeDeleteFileDialog"
              class="btn-dialog btn-cancel"
            >
              取消
            </button>
            <button
              @click="confirmDeleteFileImages"
              class="btn-dialog btn-confirm"
            >
              确定删除全部
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 图片查看对话框 -->
    <transition name="dialog">
      <div
        v-if="showImageDialog"
        class="dialog-overlay"
        @click="closeImageDialog"
      >
        <div class="dialog image-dialog" @click.stop>
          <div class="dialog-header">
            <h3>🖼️ 图片详情</h3>
          </div>
          <div class="dialog-body">
            <div class="image-detail-info">
              <div class="info-row">
                <span class="label">图片ID:</span>
                <span class="value">{{ currentImage?.id }}</span>
              </div>
              <div class="info-row">
                <span class="label">文件MD5:</span>
                <span class="value md5-value">{{ currentImage?.fileMd5 }}</span>
              </div>
              <div class="info-row">
                <span class="label">页面编号:</span>
                <span class="value">{{ currentImage?.pageNumber || '-' }}</span>
              </div>
            </div>
            <div class="image-preview">
              <img
                :src="currentImage?.imagePath"
                :alt="'图片 ' + currentImage?.id"
                class="preview-img"
              />
            </div>
          </div>
          <div class="dialog-actions">
            <button @click="closeImageDialog" class="btn-dialog btn-close">
              关闭
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 坐标信息对话框 -->
    <transition name="dialog">
      <div
        v-if="showBboxDialog"
        class="dialog-overlay"
        @click="closeBboxDialog"
      >
        <div class="dialog" @click.stop>
          <div class="dialog-header">
            <h3>📍 提取坐标信息</h3>
          </div>
          <div class="dialog-body">
            <div class="info-row">
              <span class="label">图片ID:</span>
              <span class="value">{{ currentImage?.id }}</span>
            </div>
            <div class="bbox-data">
              <pre>{{
                JSON.stringify(currentImage?.extractionBbox, null, 2)
              }}</pre>
            </div>
          </div>
          <div class="dialog-actions">
            <button @click="closeBboxDialog" class="btn-dialog btn-close">
              关闭
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getFileExtractedImagesByMd5,
  deleteFileExtractedImageById,
  deleteFileExtractedImagesByMd5,
} from '../api/fileExtractedImagesApi'

const route = useRoute()
const router = useRouter()

const imageList = ref([])
const filterFileMd5 = ref('')
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const showDeleteDialog = ref(false)
const imageToDelete = ref(null)
const showImageDialog = ref(false)
const showBboxDialog = ref(false)
const currentImage = ref(null)

// 批量操作相关
const selectedImages = ref([])
const showBatchDeleteDialog = ref(false)
const showDeleteFileDialog = ref(false)

// 根据筛选条件过滤列表
const filteredImageList = computed(() => {
  let list = imageList.value
  if (filterFileMd5.value.trim()) {
    const keyword = filterFileMd5.value.trim().toLowerCase()
    list = list.filter(
      (img) => img.fileMd5 && img.fileMd5.toLowerCase().includes(keyword)
    )
  }
  // 按ID从小到大排序
  return list.sort((a, b) => (a.id || 0) - (b.id || 0))
})

// 是否全选
const isAllSelected = computed(() => {
  return (
    filteredImageList.value.length > 0 &&
    selectedImages.value.length === filteredImageList.value.length
  )
})

// 切换全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedImages.value = []
  } else {
    selectedImages.value = filteredImageList.value.map((img) => img.id)
  }
}

// 检查是否选中
const isSelected = (imageId) => {
  return selectedImages.value.includes(imageId)
}

// 切换选中状态
const toggleSelect = (imageId) => {
  const index = selectedImages.value.indexOf(imageId)
  if (index > -1) {
    selectedImages.value.splice(index, 1)
  } else {
    selectedImages.value.push(imageId)
  }
}

// 清空选择
const clearSelection = () => {
  selectedImages.value = []
}

// 加载用户所有图片
const loadImages = async () => {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const fileMd5 = filterFileMd5.value.trim()
    if (!fileMd5) {
      errorMessage.value = '请输入文件MD5'
      setTimeout(() => (errorMessage.value = ''), 3000)
      loading.value = false
      return
    }
    const res = await getFileExtractedImagesByMd5(fileMd5)
    if (res.data.code === 200) {
      imageList.value = res.data.data || []
    } else {
      errorMessage.value = res.data.message || '加载失败'
      setTimeout(() => (errorMessage.value = ''), 3000)
    }
  } catch (e) {
    errorMessage.value = '加载失败: ' + e.message
    setTimeout(() => (errorMessage.value = ''), 3000)
  } finally {
    loading.value = false
  }
}

// 刷新列表
const refreshList = () => {
  loadImages()
  successMessage.value = ''
  errorMessage.value = ''
}

// 应用筛选
const applyFilter = () => {
  // filteredImageList 是计算属性，会自动更新
}

// 查看图片
const viewImage = (image) => {
  currentImage.value = image
  showImageDialog.value = true
}

const closeImageDialog = () => {
  showImageDialog.value = false
  currentImage.value = null
}

// 查看坐标信息
const showBboxInfo = (image) => {
  currentImage.value = image
  showBboxDialog.value = true
}

const closeBboxDialog = () => {
  showBboxDialog.value = false
  currentImage.value = null
}

// 删除单个图片
const openDeleteDialog = (image) => {
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
    const res = await deleteFileExtractedImageById(imageToDelete.value.id)
    if (res.data.code === 200) {
      successMessage.value = '删除成功'
      setTimeout(() => (successMessage.value = ''), 3000)
      closeDeleteDialog()
      await loadImages()
    } else {
      errorMessage.value = res.data.message || '删除失败'
      setTimeout(() => (errorMessage.value = ''), 3000)
    }
  } catch (e) {
    errorMessage.value = '删除失败: ' + e.message
    setTimeout(() => (errorMessage.value = ''), 3000)
  }
}

// 批量删除
const openBatchDeleteDialog = () => {
  showBatchDeleteDialog.value = true
}

const closeBatchDeleteDialog = () => {
  showBatchDeleteDialog.value = false
}

const confirmBatchDelete = async () => {
  if (selectedImages.value.length === 0) return

  try {
    const promises = selectedImages.value.map((id) =>
      deleteFileExtractedImageById(id)
    )
    await Promise.all(promises)
    successMessage.value = `成功删除 ${selectedImages.value.length} 张图片`
    setTimeout(() => (successMessage.value = ''), 3000)
    closeBatchDeleteDialog()
    selectedImages.value = []
    await loadImages()
  } catch (e) {
    errorMessage.value = '批量删除失败: ' + e.message
    setTimeout(() => (errorMessage.value = ''), 3000)
  }
}

// 删除文件所有图片
const openDeleteFileImagesDialog = () => {
  showDeleteFileDialog.value = true
}

const closeDeleteFileDialog = () => {
  showDeleteFileDialog.value = false
}

const confirmDeleteFileImages = async () => {
  if (!filterFileMd5.value.trim()) return

  try {
    const res = await deleteFileExtractedImagesByMd5(filterFileMd5.value.trim())
    if (res.data.code === 200) {
      successMessage.value = '删除成功'
      setTimeout(() => (successMessage.value = ''), 3000)
      closeDeleteFileDialog()
      await loadImages()
    } else {
      errorMessage.value = res.data.message || '删除失败'
      setTimeout(() => (errorMessage.value = ''), 3000)
    }
  } catch (e) {
    errorMessage.value = '删除失败: ' + e.message
    setTimeout(() => (errorMessage.value = ''), 3000)
  }
}

// 返回上一页
const goBack = () => {
  router.back()
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

// 组件挂载时加载列表
onMounted(() => {
  if (route.query.fileMd5) {
    filterFileMd5.value = route.query.fileMd5
    loadImages()
  }
})
</script>

<style scoped>
.image-manage {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
}

h2 {
  margin-bottom: 25px;
  color: #333;
  font-size: 28px;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 25px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-item label {
  font-weight: 500;
  color: #555;
  white-space: nowrap;
}

.filter-item input {
  padding: 10px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  min-width: 300px;
}

.filter-item input:focus {
  outline: none;
  border-color: #007bff;
}

.btn-refresh,
.btn-batch-delete,
.btn-delete-file,
.btn-back {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-refresh {
  background: #28a745;
  color: white;
}

.btn-refresh:hover {
  background: #218838;
}

.btn-batch-delete {
  background: #ffc107;
  color: #333;
}

.btn-batch-delete:hover {
  background: #e0a800;
}

.btn-delete-file {
  background: #dc3545;
  color: white;
}

.btn-delete-file:hover {
  background: #c82333;
}

.btn-back {
  background: #6c757d;
  color: white;
}

.btn-back:hover {
  background: #5a6268;
}

/* 列表 */
.loading {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.spinner {
  width: 50px;
  height: 50px;
  margin: 0 auto 20px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
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
  padding: 80px 20px;
  color: #999;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 20px;
  opacity: 0.6;
}

.image-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.list-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.info-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 500;
}

.count-text {
  color: #666;
  font-size: 14px;
}

.count-text strong {
  color: #007bff;
  font-size: 16px;
}

.btn-clear {
  padding: 6px 12px;
  background: #e0e0e0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}

.btn-clear:hover {
  background: #d0d0d0;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

th {
  padding: 16px 12px;
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.5px;
}

tbody tr {
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

tbody tr:hover {
  background: #f8f9ff;
}

tbody tr:last-child {
  border-bottom: none;
}

td {
  padding: 16px 12px;
  font-size: 14px;
  color: #333;
}

.checkbox-cell {
  width: 50px;
  text-align: center;
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

/* 消息提示 */
.message {
  position: fixed;
  top: 80px;
  right: 30px;
  padding: 16px 24px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
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

.message-enter-active,
.message-leave-active {
  transition: all 0.3s;
}

.message-enter-from,
.message-leave-to {
  transform: translateX(400px);
  opacity: 0;
}

.error-message {
  background: #ffebee;
  color: #c62828;
  border-left: 4px solid #c62828;
}

.success-message {
  background: #e8f5e9;
  color: #2e7d32;
  border-left: 4px solid #2e7d32;
}

.message-icon {
  font-size: 20px;
}

/* 对话框 */
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

.dialog {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 450px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: dialogIn 0.3s ease-out;
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

.dialog-enter-active,
.dialog-leave-active {
  transition: all 0.3s;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

.dialog-header {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-header h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dialog-body {
  padding: 24px;
}

.dialog-body p {
  margin: 8px 0;
  color: #666;
}

.image-info,
.vector-info {
  font-weight: 600;
  color: #333;
  background: #f5f5f5;
  padding: 12px;
  border-radius: 8px;
  margin: 16px 0;
  word-break: break-all;
}

.warning-text {
  color: #ff5722;
  font-size: 13px;
  font-weight: 500;
}

.warning-text.danger {
  color: #dc3545;
  font-size: 14px;
  font-weight: 600;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  padding: 16px 24px 24px;
  justify-content: flex-end;
}

.btn-dialog {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
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
  background: linear-gradient(135deg, #f44336, #e91e63);
  color: white;
}

.btn-confirm:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.3);
}

.btn-close {
  background: #6c757d;
  color: white;
}

.btn-close:hover {
  background: #5a6268;
}

/* 图片对话框 */
.image-dialog {
  max-width: 800px;
}

.image-detail-info {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.info-row .label {
  font-weight: 600;
  color: #555;
  min-width: 100px;
}

.info-row .value {
  color: #333;
}

.md5-value {
  font-family: monospace;
  font-size: 13px;
}

.image-preview {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.preview-img {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
  border-radius: 8px;
}

/* 坐标信息对话框 */
.bbox-data {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  margin-top: 15px;
}

.bbox-data pre {
  margin: 0;
  font-size: 13px;
  color: #333;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 响应式 */
@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-item input {
    min-width: 100%;
  }

  .image-list {
    overflow-x: auto;
  }

  table {
    min-width: 800px;
  }

  .message {
    right: 10px;
    left: 10px;
  }
}
</style>
