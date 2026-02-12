<template>
  <div class="file-manager">
    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <button @click="refreshList" class="btn-action btn-refresh">
          <span class="icon">🔄</span>
          <span>刷新列表</span>
        </button>
        <button
          v-if="processTasks.size > 0"
          @click="showProcessProgress = true"
          class="btn-action btn-progress"
        >
          <span class="icon">📊</span>
          <span>查看进度 ({{ processTasks.size }})</span>
        </button>
      </div>
      <div class="file-count" v-if="!loading">
        共 <strong>{{ fileList.length }}</strong> 个文件
      </div>
    </div>

    <!-- 文件列表 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="fileList.length === 0" class="empty">
      <div class="empty-icon">📭</div>
      <p>暂无文件</p>
      <p class="empty-hint">快去上传一些文件吧！</p>
    </div>

    <div v-else class="file-list">
      <table>
        <thead>
          <tr>
            <th>文件名</th>
            <th>大小</th>
            <th>状态</th>
            <th>公开</th>
            <th>上传时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="file in fileList" :key="file.fileMd5" class="file-row">
            <td class="file-name-cell">
              <div v-if="editingFile === file.fileMd5" class="edit-name">
                <input
                  v-model="newFileName"
                  @keyup.enter="saveFileName(file.fileMd5)"
                  @keyup.esc="cancelEdit"
                  class="edit-input"
                  autofocus
                />
                <button
                  @click="saveFileName(file.fileMd5)"
                  class="btn-mini btn-save"
                >
                  ✓
                </button>
                <button @click="cancelEdit" class="btn-mini btn-cancel-edit">
                  ✕
                </button>
              </div>
              <div v-else class="file-name">
                <span class="file-icon">📄</span>
                <span class="name-text">{{ file.fileName }}</span>
              </div>
            </td>
            <td>{{ formatFileSize(file.totalSize) }}</td>
            <td>
              <span :class="['status-badge', getStatusClass(file.status)]">
                {{ getStatusText(file.status) }}
              </span>
            </td>
            <td>
              <label class="switch">
                <input
                  type="checkbox"
                  :checked="file.isPublic"
                  @change="togglePublic(file)"
                />
                <span class="slider"></span>
              </label>
            </td>
            <td class="date-cell">{{ formatDate(file.createdAt) }}</td>
            <td class="actions">
              <button
                @click="handleParse(file)"
                class="btn-icon btn-parse"
                title="解析文档"
              >
                🔍
              </button>
              <button
                @click="viewVectors(file)"
                class="btn-icon btn-view"
                title="查看向量"
              >
                📊
              </button>
              <button
                @click="viewEsDoc(file)"
                class="btn-icon btn-es"
                title="查看ES文档"
              >
                📦
              </button>
              <button
                @click="viewImages(file)"
                class="btn-icon btn-images"
                title="查看图片"
              >
                🖼️
              </button>
              <button
                @click="startEdit(file)"
                class="btn-icon btn-edit"
                title="重命名"
              >
                ✏️
              </button>
              <button
                @click="handleDelete(file)"
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
            <p>确定要删除文件吗？</p>
            <p class="file-to-delete">"{{ fileToDelete?.fileName }}"</p>
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

    <!-- 文件处理进度侧边栏 -->
    <transition name="sidebar">
      <div
        v-if="showProcessProgress"
        class="sidebar-overlay"
        @click="closeProcessProgress"
      >
        <div class="sidebar" @click.stop>
          <div class="sidebar-header">
            <h3>📊 文件处理进度 ({{ processTasks.size }})</h3>
            <button @click="closeProcessProgress" class="btn-close">✕</button>
          </div>
          <div class="sidebar-body">
            <div
              v-for="[fileMd5, task] in processTasks"
              :key="fileMd5"
              class="task-item"
            >
              <div class="task-header">
                <span class="task-filename">{{ task.fileName }}</span>
                <button
                  @click="closeTaskProgress(fileMd5)"
                  class="btn-close-small"
                >
                  ✕
                </button>
              </div>
              <FileProcessProgress
                :fileName="task.fileName"
                :progress="task.progress"
                :status="task.status"
                :message="task.message"
                :errorTextList="task.errorTextList"
                :errorImageList="task.errorImageList"
              />
            </div>
            <div v-if="processTasks.size === 0" class="no-tasks">
              <p>没有正在处理的任务</p>
            </div>
            <div v-if="processTasks.size > 0" class="sidebar-footer">
              <button @click="cancelAllTasks" class="btn-cancel-all">
                取消所有任务
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import {
  getFileList,
  deleteFile,
  updateFileName,
  updateIsPublic,
  parseDocument,
} from '@/api/fileManageApi'
import FileProcessProgress from './FileProcessProgress.vue'
import { subscribeFileProcess } from '@/api/fileUploadApi'

