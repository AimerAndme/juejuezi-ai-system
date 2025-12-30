<template>
  <div class="es-doc-manage-container">
    <div class="header">
      <h1>📦 ES索引文档管理</h1>
      <button @click="goBack" class="btn-back">← 返回文件中心</button>
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
      <button v-if="currentFileMd5" @click="handleRefresh" class="btn-refresh">
        🔄 刷新
      </button>
    </div>

    <div v-if="currentFileMd5" class="stats-bar">
      <span
        >文件MD5: <strong>{{ currentFileMd5 }}</strong></span
      >
      <span
        >总文档数: <strong>{{ totalCount }}</strong></span
      >
      <span
        >当前加载: <strong>{{ esDocList.length }}</strong></span
      >
      <button
        @click="handleDeleteAll"
        class="btn-delete-all"
        :disabled="esDocList.length === 0"
      >
        🗑️ 删除该文件所有文档
      </button>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="esDocList.length === 0 && currentFileMd5" class="empty">
      <p>📭 暂无ES文档数据</p>
    </div>

    <div v-else-if="esDocList.length > 0" class="table-container">
      <table class="doc-table">
        <thead>
          <tr>
            <th>文档ID</th>
            <th>分片序号</th>
            <th>文本内容</th>
            <th>向量维度</th>
            <th>模型版本</th>
            <th>用户ID</th>
            <th>组织标签</th>
            <th>是否公开</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="doc in esDocList" :key="doc.id">
            <td class="doc-id" :title="doc.id">{{ formatDocId(doc.id) }}</td>
            <td>{{ doc.source?.chunkId || '-' }}</td>
            <td class="text-cell">
              <div class="text-content">
                <span :title="doc.source?.textContent">
                  {{ formatText(doc.source?.textContent) }}
                </span>
                <button
                  v-if="
                    doc.source?.textContent &&
                    doc.source.textContent.length > 60
                  "
                  @click="showFullText(doc)"
                  class="btn-view-full"
                  title="查看全文"
                >
                  👁️
                </button>
              </div>
            </td>
            <td>{{ getVectorDims(doc.source?.vector) }}</td>
            <td>{{ doc.source?.modelVersion || '-' }}</td>
            <td :title="doc.source?.userId">
              {{ formatUserId(doc.source?.userId) }}
            </td>
            <td>{{ doc.source?.orgTag || '-' }}</td>
            <td>
              <span
                :class="doc.source?.isPublic ? 'badge-public' : 'badge-private'"
              >
                {{ doc.source?.isPublic ? '公开' : '私有' }}
              </span>
            </td>
            <td class="actions">
              <button
                @click="handleViewVector(doc)"
                class="btn-icon btn-vector"
                title="查看向量"
              >
                🔢
              </button>
              <button
                @click="handleDelete(doc)"
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
    <div v-if="esDocList.length > 0 && hasMore" class="pagination">
      <button @click="loadMore" class="btn-load-more" :disabled="loading">
        {{ loading ? '加载中...' : '加载更多' }}
      </button>
    </div>

    <!-- 全文对话框 -->
    <div v-if="showTextDialog" class="dialog-overlay" @click="closeTextDialog">
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>📄 文本内容详情</h3>
          <button @click="closeTextDialog" class="btn-close">✕</button>
        </div>
        <div class="dialog-body">
          <div class="info-row">
            <span class="label">文档ID:</span>
            <span class="value">{{ selectedDoc?.id }}</span>
          </div>
          <div class="info-row">
            <span class="label">文件MD5:</span>
            <span class="value">{{ selectedDoc?.source?.fileMd5 }}</span>
          </div>
          <div class="info-row">
            <span class="label">分片序号:</span>
            <span class="value">{{ selectedDoc?.source?.chunkId }}</span>
          </div>
          <div class="text-full">
            <pre>{{ selectedDoc?.source?.textContent }}</pre>
          </div>
        </div>
      </div>
    </div>

    <!-- 向量对话框 -->
    <div
      v-if="showVectorDialog"
      class="dialog-overlay"
      @click="closeVectorDialog"
    >
      <div class="dialog-content" @click.stop>
        <div class="dialog-header">
          <h3>🔢 向量数据详情</h3>
          <button @click="closeVectorDialog" class="btn-close">✕</button>
        </div>
        <div class="dialog-body">
          <div class="info-row">
            <span class="label">文档ID:</span>
            <span class="value">{{ selectedDoc?.id }}</span>
          </div>
          <div class="info-row">
            <span class="label">向量维度:</span>
            <span class="value">{{
              getVectorDims(selectedDoc?.source?.vector)
            }}</span>
          </div>
          <div class="vector-data">
            <pre>{{
              JSON.stringify(selectedDoc?.source?.vector, null, 2)
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
          <p>确定要删除此ES文档吗？</p>
          <p class="warning-text">文档ID: {{ docToDelete?.id }}</p>
          <div class="dialog-actions">
            <button @click="confirmDelete" class="btn-confirm">确认删除</button>
            <button @click="closeDeleteDialog" class="btn-cancel">取消</button>
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
          <p>确定要删除该文件的所有ES文档吗？</p>
          <p class="warning-text">文件MD5: {{ currentFileMd5 }}</p>
          <p class="warning-text">文档总数: {{ totalCount }}</p>
          <p class="danger-text">⚠️ 此操作不可恢复！</p>
          <div class="dialog-actions">
            <button @click="confirmDeleteAll" class="btn-confirm btn-danger">
              确认删除全部
            </button>
            <button @click="closeDeleteAllDialog" class="btn-cancel">
              取消
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  queryEsByFileMd5,
  countEsByFileMd5,
  deleteEsByFileMd5,
  deleteEsDocById,
} from '../api/esIndexApi'

const router = useRouter()

// 数据
const searchFileMd5 = ref('')
const currentFileMd5 = ref('')
const esDocList = ref([])
const totalCount = ref(0)
const currentPage = ref(0)
const pageSize = ref(50)
const hasMore = ref(true)
const loading = ref(false)

// 对话框
const showTextDialog = ref(false)
const showVectorDialog = ref(false)
const showDeleteDialog = ref(false)
const showDeleteAllDialog = ref(false)
const selectedDoc = ref(null)
const docToDelete = ref(null)

// 提示消息
const message = ref('')
const messageType = ref('info')

// 返回文件中心
const goBack = () => {
  router.push('/file-center')
}

// 搜索
const handleSearch = async () => {
  if (!searchFileMd5.value.trim()) {
    showMessage('请输入文件MD5', 'error')
    return
  }
  currentFileMd5.value = searchFileMd5.value.trim()
  esDocList.value = []
  currentPage.value = 0
  hasMore.value = true
  await loadData()
  await loadCount()
}

// 刷新
const handleRefresh = async () => {
  esDocList.value = []
  currentPage.value = 0
  hasMore.value = true
  await loadData()
  await loadCount()
}

// 加载数据
const loadData = async () => {
  if (!currentFileMd5.value || loading.value) return

  loading.value = true
  try {
    const from = currentPage.value * pageSize.value
    const response = await queryEsByFileMd5(
      currentFileMd5.value,
      from,
      pageSize.value
    )

    if (response.data.code === 200) {
      const newDocs = response.data.data || []
      esDocList.value.push(...newDocs)

      if (newDocs.length < pageSize.value) {
        hasMore.value = false
      } else {
        currentPage.value++
      }
    } else {
      showMessage(response.data.message || '查询失败', 'error')
    }
  } catch (error) {
    console.error('查询ES文档失败:', error)
    showMessage('查询失败: ' + error.message, 'error')
  } finally {
    loading.value = false
  }
}

// 加载总数
const loadCount = async () => {
  if (!currentFileMd5.value) return

  try {
    const response = await countEsByFileMd5(currentFileMd5.value)
    if (response.data.code === 200) {
      totalCount.value = response.data.count || 0
    }
  } catch (error) {
    console.error('统计ES文档失败:', error)
  }
}

// 加载更多
const loadMore = () => {
  loadData()
}

// 显示全文
const showFullText = (doc) => {
  selectedDoc.value = doc
  showTextDialog.value = true
}

const closeTextDialog = () => {
  showTextDialog.value = false
  selectedDoc.value = null
}

// 查看向量
const handleViewVector = (doc) => {
  selectedDoc.value = doc
  showVectorDialog.value = true
}

const closeVectorDialog = () => {
  showVectorDialog.value = false
  selectedDoc.value = null
}

// 删除单个文档
const handleDelete = (doc) => {
  docToDelete.value = doc
  showDeleteDialog.value = true
}

const closeDeleteDialog = () => {
  showDeleteDialog.value = false
  docToDelete.value = null
}

const confirmDelete = async () => {
  if (!docToDelete.value) return

  try {
    const response = await deleteEsDocById(docToDelete.value.id)
    if (response.data.code === 200) {
      showMessage('删除成功', 'success')
      // 从列表中移除
      esDocList.value = esDocList.value.filter(
        (d) => d.id !== docToDelete.value.id
      )
      totalCount.value--
      closeDeleteDialog()
    } else {
      showMessage(response.data.message || '删除失败', 'error')
    }
  } catch (error) {
    console.error('删除ES文档失败:', error)
    showMessage('删除失败: ' + error.message, 'error')
  }
}

// 删除全部文档
const handleDeleteAll = () => {
  showDeleteAllDialog.value = true
}

const closeDeleteAllDialog = () => {
  showDeleteAllDialog.value = false
}

const confirmDeleteAll = async () => {
  if (!currentFileMd5.value) return

  try {
    const response = await deleteEsByFileMd5(currentFileMd5.value)
    if (response.data.code === 200) {
      showMessage('批量删除成功', 'success')
      esDocList.value = []
      totalCount.value = 0
      hasMore.value = false
      closeDeleteAllDialog()
    } else {
      showMessage(response.data.message || '删除失败', 'error')
    }
  } catch (error) {
    console.error('批量删除ES文档失败:', error)
    showMessage('删除失败: ' + error.message, 'error')
  }
}

// 工具函数
const formatDocId = (id) => {
  if (!id) return '-'
  return id.length > 20 ? id.substring(0, 20) + '...' : id
}

const formatUserId = (userId) => {
  if (!userId) return '-'
  return userId.length > 15 ? userId.substring(0, 15) + '...' : userId
}

const formatText = (text) => {
  if (!text) return '-'
  return text.length > 60 ? text.substring(0, 60) + '...' : text
}

const getVectorDims = (vector) => {
  if (!vector || !Array.isArray(vector)) return '-'
  return vector.length + '维'
}

const showMessage = (msg, type = 'info') => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

onMounted(() => {
  // 可以从URL参数获取初始fileMd5
  const params = new URLSearchParams(window.location.search)
  const fileMd5 = params.get('fileMd5')
  if (fileMd5) {
    searchFileMd5.value = fileMd5
    handleSearch()
  }
})
</script>

<style scoped>
.es-doc-manage-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h1 {
  margin: 0;
  font-size: 28px;
  color: #333;
}

.btn-back {
  padding: 10px 20px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-back:hover {
  background: #5a6268;
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
}

.doc-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1200px;
}

.doc-table th,
.doc-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.doc-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
  font-size: 13px;
  white-space: nowrap;
}

