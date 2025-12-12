<template>
  <div class="file-uploader">
    <h2>文件上传</h2>

    <!-- 文件选择区域 -->
    <div class="upload-section">
      <div class="file-input-wrapper">
        <input
          type="file"
          ref="fileInput"
          @change="handleFileSelect"
          accept=".docx,.md,.pdf"
          :disabled="isUploading"
        />
        <button
          @click="triggerFileInput"
          :disabled="isUploading"
          class="btn-select"
        >
          选择文件
        </button>
      </div>

      <!-- 文件信息显示 -->
      <div v-if="selectedFile" class="file-info">
        <div class="info-row">
          <span class="label">文件名:</span>
          <span class="value">{{ selectedFile.name }}</span>
        </div>
        <div class="info-row">
          <span class="label">文件大小:</span>
          <span class="value">{{ formatFileSize(selectedFile.size) }}</span>
        </div>
        <div class="info-row">
          <span class="label">文件类型:</span>
          <span class="value">{{ selectedFile.type || '未知' }}</span>
        </div>
      </div>

      <!-- 进度条 -->
      <div v-if="selectedFile" class="progress-section">
        <div class="progress-info">
          <span>{{ uploadStatus }}</span>
          <span>{{ uploadProgress }}%</span>
        </div>
        <div class="progress-bar">
          <div
            class="progress-fill"
            :style="{ width: uploadProgress + '%' }"
          ></div>
        </div>
        <div v-if="isUploading" class="upload-stats">
          <span>上传速度: {{ uploadSpeed }}</span>
          <span>剩余时间: {{ estimatedTime }}</span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button
          @click="startUpload"
          :disabled="!selectedFile || isUploading || uploadProgress === 100"
          class="btn-upload"
        >
          {{ isUploading ? '上传中...' : '开始上传' }}
        </button>
        <button
          @click="cancelUpload"
          :disabled="!isUploading"
          class="btn-cancel"
        >
          取消
        </button>
        <button
          @click="resumeUpload"
          v-if="canResume"
          :disabled="isUploading"
          class="btn-resume"
        >
          断点续传
        </button>
      </div>

      <!-- 错误信息 -->
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>

      <!-- 成功信息 -->
      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  initiateUpload,
  uploadChunk,
  getUploadStatus,
  completeUpload,
} from '../api/fileUploadApi'
import { calculateFileMD5, calculateChunkMD5 } from '../utils/md5'

// 常量配置
const CHUNK_SIZE = 2 * 1024 * 1024 // 2MB
const MAX_RETRY = 3 // 最大重试次数

// 响应式数据
const fileInput = ref(null)
const selectedFile = ref(null)
const fileMd5 = ref('')
const uploadProgress = ref(0)
const uploadStatus = ref('等待上传')
const isUploading = ref(false)
const canResume = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// 上传统计
const uploadedChunks = ref([])
const totalChunks = ref(0)
const uploadStartTime = ref(0)
const uploadedBytes = ref(0)
const uploadSpeed = ref('0 KB/s')
const estimatedTime = ref('--:--')

// 取消上传标志
let cancelFlag = false

// 获取当前用户ID（从localStorage或其他地方获取）
const getUserId = () => {
  return localStorage.getItem('userId') || 'default-user'
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileSelect = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  const allowedTypes = ['.docx', '.md', '.pdf']
  const fileExtension = '.' + file.name.split('.').pop().toLowerCase()

  if (!allowedTypes.includes(fileExtension)) {
    errorMessage.value = '不支持的文件类型，仅支持 .docx, .md, .pdf'
    return
  }

  selectedFile.value = file
  errorMessage.value = ''
  successMessage.value = ''
  uploadProgress.value = 0
  uploadStatus.value = '正在计算文件MD5...'

  try {
    // 计算文件MD5
    fileMd5.value = await calculateFileMD5(file, (progress) => {
      uploadStatus.value = `正在计算文件MD5... ${progress}%`
    })

    uploadStatus.value = '就绪，可以开始上传'

    // 检查是否可以断点续传
    await checkResumeStatus()
  } catch (error) {
    errorMessage.value = '文件MD5计算失败: ' + error.message
    uploadStatus.value = '计算失败'
  }
}

// 检查断点续传状态
const checkResumeStatus = async () => {
  try {
    const response = await getUploadStatus(fileMd5.value, getUserId())
    if (response.data.code === 200 && response.data.data) {
      const data = response.data.data
      if (
        data.status === 0 &&
        data.uploadedChunks &&
        data.uploadedChunks.length > 0
      ) {
        uploadedChunks.value = data.uploadedChunks
        totalChunks.value = data.totalChunks
        canResume.value = true
        uploadStatus.value = `检测到未完成的上传 (${data.uploadedChunks.length}/${data.totalChunks} 分片)`
      }
    }
  } catch (error) {
    console.warn('检查断点续传状态失败:', error)
  }
}

// 开始上传
const startUpload = async () => {
  if (!selectedFile.value || !fileMd5.value) return

  isUploading.value = true
  cancelFlag = false
  errorMessage.value = ''
  successMessage.value = ''
  uploadStartTime.value = Date.now()
  uploadedBytes.value = 0

  try {
    // 1. 初始化上传
    uploadStatus.value = '初始化上传...'
    const initResponse = await initiateUpload({
      fileName: selectedFile.value.name,
      totalSize: selectedFile.value.size,
      fileMd5: fileMd5.value,
      userId: getUserId(),
      isPublic: false,
    })

    if (initResponse.data.code !== 200) {
      throw new Error(initResponse.data.message || '初始化失败')
    }

    const initData = initResponse.data.data

    // 检查是否需要上传（秒传）
    if (!initData.needUpload) {
      uploadProgress.value = 100
      uploadStatus.value = '上传完成'
      successMessage.value = initData.message || '文件秒传成功！'
      isUploading.value = false
      return
    }

    // 2. 上传分片
    totalChunks.value = initData.totalChunks
    uploadedChunks.value = initData.uploadedChunks || []

    await uploadChunks()

    // 3. 完成上传
    if (!cancelFlag) {
      await finishUpload()
    }
  } catch (error) {
    errorMessage.value = '上传失败: ' + error.message
    uploadStatus.value = '上传失败'
    isUploading.value = false
  }
}