const router = useRouter()

// 响应式数据
const fileList = ref([])
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const editingFile = ref(null)
const newFileName = ref('')
const showDeleteDialog = ref(false)
const fileToDelete = ref(null)

// 文件处理进度相关 - 支持并发多个文件
const processTasks = ref(new Map())
const showProcessProgress = ref(false)

// 获取用户ID
const getUserId = () => {
  return localStorage.getItem('userId') || 'default-user'
}

// 加载文件列表
const loadFileList = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await getFileList(getUserId())
    if (response.data.code === 200) {
      fileList.value = response.data.data || []
    } else {
      errorMessage.value = response.data.message || '加载失败'
      setTimeout(() => (errorMessage.value = ''), 3000)
    }
  } catch (error) {
    console.error('加载文件列表失败:', error)
    errorMessage.value = '加载失败: ' + error.message
    setTimeout(() => (errorMessage.value = ''), 3000)
  } finally {
    loading.value = false
  }
}

// 刷新列表
const refreshList = () => {
  loadFileList()
  successMessage.value = ''
  errorMessage.value = ''
}

// 开始编辑文件名
const startEdit = (file) => {
  editingFile.value = file.fileMd5
  newFileName.value = file.fileName
}

// 取消编辑
const cancelEdit = () => {
  editingFile.value = null
  newFileName.value = ''
}

// 保存文件名
const saveFileName = async (fileMd5) => {
  if (!newFileName.value.trim()) {
    errorMessage.value = '文件名不能为空'
    setTimeout(() => (errorMessage.value = ''), 3000)
    return
  }

  try {
    const response = await updateFileName(
      fileMd5,
      newFileName.value,
      getUserId(),
    )
    if (response.data.code === 200) {
      successMessage.value = '重命名成功'
      setTimeout(() => (successMessage.value = ''), 3000)
      editingFile.value = null
      await loadFileList()
    } else {
      errorMessage.value = response.data.message || '重命名失败'
      setTimeout(() => (errorMessage.value = ''), 3000)
    }
  } catch (error) {
    errorMessage.value = '重命名失败: ' + error.message
    setTimeout(() => (errorMessage.value = ''), 3000)
  }
}

// 切换公开状态
const togglePublic = async (file) => {
  try {
    const response = await updateIsPublic(
      file.fileMd5,
      !file.isPublic,
      getUserId(),
    )
    if (response.data.code === 200) {
      successMessage.value = '更新成功'
      setTimeout(() => (successMessage.value = ''), 3000)
      await loadFileList()
    } else {
      errorMessage.value = response.data.message || '更新失败'
      setTimeout(() => (errorMessage.value = ''), 3000)
    }
  } catch (error) {
    errorMessage.value = '更新失败: ' + error.message
    setTimeout(() => (errorMessage.value = ''), 3000)
  }
}

// 解析文档
const handleParse = async (file) => {
  try {
    successMessage.value = '正在解析文档，请稍候...'
    const response = await parseDocument(file.fileMd5, getUserId())
    if (response.data) {
      successMessage.value = '文档解析请求已提交'
      setTimeout(() => (successMessage.value = ''), 3000)

      // 订阅文件处理通知
      subscribeFileProcessNotification(file)
    } else {
      errorMessage.value = response.data || '解析失败'
      setTimeout(() => (errorMessage.value = ''), 3000)
    }
  } catch (error) {
    errorMessage.value = '解析失败: ' + (error.response?.data || error.message)
    setTimeout(() => (errorMessage.value = ''), 3000)
  }
}

