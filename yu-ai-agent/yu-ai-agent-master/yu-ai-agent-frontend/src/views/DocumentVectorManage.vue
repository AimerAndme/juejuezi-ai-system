<template>
  <div class="vector-manage">
    <h2>📊 文档向量管理</h2>

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
        v-if="selectedVectors.length > 0"
        @click="openBatchDeleteDialog"
        class="btn-batch-delete"
      >
        🗑️ 批量删除 ({{ selectedVectors.length }})
      </button>
      <button
        v-if="filterFileMd5.trim()"
        @click="openDeleteFileVectorsDialog"
        class="btn-delete-file"
      >
        🗑️ 删除该文件所有向量
      </button>
      <button @click="goBack" class="btn-back">⬅ 返回</button>
    </div>

    <!-- 列表 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="filteredVectorList.length === 0" class="empty">
      <div class="empty-icon">📭</div>
      <p>暂无向量数据</p>
    </div>

    <div v-else class="vector-list">
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
            共 <strong>{{ filteredVectorList.length }}</strong> 条向量记录
            <span v-if="selectedVectors.length > 0">
              （已选 <strong>{{ selectedVectors.length }}</strong> 条）
            </span>
          </span>
        </div>
        <button
          v-if="selectedVectors.length > 0"
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
            <th>向量ID</th>
            <th>文件MD5</th>
            <th>分片序号</th>
            <th>文本内容</th>
            <th>模型版本</th>
            <th>公开</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="vector in filteredVectorList" :key="vector.vectorId">
            <td class="checkbox-cell">
              <input
                type="checkbox"
                :checked="isSelected(vector.vectorId)"
                @change="toggleSelect(vector.vectorId)"
              />
            </td>
            <td>{{ vector.vectorId }}</td>
            <td class="md5-cell">
              <span class="md5-text" :title="vector.fileMd5">
                {{ formatMd5(vector.fileMd5) }}
              </span>
            </td>
            <td>{{ vector.chunkId }}</td>
            <td class="text-cell">
              <div class="text-content">
                <span :title="vector.textContent">
                  {{ formatText(vector.textContent) }}
                </span>
                <button
                  v-if="vector.textContent && vector.textContent.length > 60"
                  @click="showFullText(vector)"
                  class="btn-view-full"
                  title="查看全文"
                >
                  👁️
                </button>
              </div>
            </td>
            <td>{{ vector.modelVersion }}</td>
            <td>
              <span
                :class="[
                  'public-badge',
                  vector.isPublic ? 'public' : 'private',
                ]"
              >
                {{ vector.isPublic ? '公开' : '私有' }}
              </span>
            </td>
            <td class="actions">
              <button
                class="btn-icon btn-delete"
                @click="openDeleteDialog(vector)"
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
            <p>确定要删除向量吗？</p>
            <p class="vector-info">向量 ID: {{ vectorToDelete?.vectorId }}</p>
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
            <p>确定要删除选中的向量吗？</p>
            <p class="vector-info">
              已选中 {{ selectedVectors.length }} 条向量
            </p>
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

    <!-- 删除文件所有向量确认对话框 -->
    <transition name="dialog">
      <div
        v-if="showDeleteFileDialog"
        class="dialog-overlay"
        @click="closeDeleteFileDialog"
      >
        <div class="dialog" @click.stop>
          <div class="dialog-header">
            <h3>⚠️ 确认删除文件所有向量</h3>
          </div>
          <div class="dialog-body">
            <p>确定要删除该文件的所有向量吗？</p>
            <p class="vector-info">文件MD5: {{ filterFileMd5 }}</p>
            <p class="vector-info">
              向量数量: {{ filteredVectorList.length }} 条
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
              @click="confirmDeleteFileVectors"
              class="btn-dialog btn-confirm"
            >
              确定删除全部
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 全文查看对话框 -->
    <transition name="dialog">
      <div
        v-if="showTextDialog"
        class="dialog-overlay"
        @click="closeTextDialog"
      >
        <div class="dialog text-dialog" @click.stop>
          <div class="dialog-header">
            <h3>📄 文本内容详情</h3>
          </div>
          <div class="dialog-body">
            <div class="text-detail-info">
              <div class="info-row">
                <span class="label">向量ID:</span>
                <span class="value">{{ currentVector?.vectorId }}</span>
              </div>
              <div class="info-row">
                <span class="label">文件MD5:</span>
                <span class="value md5-value">{{
                  currentVector?.fileMd5
                }}</span>
              </div>
              <div class="info-row">
                <span class="label">分片序号:</span>
                <span class="value">{{ currentVector?.chunkId }}</span>
              </div>
            </div>
            <div class="text-content-full">
              <div class="content-label">完整内容:</div>
              <pre class="content-text">{{ currentVector?.textContent }}</pre>
            </div>
          </div>
          <div class="dialog-actions">
            <button @click="closeTextDialog" class="btn-dialog btn-close">
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
  getAllVectorsByUser,
  deleteVector,
  deleteVectorsByFile,
} from '../api/documentVectorApi'

