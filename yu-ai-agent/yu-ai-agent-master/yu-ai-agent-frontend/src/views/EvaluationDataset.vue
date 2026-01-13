<template>
  <div class="dataset-manager">
    <div class="header">
      <h2 class="main-title">数据集管理</h2>
      <p class="subtitle">创建和管理RAG评估数据集</p>
    </div>

    <div class="content-container">
      <div class="action-bar">
        <AppButton
          text="创建示例数据集"
          variant="primary"
          size="medium"
          @click="createSampleDataset"
        />
        <AppButton
          text="导入数据集"
          variant="secondary"
          size="medium"
          @click="importDataset"
        />
        <AppButton
          text="导出数据集"
          variant="secondary"
          size="medium"
          @click="exportDataset"
        />
        <AppButton
          text="清空数据集"
          variant="danger"
          size="medium"
          @click="clearDataset"
        />
        <AppButton
          text="运行评估"
          variant="success"
          size="medium"
          @click="showEvaluationModal = true"
          :disabled="testCases.length === 0"
        />
      </div>

      <div class="dataset-stats">
        <div class="stat-card">
          <div class="stat-icon">📊</div>
          <div class="stat-info">
            <div class="stat-label">测试用例数</div>
            <div class="stat-value">{{ testCases.length }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <div class="stat-label">已完成评估</div>
            <div class="stat-value">{{ evaluatedCount }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📝</div>
          <div class="stat-info">
            <div class="stat-label">待评估</div>
            <div class="stat-value">{{ pendingCount }}</div>
          </div>
        </div>
      </div>

      <div class="test-cases-section">
        <div class="section-header">
          <h3>测试用例列表</h3>
          <AppButton
            text="添加测试用例"
            variant="primary"
            size="small"
            @click="showAddModal = true"
          />
        </div>

        <div v-if="testCases.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <div class="empty-text">暂无测试用例</div>
          <div class="empty-actions">
            <AppButton
              text="创建示例数据集"
              variant="primary"
              size="small"
              @click="createSampleDataset"
            />
            <AppButton
              text="手动添加"
              variant="secondary"
              size="small"
              @click="showAddModal = true"
            />
          </div>
        </div>

        <div v-else class="test-cases-list">
          <div
            v-for="(testCase, index) in testCases"
            :key="testCase.id"
            class="test-case-item"
          >
            <div class="test-case-header">
              <div class="test-case-id">
                #{{ index + 1 }} - {{ testCase.id }}
              </div>
              <div class="test-case-actions">
                <AppButton
                  text="编辑"
                  variant="ghost"
                  size="small"
                  @click="editTestCase(testCase)"
                />
                <AppButton
                  text="删除"
                  variant="danger"
                  size="small"
                  @click="deleteTestCase(index)"
                />
              </div>
            </div>
            <div class="test-case-content">
              <div class="content-row">
                <div class="content-label">问题:</div>
                <div class="content-value">{{ testCase.question }}</div>
              </div>
              <div v-if="testCase.groundTruthAnswer" class="content-row">
                <div class="content-label">标准答案:</div>
                <div class="content-value">
                  {{ testCase.groundTruthAnswer }}
                </div>
              </div>
              <div
                v-if="
                  testCase.relevantDocuments &&
                  testCase.relevantDocuments.length > 0
                "
                class="content-row"
              >
                <div class="content-label">相关文档:</div>
                <div class="content-value tags">
                  <span
                    v-for="doc in testCase.relevantDocuments"
                    :key="doc"
                    class="tag"
                    >{{ doc }}</span
                  >
                </div>
              </div>
              <div v-if="testCase.retrievedAnswer" class="content-row">
                <div class="content-label">检索答案:</div>
                <div class="content-value">{{ testCase.retrievedAnswer }}</div>
              </div>
              <div
                v-if="
                  testCase.retrievedDocuments &&
                  testCase.retrievedDocuments.length > 0
                "
                class="content-row"
              >
                <div class="content-label">检索文档:</div>
                <div class="content-value tags">
                  <span
                    v-for="doc in testCase.retrievedDocuments"
                    :key="doc"
                    class="tag"
                    >{{ doc }}</span
                  >
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showAddModal"
      class="modal-overlay"
      @click="showAddModal = false"
    >
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>{{ editingTestCase ? '编辑测试用例' : '添加测试用例' }}</h3>
          <button class="close-btn" @click="showAddModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>问题 *</label>
            <textarea
              v-model="currentTestCase.question"
              placeholder="输入用户问题"
              rows="3"
            ></textarea>
          </div>
          <div class="form-group">
            <label>标准答案</label>
            <textarea
              v-model="currentTestCase.groundTruthAnswer"
              placeholder="输入标准答案（可选）"
              rows="3"
            ></textarea>
          </div>
          <div class="form-group">
            <label>相关文档（每行一个）</label>
            <textarea
              v-model="relevantDocsText"
              placeholder="输入相关文档列表（可选）"
              rows="4"
            ></textarea>
          </div>
          <div class="form-group">
            <label>检索答案</label>
            <textarea
              v-model="currentTestCase.retrievedAnswer"
              placeholder="输入RAG系统生成的答案（可选）"
              rows="3"
            ></textarea>
          </div>
          <div class="form-group">
            <label>检索文档（每行一个）</label>
            <textarea
              v-model="retrievedDocsText"
              placeholder="输入检索到的文档列表（可选）"
              rows="4"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <AppButton
            text="取消"
            variant="secondary"
            size="medium"
            @click="showAddModal = false"
          />
          <AppButton
            text="保存"
            variant="primary"
            size="medium"
            @click="saveTestCase"
          />
        </div>
      </div>
    </div>

    <div
      v-if="showEvaluationModal"
      class="modal-overlay"
      @click="showEvaluationModal = false"
    >
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>运行评估</h3>
          <button class="close-btn" @click="showEvaluationModal = false">
            ×
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>评估类型</label>
            <select v-model="evaluationType" class="form-select">
              <option value="quick">快速评估</option>
              <option value="full">完整评估（包含LLM评估）</option>
              <option value="rag-pipeline">RAG流程评估（检索+生成）</option>
              <option value="retrieval-only">检索质量评估</option>
            </select>
          </div>
          <div
            v-if="
              evaluationType === 'rag-pipeline' ||
              evaluationType === 'retrieval-only'
            "
            class="form-group"
          >
            <label>检索文档数量 (Top-K)</label>
            <input
              v-model.number="topK"
              type="number"
              min="1"
              max="20"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>数据集信息</label>
            <div class="dataset-info">
              <div class="info-item">
                <span class="info-label">测试用例数:</span>
                <span class="info-value">{{ testCases.length }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">已评估:</span>
                <span class="info-value">{{ evaluatedCount }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">待评估:</span>
                <span class="info-value">{{ pendingCount }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <AppButton
            text="取消"
            variant="secondary"
            size="medium"
            @click="showEvaluationModal = false"
          />
          <AppButton
            text="开始评估"
            variant="success"
            size="medium"
            @click="runEvaluation"
          />
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">{{ loadingText }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppButton from '../components/AppButton.vue'
import {
  createSampleDataset as createSampleDatasetApi,
  runEvaluationFromDataset,
  runFullEvaluation as runFullEvaluationApi,
  evaluateWithRAGPipeline,
  evaluateRetrievalOnly,
} from '../api/evaluationApi'

useHead({
  title: '数据集管理 - 鱼皑AI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: '创建和管理RAG评估数据集，支持导入导出和批量操作',
    },
  ],
})

const router = useRouter()
const loading = ref(false)
const loadingText = ref('')
const showAddModal = ref(false)
const showEvaluationModal = ref(false)
const editingTestCase = ref(null)
const testCases = ref([])
const currentTestCase = ref({
  id: '',
  question: '',
  groundTruthAnswer: '',
  relevantDocuments: [],
  retrievedAnswer: '',
  retrievedDocuments: [],
})
const relevantDocsText = ref('')
const retrievedDocsText = ref('')
const evaluationType = ref('quick')
const topK = ref(5)

const evaluatedCount = computed(() => {
  return testCases.value.filter(
    (tc) => tc.retrievedAnswer && tc.retrievedDocuments
  ).length
})

const pendingCount = computed(() => {
  return testCases.value.length - evaluatedCount.value
})

const createSampleDataset = async () => {
  loading.value = true
  loadingText.value = '正在创建示例数据集...'

  try {
    await createSampleDatasetApi()
    loadingText.value = '示例数据集已创建到 ./sample_dataset.json'
    setTimeout(() => {
      loading.value = false
      alert('示例数据集已创建成功！\n\n请使用导入功能加载数据集。')
    }, 1000)
  } catch (error) {
    loading.value = false
    alert('创建示例数据集失败: ' + error.message)
  }
}

const importDataset = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = (e) => {
    const file = e.target.files[0]
    const reader = new FileReader()
    reader.onload = (event) => {
      try {
        const data = JSON.parse(event.target.result)
        if (data.testCases && Array.isArray(data.testCases)) {
          testCases.value = data.testCases
          alert(`成功导入 ${data.testCases.length} 个测试用例`)
        } else {
          alert('数据集格式错误，请确保包含 testCases 数组')
        }
      } catch (error) {
        alert('解析数据集失败: ' + error.message)
      }
    }
    reader.readAsText(file)
  }
  input.click()
}

const exportDataset = () => {
  const data = { testCases: testCases.value }
  const blob = new Blob([JSON.stringify(data, null, 2)], {
    type: 'application/json',
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `dataset_${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
}

const clearDataset = () => {
  if (confirm('确定要清空所有测试用例吗？')) {
    testCases.value = []
  }
}

const editTestCase = (testCase) => {
  editingTestCase.value = testCase
  currentTestCase.value = { ...testCase }
  relevantDocsText.value = testCase.relevantDocuments
    ? testCase.relevantDocuments.join('\n')
    : ''
  retrievedDocsText.value = testCase.retrievedDocuments
    ? testCase.retrievedDocuments.join('\n')
    : ''
  showAddModal.value = true
}

const deleteTestCase = (index) => {
  if (confirm('确定要删除这个测试用例吗？')) {
    testCases.value.splice(index, 1)
  }
}

const saveTestCase = () => {
  if (!currentTestCase.value.question.trim()) {
    alert('请输入问题')
    return
  }

  currentTestCase.value.relevantDocuments = relevantDocsText.value
    .split('\n')
    .map((s) => s.trim())
    .filter((s) => s)

  currentTestCase.value.retrievedDocuments = retrievedDocsText.value
    .split('\n')
    .map((s) => s.trim())
    .filter((s) => s)

  if (editingTestCase.value) {
    const index = testCases.value.findIndex(
      (tc) => tc.id === editingTestCase.value.id
    )
    if (index !== -1) {
      testCases.value[index] = { ...currentTestCase.value }
    }
  } else {
    currentTestCase.value.id = `test_${Date.now()}`
    testCases.value.push({ ...currentTestCase.value })
  }

  showAddModal.value = false
  editingTestCase.value = null
  currentTestCase.value = {
    id: '',
    question: '',
    groundTruthAnswer: '',
    relevantDocuments: [],
    retrievedAnswer: '',
    retrievedDocuments: [],
  }
  relevantDocsText.value = ''
  retrievedDocsText.value = ''
}

const runEvaluation = async () => {
  if (testCases.value.length === 0) {
    alert('请先导入或创建测试用例')
    return
  }

  showEvaluationModal.value = false
  loading.value = true

  try {
    switch (evaluationType.value) {
      case 'quick':
        loadingText.value = '正在运行快速评估...'
        const quickResult = await runEvaluationFromDataset(testCases.value)
        loadingText.value = '评估完成！'
        setTimeout(() => {
          loading.value = false
          alert(`快速评估完成！\n\n平均得分: ${quickResult.data.summary}`)
        }, 500)
        break

      case 'full':
        loadingText.value = '正在运行完整评估（包含LLM评估）...'
        const fullResult = await runFullEvaluationApi(testCases.value)
        loadingText.value = '评估完成！'
        setTimeout(() => {
          loading.value = false
          alert(
            `完整评估完成！\n\n平均得分: ${fullResult.data.summary}\n\n报告已生成到: ./evaluation_reports/`
          )
        }, 500)
        break

      case 'rag-pipeline':
        loadingText.value = '正在执行RAG流程（检索+生成）...'
        const ragResult = await evaluateWithRAGPipeline(
          testCases.value,
          topK.value
        )
        loadingText.value = '评估完成！'
        setTimeout(() => {
          loading.value = false
          alert(
            `RAG流程评估完成！\n\n${ragResult.data.summary}\n\n使用混合检索服务进行文档检索和答案生成`
          )
        }, 500)
        break

      case 'retrieval-only':
        loadingText.value = '正在执行文档检索...'
        const retrievalResult = await evaluateRetrievalOnly(
          testCases.value,
          topK.value
        )
        loadingText.value = '评估完成！'
        setTimeout(() => {
          loading.value = false
          alert(
            `检索质量评估完成！\n\n${retrievalResult.data.summary}\n\n使用混合检索服务进行文档检索`
          )
        }, 500)
        break

      default:
        loading.value = false
        alert('未知的评估类型')
    }
  } catch (error) {
    loading.value = false
    alert('评估失败: ' + error.message)
  }
}
</script>

<style scoped>
.dataset-manager {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-light);
  padding: 0;
  font-family: var(--font-family-base);
}

.header {
  padding: 50px 20px 30px;
  text-align: center;
  background-color: var(--bg-color);
  border-bottom: 1px solid var(--border-color);
}

.main-title {
  font-family: var(--font-family-title);
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--primary-color);
  margin: 0 0 12px;
}

.subtitle {
  font-family: var(--font-family-base);
  font-size: 1rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
}

.content-container {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 30px;
  flex: 1;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.dataset-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--card-border);
}

.stat-icon {
  font-size: 2rem;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--primary-color);
}

.test-cases-section {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  padding: 25px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--card-border);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid var(--primary-color);
}

.section-header h3 {
  font-family: var(--font-family-title);
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.4;
}

.empty-text {
  font-size: 1rem;
  color: var(--text-secondary);
  margin-bottom: 25px;
}

.empty-actions {
  display: flex;
  gap: 12px;
}

.test-cases-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.test-case-item {
  background: rgba(22, 119, 255, 0.03);
  border-radius: var(--radius-md);
  padding: 20px;
  border: 1px solid var(--border-light);
  transition: all 0.3s ease;
}

.test-case-item:hover {
  background: rgba(22, 119, 255, 0.08);
  border-color: var(--primary-color);
}

.test-case-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.test-case-id {
  font-family: var(--font-family-title);
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.test-case-actions {
  display: flex;
  gap: 8px;
}

.test-case-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.content-label {
  min-width: 100px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
  padding-top: 2px;
}

.content-value {
  flex: 1;
  font-size: 0.9rem;
  color: var(--text-primary);
  line-height: 1.6;
}

.content-value.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: var(--primary-color);
  color: white;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 500;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid var(--border-light);
}

.modal-header h3 {
  font-family: var(--font-family-title);
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  color: var(--text-primary);
}

.modal-body {
  padding: 25px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  font-family: var(--font-family-base);
  font-size: 0.9rem;
  color: var(--text-primary);
  resize: vertical;
  transition: all 0.3s ease;
}

.form-group textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
}

.form-select,
.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  font-family: var(--font-family-base);
  font-size: 0.9rem;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.form-select:focus,
.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
}

.dataset-info {
  background: rgba(22, 119, 255, 0.05);
  border-radius: var(--radius-md);
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.info-value {
  font-size: 0.95rem;
  color: var(--primary-color);
  font-weight: 600;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 25px;
  border-top: 1px solid var(--border-light);
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 20px;
}

.loading-text {
  color: white;
  font-size: 1.1rem;
  font-weight: 500;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1024px) {
  .dataset-stats {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .main-title {
    font-size: 2rem;
  }

  .subtitle {
    font-size: 0.9rem;
  }

  .content-container {
    padding: 0 20px;
  }

  .action-bar {
    flex-direction: column;
  }

  .action-bar .app-button {
    width: 100%;
  }

  .test-case-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .content-row {
    flex-direction: column;
  }

  .content-label {
    min-width: auto;
  }
}
</style>