// 订阅文件处理通知
const subscribeFileProcessNotification = (file) => {
  const fileMd5 = file.fileMd5

  // 如果该文件已有任务，先关闭
  if (processTasks.value.has(fileMd5)) {
    const existingTask = processTasks.value.get(fileMd5)
    console.log('[文件管理] 关闭旧的 SSE 连接:', fileMd5)
    if (existingTask.eventSource) {
      existingTask.eventSource.close()
    }
  }

  // 创建新任务
  const task = {
    fileMd5: fileMd5,
    fileName: file.fileName,
    progress: 0,
    status: 'PENDING',
    message: '等待处理...',
    errorTextList: [],
    errorImageList: [],
    eventSource: null,
  }

  processTasks.value.set(fileMd5, task)
  showProcessProgress.value = true

  console.log('[文件管理] 开始订阅 SSE，fileMd5:', fileMd5)

  const eventSource = subscribeFileProcess(
    getUserId(),
    fileMd5,
    (notification) => {
      console.log('[文件管理] 收到通知:', fileMd5, notification)

      const task = processTasks.value.get(fileMd5)
      if (task) {
        task.progress = notification.progress || 0
        task.status = notification.status || 'PENDING'
        task.message = notification.message || ''

        if (notification.errorTextList) {
          task.errorTextList = notification.errorTextList
        }

        if (notification.errorImageList) {
          task.errorImageList = notification.errorImageList
        }
      }
    },
    (error) => {
      console.error('[文件管理] 连接错误:', fileMd5, error)
      const task = processTasks.value.get(fileMd5)
      if (task) {
        task.status = 'FAILED'
        task.message = '连接失败，请刷新页面重试'
      }
    },
    (notification) => {
      console.log('[文件管理] 完成:', fileMd5, notification)
      const task = processTasks.value.get(fileMd5)
      if (notification.status === 'SUCCESS') {
        successMessage.value = `${file.fileName} 处理成功！`
        setTimeout(() => (successMessage.value = ''), 3000)
        // 刷新文件列表
        loadFileList()
      } else if (notification.status === 'FAILED') {
        errorMessage.value = `${file.fileName} 处理失败: ${notification.message || '未知错误'}`
        setTimeout(() => (errorMessage.value = ''), 3000)
      }
      // 处理完成后清理 eventSource，但保留任务信息
      if (task && task.eventSource) {
        task.eventSource.close()
        task.eventSource = null
      }
    },
  )

  task.eventSource = eventSource
}

// 关闭单个任务进度追踪
const closeTaskProgress = (fileMd5) => {
  const task = processTasks.value.get(fileMd5)
  if (task) {
    if (task.eventSource) {
      task.eventSource.close()
    }
    processTasks.value.delete(fileMd5)
  }

  // 如果没有任务了，隐藏侧边栏
  if (processTasks.value.size === 0) {
    showProcessProgress.value = false
  }
}

// 关侧边栏（不删除任务）
const closeProcessProgress = () => {
  showProcessProgress.value = false
}

// 取消所有任务
const cancelAllTasks = () => {
  processTasks.value.forEach((task, fileMd5) => {
    if (task.eventSource) {
      task.eventSource.close()
    }
  })
  processTasks.value.clear()
  showProcessProgress.value = false
}

// 查看文件向量
const viewVectors = (file) => {
  router.push({
    path: '/vector-manage',
    query: { fileMd5: file.fileMd5 },
  })
}

// 查看ES文档
const viewEsDoc = (file) => {
  router.push({
    path: '/es-doc-manage',
    query: { fileMd5: file.fileMd5 },
  })
}

// 查看图片
const viewImages = (file) => {
  router.push({
    path: '/image-manage',
    query: { fileMd5: file.fileMd5 },
  })
}

// 显示删除对话框
const handleDelete = (file) => {
  fileToDelete.value = file
  showDeleteDialog.value = true
}

// 关闭删除对话框
const closeDeleteDialog = () => {
  showDeleteDialog.value = false
  fileToDelete.value = null
}

