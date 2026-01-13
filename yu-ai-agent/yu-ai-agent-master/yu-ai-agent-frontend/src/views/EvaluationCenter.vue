<template>
  <div class="evaluation-center">
    <div class="header">
      <h2 class="main-title">RAG评估中心</h2>
      <p class="subtitle">专业的RAG系统质量评估平台</p>
    </div>

    <div class="category-grid">
      <div class="category-section">
        <h3>快速开始</h3>
        <div class="card-grid">
          <div
            class="eval-card quick-card"
            @click="navigateTo('/evaluation/dataset')"
          >
            <div class="card-icon">📊</div>
            <div class="card-title">数据集管理</div>
            <div class="card-desc">创建和管理评估数据集</div>
            <AppButton text="进入" variant="primary" size="small" />
          </div>
          <div
            class="eval-card quick-card"
            @click="navigateTo('/evaluation/dashboard')"
          >
            <div class="card-icon">📈</div>
            <div class="card-title">评估仪表板</div>
            <div class="card-desc">查看评估结果和报告</div>
            <AppButton text="进入" variant="primary" size="small" />
          </div>
        </div>
      </div>

      <div class="category-section">
        <h3>评估工具</h3>
        <div class="card-grid">
          <div class="eval-card tool-card" @click="runQuickEvaluation">
            <div class="card-icon">⚡</div>
            <div class="card-title">快速评估</div>
            <div class="card-desc">使用示例数据集快速测试</div>
            <AppButton text="运行" variant="success" size="small" />
          </div>
          <div class="eval-card tool-card" @click="runFullEvaluation">
            <div class="card-icon">🔬</div>
            <div class="card-title">完整评估</div>
            <div class="card-desc">包含LLM-as-a-Judge评估</div>
            <AppButton text="运行" variant="warning" size="small" />
          </div>
          <div class="eval-card tool-card" @click="runRAGPipelineEvaluation">
            <div class="card-icon">🔄</div>
            <div class="card-title">RAG流程评估</div>
            <div class="card-desc">端到端检索+生成评估</div>
            <AppButton text="运行" variant="primary" size="small" />
          </div>
          <div class="eval-card tool-card" @click="runRetrievalOnlyEvaluation">
            <div class="card-icon">🔍</div>
            <div class="card-title">检索质量评估</div>
            <div class="card-desc">仅评估混合检索质量</div>
            <AppButton text="运行" variant="info" size="small" />
          </div>
        </div>
      </div>

      <div class="category-section">
        <h3>评估指标</h3>
        <div class="metrics-grid">
          <div class="metric-item">
            <div class="metric-header">
              <span class="metric-icon">🎯</span>
              <span class="metric-name">检索质量</span>
            </div>
            <div class="metric-list">
              <div class="metric-tag">Precision@K</div>
              <div class="metric-tag">Recall@K</div>
              <div class="metric-tag">MRR</div>
              <div class="metric-tag">NDCG@K</div>
            </div>
          </div>
          <div class="metric-item">
            <div class="metric-header">
              <span class="metric-icon">✨</span>
              <span class="metric-name">生成质量</span>
            </div>
            <div class="metric-list">
              <div class="metric-tag">Faithfulness</div>
              <div class="metric-tag">Answer Relevance</div>
              <div class="metric-tag">Context Precision</div>
              <div class="metric-tag">Context Recall</div>
            </div>
          </div>
        </div>
      </div>

      <div class="category-section">
        <h3>最近评估</h3>
        <div class="recent-evaluations">
          <div v-if="recentEvaluations.length === 0" class="empty-state">
            <div class="empty-icon">📋</div>
            <div class="empty-text">暂无评估记录</div>
            <AppButton
              text="创建评估"
              variant="primary"
              size="small"
              @click="navigateTo('/evaluation/dataset')"
            />
          </div>
          <div v-else class="evaluation-list">
            <div
              v-for="evaluation in recentEvaluations"
              :key="evaluation.id"
              class="evaluation-item"
              @click="viewEvaluation(evaluation.id)"
            >
              <div class="eval-info">
                <div class="eval-title">{{ evaluation.name }}</div>
                <div class="eval-time">
                  {{ formatTime(evaluation.timestamp) }}
                </div>
              </div>
              <div class="eval-score" :class="getScoreClass(evaluation.score)">
                {{ evaluation.score.toFixed(3) }}
              </div>
            </div>
          </div>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppButton from '../components/AppButton.vue'
import {
  runEvaluationFromDataset,
  runFullEvaluation as runFullEvaluationApi,
  createSampleDataset,
  evaluateWithRAGPipeline,
  evaluateRetrievalOnly,
} from '../api/evaluationApi'

useHead({
  title: 'RAG评估中心 - 鱼皑AI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: '专业的RAG系统质量评估平台，提供全面的检索和生成质量评估指标',
    },
  ],
})

const router = useRouter()
const loading = ref(false)
const loadingText = ref('')
const recentEvaluations = ref([])

const navigateTo = (path) => {
  router.push(path)
}