const route = useRoute()
const router = useRouter()

const vectorList = ref([])
const filterFileMd5 = ref('')
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const showDeleteDialog = ref(false)
const vectorToDelete = ref(null)
const showTextDialog = ref(false)
const currentVector = ref(null)

// 批量操作相关
const selectedVectors = ref([])
const showBatchDeleteDialog = ref(false)
const showDeleteFileDialog = ref(false)

const getUserId = () => {
  return localStorage.getItem('userId') || 'default-user'
}

// 根据筛选条件过滤列表
const filteredVectorList = computed(() => {
  let list = vectorList.value
  if (filterFileMd5.value.trim()) {
    const keyword = filterFileMd5.value.trim().toLowerCase()
    list = list.filter(
      (v) => v.fileMd5 && v.fileMd5.toLowerCase().includes(keyword)
    )
  }
  // 按分片序号从小到大排序
  return list.sort((a, b) => (a.chunkId || 0) - (b.chunkId || 0))
})

// 是否全选
const isAllSelected = computed(() => {
  return (
    filteredVectorList.value.length > 0 &&
    selectedVectors.value.length === filteredVectorList.value.length
  )
})

// 切换全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedVectors.value = []
  } else {
    selectedVectors.value = filteredVectorList.value.map((v) => v.vectorId)
  }
}

// 检查是否选中
const isSelected = (vectorId) => {
  return selectedVectors.value.includes(vectorId)
}

// 切换选中状态
const toggleSelect = (vectorId) => {
  const index = selectedVectors.value.indexOf(vectorId)
  if (index > -1) {
    selectedVectors.value.splice(index, 1)
  } else {
    selectedVectors.value.push(vectorId)
  }
}

// 清空选择
const clearSelection = () => {
  selectedVectors.value = []
}

// 加载用户所有向量
const loadVectors = async () => {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const res = await getAllVectorsByUser(getUserId())
    if (res.data.code === 200) {
      vectorList.value = res.data.data || []
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
  selectedVectors.value = []
  loadVectors()
}

// 应用筛选
const applyFilter = () => {
  // 筛选由 computed 自动完成
}

// 返回上一页
const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/file-center')
  }
}

// 文本截断展示
const formatText = (text) => {
  if (!text) return ''
  const t = String(text)
  return t.length > 60 ? t.slice(0, 60) + '...' : t
}

// MD5 截断展示
const formatMd5 = (md5) => {
  if (!md5) return ''
  return md5.length > 12 ? md5.slice(0, 12) + '...' : md5
}

// 删除弹窗
const openDeleteDialog = (vector) => {
  vectorToDelete.value = vector
  showDeleteDialog.value = true
}

const closeDeleteDialog = () => {
  showDeleteDialog.value = false
  vectorToDelete.value = null
}

// 显示全文对话框
const showFullText = (vector) => {
  currentVector.value = vector
  showTextDialog.value = true
}

const closeTextDialog = () => {
  showTextDialog.value = false
  currentVector.value = null
}

// 确认删除
const confirmDelete = async () => {
  if (!vectorToDelete.value) return
  try {
    const res = await deleteVector(vectorToDelete.value.vectorId, getUserId())
    if (res.data.code === 200) {
      successMessage.value = '删除成功'
      setTimeout(() => (successMessage.value = ''), 3000)
      // 从选中列表中移除
      const index = selectedVectors.value.indexOf(vectorToDelete.value.vectorId)
      if (index > -1) {
        selectedVectors.value.splice(index, 1)
      }
      await loadVectors()
    } else {
      errorMessage.value = res.data.message || '删除失败'
      setTimeout(() => (errorMessage.value = ''), 3000)
    }
  } catch (e) {
    errorMessage.value = '删除失败: ' + e.message
    setTimeout(() => (errorMessage.value = ''), 3000)
  } finally {
    closeDeleteDialog()
  }
}