// 确认删除
const confirmDelete = async () => {
  if (!fileToDelete.value) return

  try {
    const response = await deleteFile(fileToDelete.value.fileMd5, getUserId())
    if (response.data.code === 200) {
      successMessage.value = '删除成功'
      setTimeout(() => (successMessage.value = ''), 3000)
      closeDeleteDialog()
      await loadFileList()
    } else {
      errorMessage.value = response.data.message || '删除失败'
      setTimeout(() => (errorMessage.value = ''), 3000)
      closeDeleteDialog()
    }
  } catch (error) {
    errorMessage.value = '删除失败: ' + error.message
    setTimeout(() => (errorMessage.value = ''), 3000)
    closeDeleteDialog()
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

// 格式化日期
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

// 获取状态文本
const getStatusText = (status) => {
  return status === 1 ? '已完成' : '上传中'
}

// 获取状态样式
const getStatusClass = (status) => {
  return status === 1 ? 'status-success' : 'status-uploading'
}

// 暴露刷新方法给父组件
defineExpose({
  refreshList,
})

// 组件挂载时加载列表
onMounted(() => {
  loadFileList()
})

// 组件卸载前清理资源
onBeforeUnmount(() => {
  processTasks.value.forEach((task, fileMd5) => {
    if (task.eventSource) {
      console.log('[文件管理] 组件卸载，关闭 SSE 连接:', fileMd5)
      task.eventSource.close()
    }
  })
  processTasks.value.clear()
})
</script>

<style scoped>
.file-manager {
  width: 100%;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e0e0e0;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-action {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-refresh {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.btn-refresh:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.btn-progress {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
}

.btn-progress:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(240, 147, 251, 0.3);
}

.btn-progress .icon {
  display: inline-block;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.btn-refresh .icon {
  display: inline-block;
  animation: rotate 2s linear infinite paused;
}

.btn-refresh:hover .icon {
  animation-play-state: running;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.file-count {
  color: #666;
  font-size: 14px;
}

.file-count strong {
  color: #667eea;
  font-size: 18px;
}

/* 加载状态 */
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
  border-top: 4px solid #667eea;
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

/* 空状态 */
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

.empty p {
  font-size: 18px;
  margin: 10px 0;
}

.empty-hint {
  font-size: 14px;
  color: #bbb;
}

/* 文件列表 */
.file-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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

.file-name-cell {
  max-width: 300px;
}

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  font-size: 20px;
}

.name-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.edit-name {
  display: flex;
  gap: 6px;
  align-items: center;
}

.edit-input {
  flex: 1;
  padding: 6px 10px;
  border: 2px solid #667eea;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.btn-mini {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.2s;
}

.btn-save {
  background: #4caf50;
  color: white;
}

.btn-save:hover {
  background: #45a049;
}

.btn-cancel-edit {
  background: #f44336;
  color: white;
}

.btn-cancel-edit:hover {
  background: #da190b;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-success {
  background: #e8f5e9;
  color: #4caf50;
}

.status-uploading {
  background: #fff3e0;
  color: #ff9800;
}

/* Switch 开关 */
.switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.3s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: '';
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #667eea;
}

input:checked + .slider:before {
  transform: translateX(24px);
}

.date-cell {
  color: #888;
  font-size: 13px;
}

/* 操作按钮 */
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

.btn-edit:hover {
  background: #e3f2fd;
}

.btn-parse:hover {
  background: #fff3e0;
}

.btn-view:hover {
  background: #e8f5e9;
}

.btn-es:hover {
  background: #e1f5fe;
}

.btn-images:hover {
  background: #fce4ec;
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

/* 侧边栏 */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 2000;
}

.sidebar {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  width: 500px;
  background: white;
  box-shadow: -5px 0 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  animation: slideInRight 0.3s ease-out;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}

.sidebar-enter-active,
.sidebar-leave-active {
  transition: all 0.3s;
}

.sidebar-enter-from,
.sidebar-leave-to {
  transform: translateX(100%);
}

.sidebar-header {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sidebar-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.task-item {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e4e7ed;
}

.task-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.task-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.task-filename {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  max-width: 350px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-close-small {
  width: 24px;
  height: 24px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-close-small:hover {
  background: #e0e0e0;
  transform: rotate(90deg);
}

.no-tasks {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.no-tasks p {
  margin: 0;
  font-size: 14px;
}

.sidebar-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.btn-cancel-all {
  width: 100%;
  padding: 10px;
  background: #f56c6c;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-cancel-all:hover {
  background: #f78989;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
}

/* 对话框 */
.dialog {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 450px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: dialogIn 0.3s ease-out;
}

.btn-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #e0e0e0;
  transform: rotate(90deg);
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

.file-to-delete {
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

/* 响应式 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .file-list {
    overflow-x: auto;
  }

  table {
    min-width: 600px;
  }

  .message {
    right: 10px;
    left: 10px;
  }
}
</style>