const runQuickEvaluation = async () => {
  loading.value = true
  loadingText.value = '正在创建示例数据集...'

  try {
    await createSampleDataset()
    loadingText.value = '正在运行快速评估...'

    const result = await runEvaluationFromDataset('./sample_dataset.json')

    loadingText.value = '评估完成！'
    setTimeout(() => {
      loading.value = false
      alert(`快速评估完成！\n\n平均得分: ${result.data.summary}`)
    }, 500)
  } catch (error) {
    loading.value = false
    alert('评估失败: ' + error.message)
  }
}

const runFullEvaluation = async () => {
  loading.value = true
  loadingText.value = '正在创建示例数据集...'

  try {
    await createSampleDataset()
    loadingText.value = '正在运行完整评估（包含LLM评估）...'

    const result = await runFullEvaluationApi('./sample_dataset.json')

    loadingText.value = '评估完成！'
    setTimeout(() => {
      loading.value = false
      alert(
        `完整评估完成！\n\n平均得分: ${result.data.summary}\n\n报告已生成到: ./evaluation_reports/`
      )
    }, 500)
  } catch (error) {
    loading.value = false
    alert('评估失败: ' + error.message)
  }
}

const runRAGPipelineEvaluation = async () => {
  loading.value = true
  loadingText.value = '正在创建示例数据集...'

  try {
    await createSampleDataset()
    loadingText.value = '正在执行RAG流程（检索+生成）...'

    const result = await evaluateWithRAGPipeline('./sample_dataset.json', 5)

    loadingText.value = '评估完成！'
    setTimeout(() => {
      loading.value = false
      alert(
        `RAG流程评估完成！\n\n${result.data.summary}\n\n使用混合检索服务进行文档检索和答案生成`
      )
    }, 500)
  } catch (error) {
    loading.value = false
    alert('评估失败: ' + error.message)
  }
}

const runRetrievalOnlyEvaluation = async () => {
  loading.value = true
  loadingText.value = '正在创建示例数据集...'

  try {
    await createSampleDataset()
    loadingText.value = '正在执行文档检索...'

    const result = await evaluateRetrievalOnly('./sample_dataset.json', 5)

    loadingText.value = '评估完成！'
    setTimeout(() => {
      loading.value = false
      alert(
        `检索质量评估完成！\n\n${result.data.summary}\n\n使用混合检索服务进行文档检索`
      )
    }, 500)
  } catch (error) {
    loading.value = false
    alert('评估失败: ' + error.message)
  }
}

const viewEvaluation = (id) => {
  router.push(`/evaluation/result/${id}`)
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const getScoreClass = (score) => {
  if (score >= 0.8) return 'score-high'
  if (score >= 0.6) return 'score-medium'
  return 'score-low'
}
</script>

<style scoped>
.evaluation-center {
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

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 25px;
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 30px;
  flex: 1;
}

.category-section {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 25px;
  border: 1px solid var(--card-border);
}

.category-section h3 {
  font-family: var(--font-family-title);
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--primary-color);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.eval-card {
  background: rgba(22, 119, 255, 0.03);
  border-radius: var(--radius-md);
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  border: 1px solid var(--border-light);
}

.eval-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
  background: rgba(22, 119, 255, 0.08);
}

.card-icon {
  font-size: 2rem;
  margin-bottom: 12px;
}

.card-title {
  font-family: var(--font-family-title);
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.card-desc {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-bottom: 15px;
  line-height: 1.5;
}

.metrics-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 15px;
}

.metric-item {
  background: rgba(22, 119, 255, 0.03);
  border-radius: var(--radius-md);
  padding: 18px;
  border: 1px solid var(--border-light);
}

.metric-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.metric-icon {
  font-size: 1.5rem;
}

.metric-name {
  font-family: var(--font-family-title);
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.metric-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.metric-tag {
  background: var(--primary-color);
  color: white;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 500;
}

.recent-evaluations {
  min-height: 200px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 15px;
  opacity: 0.5;
}

.empty-text {
  font-size: 0.95rem;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.evaluation-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.evaluation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: rgba(22, 119, 255, 0.03);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--border-light);
}

.evaluation-item:hover {
  background: rgba(22, 119, 255, 0.08);
  border-color: var(--primary-color);
  transform: translateX(3px);
}

.eval-info {
  flex: 1;
}

.eval-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.eval-time {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.eval-score {
  font-size: 1.2rem;
  font-weight: 700;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  min-width: 70px;
  text-align: center;
}

.score-high {
  background: rgba(82, 196, 26, 0.15);
  color: #52c41a;
}

.score-medium {
  background: rgba(250, 173, 20, 0.15);
  color: #faad14;
}

.score-low {
  background: rgba(245, 34, 45, 0.15);
  color: #f5222d;
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
  .category-grid {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 0 20px;
  }

  .card-grid {
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

  .category-section {
    padding: 20px;
  }

  .category-section h3 {
    font-size: 1.1rem;
  }
}
</style>