.doc-table td {
  font-size: 13px;
  color: #666;
}

.doc-table tbody tr:hover {
  background: #f8f9fa;
}

.doc-id {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #007bff;
  cursor: pointer;
}

.text-cell {
  max-width: 400px;
}

.text-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.text-content span {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-view-full {
  padding: 4px 8px;
  background: #17a2b8;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  flex-shrink: 0;
}

.btn-view-full:hover {
  background: #138496;
}

.badge-public,
.badge-private {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.badge-public {
  background: #d4edda;
  color: #155724;
}

.badge-private {
  background: #f8d7da;
  color: #721c24;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  padding: 6px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-vector {
  background: #17a2b8;
  color: white;
}

.btn-vector:hover {
  background: #138496;
}

.btn-delete {
  background: #dc3545;
  color: white;
}

.btn-delete:hover {
  background: #c82333;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}

.btn-load-more {
  padding: 12px 40px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-load-more:hover:not(:disabled) {
  background: #0056b3;
}

.btn-load-more:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.dialog-content {
  background: white;
  border-radius: 8px;
  width: 700px;
  max-width: 90vw;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.dialog-small {
  width: 500px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
}

.btn-close:hover {
  color: #333;
}

.dialog-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-row .label {
  width: 100px;
  font-weight: 600;
  color: #666;
}

.info-row .value {
  flex: 1;
  color: #333;
  word-break: break-all;
}

.text-full pre,
.vector-data pre {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-size: 13px;
  line-height: 1.6;
  margin-top: 10px;
  max-height: 400px;
  overflow-y: auto;
}

.dialog-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  justify-content: center;
}

.btn-confirm,
.btn-cancel {
  padding: 10px 30px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn-confirm {
  background: #007bff;
  color: white;
}

.btn-confirm:hover {
  background: #0056b3;
}

.btn-danger {
  background: #dc3545;
}

.btn-danger:hover {
  background: #c82333;
}

.btn-cancel {
  background: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background: #5a6268;
}

.warning-text {
  color: #856404;
  background: #fff3cd;
  padding: 8px 12px;
  border-radius: 4px;
  margin: 10px 0;
  font-size: 13px;
}

.danger-text {
  color: #721c24;
  background: #f8d7da;
  padding: 8px 12px;
  border-radius: 4px;
  margin: 10px 0;
  font-size: 13px;
  font-weight: 600;
}

.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 6px;
  font-size: 14px;
  z-index: 2000;
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

.message.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.message.info {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}
</style>
