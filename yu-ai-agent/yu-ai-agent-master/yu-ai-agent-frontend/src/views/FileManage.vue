<template>
  <div class="file-manage">
    <h2>文件管理</h2>

    <!-- 操作栏 -->
    <div class="toolbar">
      <button @click="refreshList" class="btn-refresh">刷新</button>
      <button @click="$router.push('/file-upload')" class="btn-upload">
        上传新文件
      </button>
    </div>

    <!-- 文件列表 -->
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="fileList.length === 0" class="empty">暂无文件</div>

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
          <tr v-for="file in fileList" :key="file.fileMd5">
            <td>
              <div v-if="editingFile === file.fileMd5" class="edit-name">
                <input
                  v-model="newFileName"
                  @keyup.enter="saveFileName(file.fileMd5)"
                  @keyup.esc="cancelEdit"
                />
                <button @click="saveFileName(file.fileMd5)" class="btn-save">
                  保存
                </button>
                <button @click="cancelEdit" class="btn-cancel">取消</button>
              </div>
              <div v-else class="file-name">
                {{ file.fileName }}
              </div>
            </td>
            <td>{{ formatFileSize(file.totalSize) }}</td>
            <td>
              <span :class="getStatusClass(file.status)">
                {{ getStatusText(file.status) }}
              </span>
            </td>
            <td>
              <input
                type="checkbox"
                :checked="file.isPublic"
                @change="togglePublic(file)"
              />
            </td>
            <td>{{ formatDate(file.createdAt) }}</td>
            <td class="actions">
              <button
                @click="startEdit(file)"
                class="btn-action btn-edit"
                title="重命名"
              >
                ✏️
              </button>
              <button
                @click="handleDelete(file)"
                class="btn-action btn-delete"
                title="删除"
              >
                🗑️
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 错误提示 -->
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

    <!-- 成功提示 -->
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>

    <!-- 删除确认对话框 -->
    <div
      v-if="showDeleteDialog"
      class="dialog-overlay"
      @click="closeDeleteDialog"
    >
      <div class="dialog" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除文件 "{{ fileToDelete?.fileName }}" 吗？</p>
        <div class="dialog-actions">
          <button @click="confirmDelete" class="btn-confirm">确定</button>
          <button @click="closeDeleteDialog" class="btn-cancel">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getFileList,
  deleteFile,
  updateFileName,
  updateIsPublic,
} from '../api/fileManageApi'

// 响应式数据
const fileList = ref([])
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const editingFile = ref(null)
const newFileName = ref('')
const showDeleteDialog = ref(false)
const fileToDelete = ref(null)

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
      fileList.value = response.data.data
    } else {
      errorMessage.value = response.data.message || '加载失败'
    }
  } catch (error) {
    errorMessage.value = '加载失败: ' + error.message
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
    return
  }

  try {
    const response = await updateFileName(
      fileMd5,
      newFileName.value,
      getUserId()
    )
    if (response.data.code === 200) {
      successMessage.value = '重命名成功'
      setTimeout(() => (successMessage.value = ''), 3000)
      editingFile.value = null
      await loadFileList()
    } else {
      errorMessage.value = response.data.message || '重命名失败'
    }
  } catch (error) {
    errorMessage.value = '重命名失败: ' + error.message
  }
}

// 切换公开状态
const togglePublic = async (file) => {
  try {
    const response = await updateIsPublic(
      file.fileMd5,
      !file.isPublic,
      getUserId()
    )
    if (response.data.code === 200) {
      successMessage.value = '更新成功'
      setTimeout(() => (successMessage.value = ''), 3000)
      await loadFileList()
    } else {
      errorMessage.value = response.data.message || '更新失败'
    }
  } catch (error) {
    errorMessage.value = '更新失败: ' + error.message
  }
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
      closeDeleteDialog()
    }
  } catch (error) {
    errorMessage.value = '删除失败: ' + error.message
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
  return date.toLocaleString('zh-CN')
}

// 获取状态文本
const getStatusText = (status) => {
  return status === 1 ? '已完成' : '上传中'
}

// 获取状态样式
const getStatusClass = (status) => {
  return status === 1 ? 'status-success' : 'status-uploading'
}

// 组件挂载时加载列表
onMounted(() => {
  loadFileList()
})
</script>

<style scoped>
.file-manage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.btn-refresh,
.btn-upload {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-refresh {
  background: #67c23a;
  color: white;
}

.btn-refresh:hover {
  background: #85ce61;
}

.btn-upload {
  background: #409eff;
  color: white;
}

.btn-upload:hover {
  background: #66b1ff;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.file-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f5f7fa;
}

th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #606266;
  border-bottom: 2px solid #ebeef5;
}

td {
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
}

tbody tr:hover {
  background: #f5f7fa;
}

.file-name {
  font-weight: 500;
}

.edit-name {
  display: flex;
  gap: 5px;
  align-items: center;
}

.edit-name input {
  flex: 1;
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.status-success {
  color: #67c23a;
  font-weight: 500;
}

.status-uploading {
  color: #e6a23c;
  font-weight: 500;
}

.actions {
  display: flex;
  gap: 5px;
}

.btn-action {
  padding: 4px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 16px;
  transition: transform 0.2s;
}

.btn-action:hover {
  transform: scale(1.2);
}

.btn-save,
.btn-cancel,
.btn-confirm {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-save,
.btn-confirm {
  background: #409eff;
  color: white;
}

.btn-save:hover,
.btn-confirm:hover {
  background: #66b1ff;
}

.btn-cancel {
  background: #f56c6c;
  color: white;
}

.btn-cancel:hover {
  background: #f78989;
}

.error-message {
  margin-top: 20px;
  padding: 12px;
  background: #fef0f0;
  color: #f56c6c;
  border-radius: 4px;
  border-left: 4px solid #f56c6c;
}

.success-message {
  margin-top: 20px;
  padding: 12px;
  background: #f0f9ff;
  color: #409eff;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

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
  z-index: 1000;
}

.dialog {
  background: white;
  padding: 24px;
  border-radius: 8px;
  min-width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dialog h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #303133;
}

.dialog p {
  margin-bottom: 24px;
  color: #606266;
}

.dialog-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>