// 打开批量删除对话框
const openBatchDeleteDialog = () => {
  if (selectedVectors.value.length === 0) {
    errorMessage.value = '请先选择要删除的向量'
    setTimeout(() => (errorMessage.value = ''), 3000)
    return
  }
  showBatchDeleteDialog.value = true
}

const closeBatchDeleteDialog = () => {
  showBatchDeleteDialog.value = false
}

// 批量删除
const confirmBatchDelete = async () => {
  if (selectedVectors.value.length === 0) return

  loading.value = true
  const userId = getUserId()
  let successCount = 0
  let failCount = 0

  try {
    // 并发删除，但限制并发数
    const batchSize = 5
    for (let i = 0; i < selectedVectors.value.length; i += batchSize) {
      const batch = selectedVectors.value.slice(i, i + batchSize)
      const promises = batch.map((vectorId) =>
        deleteVector(vectorId, userId)
          .then((res) => {
            if (res.data.code === 200) {
              successCount++
            } else {
              failCount++
            }
          })
          .catch(() => {
            failCount++
          })
      )
      await Promise.all(promises)
    }

    if (successCount > 0) {
      successMessage.value = `批量删除完成：成功 ${successCount} 条${
        failCount > 0 ? `，失败 ${failCount} 条` : ''
      }`
      setTimeout(() => (successMessage.value = ''), 3000)
    }

    if (failCount > 0 && successCount === 0) {
      errorMessage.value = `批量删除失败：${failCount} 条`
      setTimeout(() => (errorMessage.value = ''), 3000)
    }

    selectedVectors.value = []
    await loadVectors()
  } catch (e) {
    errorMessage.value = '批量删除失败: ' + e.message
    setTimeout(() => (errorMessage.value = ''), 3000)
  } finally {
    loading.value = false
    closeBatchDeleteDialog()
  }
}

// 打开删除文件所有向量对话框
const openDeleteFileVectorsDialog = () => {
  if (!filterFileMd5.value.trim()) {
    errorMessage.value = '请先输入文件MD5'
    setTimeout(() => (errorMessage.value = ''), 3000)
    return
  }
  showDeleteFileDialog.value = true
}

const closeDeleteFileDialog = () => {
  showDeleteFileDialog.value = false
}

// 确认删除文件所有向量
const confirmDeleteFileVectors = async () => {
  if (!filterFileMd5.value.trim()) return

  loading.value = true
  try {
    const res = await deleteVectorsByFile(
      filterFileMd5.value.trim(),
      getUserId()
    )
    if (res.data.code === 200) {
      successMessage.value = '删除文件所有向量成功'
      setTimeout(() => (successMessage.value = ''), 3000)
      selectedVectors.value = []
      await loadVectors()
    } else {
      errorMessage.value = res.data.message || '删除失败'
      setTimeout(() => (errorMessage.value = ''), 3000)
    }
  } catch (e) {
    errorMessage.value = '删除失败: ' + e.message
    setTimeout(() => (errorMessage.value = ''), 3000)
  } finally {
    loading.value = false
    closeDeleteFileDialog()
  }
}

// 组件挂载时加载，支持从路由参数传入 fileMd5
onMounted(() => {
  if (route.query.fileMd5) {
    filterFileMd5.value = route.query.fileMd5
  }
  loadVectors()
})
</script>

<style scoped>
.vector-manage {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
}

h2 {
  margin-bottom: 25px;
  color: #333;
  font-size: 28px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-weight: 600;
  color: #606266;
  white-space: nowrap;
}

.filter-item input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-width: 280px;
  font-size: 14px;
}