// 上传分片
const uploadChunks = async () => {
  const file = selectedFile.value
  const totalSize = file.size

  for (let i = 0; i < totalChunks.value; i++) {
    if (cancelFlag) {
      uploadStatus.value = '上传已取消'
      return
    }

    // 跳过已上传的分片
    if (uploadedChunks.value.includes(i)) {
      uploadedBytes.value += Math.min(CHUNK_SIZE, totalSize - i * CHUNK_SIZE)
      updateProgress()
      continue
    }

    const start = i * CHUNK_SIZE
    const end = Math.min(start + CHUNK_SIZE, totalSize)
    const chunk = file.slice(start, end)

    // 计算分片MD5
    const chunkMd5 = await calculateChunkMD5(chunk)

    // 上传分片（带重试）
    let retryCount = 0
    let uploaded = false

    while (retryCount < MAX_RETRY && !uploaded && !cancelFlag) {
      try {
        const formData = new FormData()
        formData.append('file', chunk)
        formData.append('fileMd5', fileMd5.value)
        formData.append('chunkIndex', i)
        formData.append('chunkMd5', chunkMd5)
        formData.append('userId', getUserId())

        const response = await uploadChunk(formData, (progressEvent) => {
          // 这里可以处理单个分片的上传进度
        })

        if (response.data.code === 200) {
          uploaded = true
          uploadedChunks.value.push(i)
          uploadedBytes.value += end - start
          updateProgress()
        } else {
          throw new Error(response.data.message || '上传失败')
        }
      } catch (error) {
        retryCount++
        if (retryCount >= MAX_RETRY) {
          throw new Error(`分片 ${i} 上传失败: ${error.message}`)
        }
        await sleep(1000 * retryCount) // 重试延迟
      }
    }

    if (!uploaded && !cancelFlag) {
      throw new Error(`分片 ${i} 上传失败`)
    }
  }
}

// 完成上传
const finishUpload = async () => {
  uploadStatus.value = '合并文件中...'

  const response = await completeUpload({
    fileMd5: fileMd5.value,
    userId: getUserId(),
  })

  if (response.data.code === 200) {
    uploadProgress.value = 100
    uploadStatus.value = '上传完成'
    successMessage.value = '文件上传成功！'
    canResume.value = false
  } else {
    throw new Error(response.data.message || '合并失败')
  }

  isUploading.value = false
}

// 更新进度
const updateProgress = () => {
  const progress = Math.floor(
    (uploadedBytes.value / selectedFile.value.size) * 100
  )
  uploadProgress.value = progress
  uploadStatus.value = `上传中... (${uploadedChunks.value.length}/${totalChunks.value} 分片)`

  // 计算上传速度和剩余时间
  const elapsed = (Date.now() - uploadStartTime.value) / 1000 // 秒
  if (elapsed > 0) {
    const speed = uploadedBytes.value / elapsed // 字节/秒
    uploadSpeed.value = formatSpeed(speed)

    const remaining = selectedFile.value.size - uploadedBytes.value
    const remainingTime = remaining / speed // 秒
    estimatedTime.value = formatTime(remainingTime)
  }
}

// 取消上传
const cancelUpload = () => {
  cancelFlag = true
  isUploading.value = false
  uploadStatus.value = '上传已取消'
  canResume.value = true
}

// 断点续传
const resumeUpload = async () => {
  await startUpload()
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

// 格式化速度
const formatSpeed = (bytesPerSecond) => {
  return formatFileSize(bytesPerSecond) + '/s'
}

// 格式化时间
const formatTime = (seconds) => {
  if (!isFinite(seconds)) return '--:--'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 延迟函数
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

// 组件挂载时检查是否有未完成的上传
onMounted(() => {
  // 可以从localStorage恢复未完成的上传任务
})
</script>

<style scoped>
.file-uploader {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h2 {
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
}

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.file-input-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-input-wrapper input[type='file'] {
  display: none;
}

.btn-select {
  padding: 10px 20px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-select:hover:not(:disabled) {
  background: #66b1ff;
}

.btn-select:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

.file-info {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  font-weight: bold;
  min-width: 80px;
  color: #606266;
}

.info-row .value {
  color: #303133;
}

.progress-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.progress-bar {
  height: 20px;
  background: #e4e7ed;
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #66b1ff);
  transition: width 0.3s;
}

.upload-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.action-buttons button {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-upload {
  background: #67c23a;
  color: white;
}

.btn-upload:hover:not(:disabled) {
  background: #85ce61;
}

.btn-upload:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

.btn-cancel {
  background: #f56c6c;
  color: white;
}

.btn-cancel:hover:not(:disabled) {
  background: #f78989;
}

.btn-cancel:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

.btn-resume {
  background: #e6a23c;
  color: white;
}

.btn-resume:hover:not(:disabled) {
  background: #ebb563;
}

.btn-resume:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

.error-message {
  padding: 12px;
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  font-size: 14px;
}

.success-message {
  padding: 12px;
  background: #f0f9ff;
  color: #67c23a;
  border: 1px solid #c6e2ff;
  border-radius: 4px;
  font-size: 14px;
}
</style>