.btn-refresh {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-refresh:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.btn-back {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #ffffff;
  color: #606266;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-back:hover {
  background: #f2f3f5;
  color: #409eff;
}

.btn-batch-delete {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  background: #f56c6c;
  color: #fff;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-batch-delete:hover {
  background: #f78989;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(245, 108, 108, 0.3);
}

.btn-delete-file {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  background: #e6a23c;
  color: #fff;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-delete-file:hover {
  background: #ebb563;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(230, 162, 60, 0.3);
}

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

.vector-list {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.list-info {
  padding: 15px 20px;
  background: #f5f7fa;
  color: #606266;
  border-bottom: 2px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-weight: 600;
  color: #606266;
}

.select-all input[type='checkbox'] {
  cursor: pointer;
  width: 16px;
  height: 16px;
}

.count-text strong {
  color: #667eea;
  font-size: 18px;
}

.btn-clear {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  background: #909399;
  color: white;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-clear:hover {
  background: #a6a9ad;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 12px 15px;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
  text-align: left;
}

th {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

tbody tr:hover {
  background: #f8f9ff;
}

.checkbox-cell {
  text-align: center;
}

.checkbox-cell input[type='checkbox'] {
  cursor: pointer;
  width: 16px;
  height: 16px;
}

.md5-cell {
  max-width: 150px;
}

.md5-text {
  font-family: monospace;
  font-size: 13px;
  color: #606266;
}

.text-cell {
  max-width: 400px;
}

.text-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.text-cell span {
  flex: 1;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.btn-view-full {
  padding: 4px 8px;
  border: none;
  background: #e8f4ff;
  color: #409eff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-view-full:hover {
  background: #409eff;
  color: white;
  transform: scale(1.1);
}

.public-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.public-badge.public {
  background: #e1f3d8;
  color: #67c23a;
}

.public-badge.private {
  background: #fef0f0;
  color: #f56c6c;
}

.actions {
  text-align: center;
}

.btn-icon {
  padding: 4px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 18px;
  transition: transform 0.2s;
}

.btn-icon:hover {
  transform: scale(1.3);
}

.message {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 2000;
  display: flex;
  align-items: center;
  gap: 8px;
}

.error-message {
  background: #fef0f0;
  color: #f56c6c;
  border-left: 4px solid #f56c6c;
}

.success-message {
  background: #f0f9ff;
  color: #409eff;
  border-left: 4px solid #409eff;
}

.message-enter-active,
.message-leave-active {
  transition: all 0.3s ease;
}

.message-enter-from,
.message-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}

.dialog {
  background: #fff;
  border-radius: 12px;
  min-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.dialog-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
}

.dialog-body {
  padding: 24px;
}

.dialog-body p {
  margin: 10px 0;
  color: #606266;
}

.vector-info {
  font-weight: 600;
  color: #409eff;
}

.warning-text {
  font-size: 13px;
  color: #f56c6c;
}

.warning-text.danger {
  font-weight: 600;
  background: #fef0f0;
  padding: 8px 12px;
  border-radius: 4px;
  margin-top: 15px;
}

.dialog-actions {
  padding: 15px 24px;
  background: #f5f7fa;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-dialog {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-confirm {
  background: #f56c6c;
  color: white;
}

.btn-confirm:hover {
  background: #f78989;
}

.btn-cancel {
  background: #909399;
  color: white;
}

.btn-cancel:hover {
  background: #a6a9ad;
}

.dialog-enter-active,
.dialog-leave-active {
  transition: all 0.3s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}

.dialog-enter-from .dialog,
.dialog-leave-to .dialog {
  transform: scale(0.9);
}

/* 全文查看对话框 */
.text-dialog {
  min-width: 700px;
  max-width: 90vw;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.text-dialog .dialog-body {
  max-height: 65vh;
  overflow-y: auto;
}

.text-detail-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  font-weight: 600;
  color: #606266;
  width: 100px;
  flex-shrink: 0;
}

.info-row .value {
  color: #303133;
  word-break: break-all;
}

.md5-value {
  font-family: monospace;
  font-size: 13px;
  background: white;
  padding: 4px 8px;
  border-radius: 4px;
}

.text-content-full {
  margin-top: 15px;
}

.content-label {
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
  font-size: 14px;
}

.content-text {
  background: #f9fafb;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, sans-serif;
  margin: 0;
  max-height: 400px;
  overflow-y: auto;
}

.btn-close {
  background: #409eff;
  color: white;
}

.btn-close:hover {
  background: #66b1ff;
}

/* 响应式 */
@media (max-width: 768px) {
  .text-dialog {
    min-width: 90vw;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .info-row .label {
    width: auto;
  }
}
</style>
