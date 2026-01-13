<template>
  <div class="evaluation-dashboard">
    <div class="header">
      <h2 class="main-title">评估仪表板</h2>
      <p class="subtitle">查看和分析RAG评估结果</p>
    </div>

    <div class="content-container">
      <div v-if="!evaluationResult" class="empty-state">
        <div class="empty-icon">📊</div>
        <div class="empty-text">暂无评估结果</div>
        <div class="empty-actions">
          <AppButton
            text="运行评估"
            variant="primary"
            size="medium"
            @click="runNewEvaluation"
          />
          <AppButton
            text="返回中心"
            variant="secondary"
            size="medium"
            @click="navigateTo('/evaluation')"
          />
        </div>
      </div>

      <div v-else class="dashboard-content">
        <div class="summary-section">
          <h3>评估概览</h3>
          <div class="summary-grid">
            <div class="summary-card">
              <div class="summary-icon">📋</div>
              <div class="summary-info">
                <div class="summary-label">评估ID</div>
                <div class="summary-value">
                  {{ evaluationResult.evaluationId }}
                </div>
              </div>
            </div>
            <div class="summary-card">
              <div class="summary-icon">📊</div>
              <div class="summary-info">
                <div class="summary-label">测试用例数</div>
                <div class="summary-value">
                  {{ evaluationResult.totalTestCases }}
                </div>
              </div>
            </div>
            <div class="summary-card">
              <div class="summary-icon">⏱️</div>
              <div class="summary-info">
                <div class="summary-label">评估时间</div>
                <div class="summary-value">
                  {{ formatTime(evaluationResult.timestamp) }}
                </div>
              </div>
            </div>
            <div class="summary-card">
              <div class="summary-icon">📈</div>
              <div class="summary-info">
                <div class="summary-label">平均得分</div>
                <div class="summary-value" :class="getScoreClass(averageScore)">
                  {{ averageScore.toFixed(4) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="metrics-section">
          <h3>评估指标详情</h3>
          <div class="metrics-grid">
            <div
              v-for="(value, metric) in evaluationResult.metricAverages"
              :key="metric"
              class="metric-card"
            >
              <div class="metric-header">
                <div class="metric-title">{{ metric }}</div>
                <div class="metric-score" :class="getScoreClass(value)">
                  {{ value.toFixed(4) }}
                </div>
              </div>
              <div class="metric-progress">
                <div class="progress-bar">
                  <div
                    class="progress-fill"
                    :style="{
                      width: value * 100 + '%',
                      backgroundColor: getScoreColor(value),
                    }"
                  ></div>
                </div>
              </div>
              <div class="metric-grade">{{ getGrade(value) }}</div>
            </div>
          </div>
        </div>

        <div class="analysis-section">
          <h3>详细分析</h3>
          <div class="analysis-grid">
            <div class="analysis-card">
              <div class="analysis-header">
                <div class="analysis-icon">🎯</div>
                <div class="analysis-title">检索质量分析</div>
              </div>
              <div class="analysis-content">
                <div v-if="getRetrievalMetrics().length > 0">
                  <div
                    v-for="metric in getRetrievalMetrics()"
                    :key="metric.name"
                    class="analysis-item"
                  >
                    <div class="analysis-item-header">
                      <span class="analysis-item-name">{{ metric.name }}</span>
                      <span
                        class="analysis-item-score"
                        :class="getScoreClass(metric.value)"
                      >
                        {{ metric.value.toFixed(4) }}
                      </span>
                    </div>
                    <div class="analysis-item-desc">
                      {{ getMetricDescription(metric.name) }}
                    </div>
                  </div>
                </div>
                <div v-else class="no-data">暂无检索质量数据</div>
              </div>
            </div>

            <div class="analysis-card">
              <div class="analysis-header">
                <div class="analysis-icon">✨</div>
                <div class="analysis-title">生成质量分析</div>
              </div>
              <div class="analysis-content">
                <div v-if="getGenerationMetrics().length > 0">
                  <div
                    v-for="metric in getGenerationMetrics()"
                    :key="metric.name"
                    class="analysis-item"
                  >
                    <div class="analysis-item-header">
                      <span class="analysis-item-name">{{ metric.name }}</span>
                      <span
                        class="analysis-item-score"
                        :class="getScoreClass(metric.value)"
                      >
                        {{ metric.value.toFixed(4) }}
                      </span>
                    </div>
                    <div class="analysis-item-desc">
                      {{ getMetricDescription(metric.name) }}
                    </div>
                  </div>
                </div>
                <div v-else class="no-data">暂无生成质量数据</div>
              </div>
            </div>
          </div>
        </div>

        <div class="recommendations-section">
          <h3>改进建议</h3>
          <div class="recommendations-list">
            <div
              v-for="(rec, index) in getRecommendations()"
              :key="index"
              class="recommendation-item"
            >
              <div class="recommendation-icon">{{ rec.icon }}</div>
              <div class="recommendation-content">
                <div class="recommendation-title">{{ rec.title }}</div>
                <div class="recommendation-desc">{{ rec.description }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="actions-section">
          <AppButton
            text="运行新评估"
            variant="primary"
            size="medium"
            @click="runNewEvaluation"
          />
          <AppButton
            text="导出报告"
            variant="secondary"
            size="medium"
            @click="exportReport"
          />
          <AppButton
            text="返回中心"
            variant="secondary"
            size="medium"
            @click="navigateTo('/evaluation')"
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
import { useRouter, useRoute } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppButton from '../components/AppButton.vue'
import {
  runEvaluationFromDataset,
  runFullEvaluation as runFullEvaluationApi,
  createSampleDataset,
} from '../api/evaluationApi'

useHead({
  title: '评估仪表板 - 鱼皑AI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: '查看和分析RAG评估结果，包含详细的指标分析和改进建议',
    },
  ],
})

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const loadingText = ref('')
const evaluationResult = ref(null)

const averageScore = computed(() => {
  if (!evaluationResult.value || !evaluationResult.value.metricAverages)
    return 0
  const values = Object.values(evaluationResult.value.metricAverages)
  return values.reduce((sum, val) => sum + val, 0) / values.length
})

const runNewEvaluation = async () => {
  loading.value = true
  loadingText.value = '正在创建示例数据集...'

  try {
    await createSampleDataset()
    loadingText.value = '正在运行评估...'

    const result = await runFullEvaluationApi('./sample_dataset.json')
    evaluationResult.value = result.data

    loading.value = false
  } catch (error) {
    loading.value = false
    alert('评估失败: ' + error.message)
  }
}

const exportReport = () => {
  if (!evaluationResult.value) return

  const data = JSON.stringify(evaluationResult.value, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `evaluation_report_${evaluationResult.value.evaluationId}.json`
  a.click()
  URL.revokeObjectURL(url)
}

const navigateTo = (path) => {
  router.push(path)
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

const getScoreClass = (score) => {
  if (score >= 0.8) return 'score-high'
  if (score >= 0.6) return 'score-medium'
  return 'score-low'
}

const getScoreColor = (score) => {
  if (score >= 0.8) return '#52c41a'
  if (score >= 0.6) return '#faad14'
  return '#f5222d'
}

const getGrade = (score) => {
  if (score >= 0.9) return '优秀 ⭐⭐⭐⭐⭐'
  if (score >= 0.8) return '良好 ⭐⭐⭐⭐'
  if (score >= 0.7) return '中等 ⭐⭐⭐'
  if (score >= 0.6) return '及格 ⭐⭐'
  return '需改进 ⭐'
}

const getRetrievalMetrics = () => {
  if (!evaluationResult.value || !evaluationResult.value.metricAverages)
    return []
  const retrievalMetrics = ['Precision@K', 'Recall@K', 'MRR', 'NDCG@K']
  return Object.entries(evaluationResult.value.metricAverages)
    .filter(([key]) => retrievalMetrics.some((m) => key.includes(m)))
    .map(([name, value]) => ({ name, value }))
}

const getGenerationMetrics = () => {
  if (!evaluationResult.value || !evaluationResult.value.metricAverages)
    return []
  const generationMetrics = [
    'Faithfulness',
    'Answer Relevance',
    'Context Precision',
    'Context Recall',
  ]
  return Object.entries(evaluationResult.value.metricAverages)
    .filter(([key]) => generationMetrics.some((m) => key.includes(m)))
    .map(([name, value]) => ({ name, value }))
}

const getMetricDescription = (metricName) => {
  const descriptions = {
    'Precision@K': '前K个结果中相关文档的比例',
    'Recall@K': '前K个结果覆盖了多少相关文档',
    MRR: '第一个相关文档的排名倒数',
    'NDCG@K': '考虑排序质量的归一化指标',
    Faithfulness: '答案是否忠实于检索到的文档',
    'Answer Relevance': '答案是否回答了用户问题',
    'Context Precision': '检索到的文档是否包含答案',
    'Context Recall': '检索到的文档是否覆盖了所有必要信息',
  }
  return descriptions[metricName] || '暂无描述'
}

const getRecommendations = () => {
  if (!evaluationResult.value || !evaluationResult.value.metricAverages)
    return []

  const recommendations = []
  const metrics = evaluationResult.value.metricAverages

  if (metrics['Precision@K'] < 0.7) {
    recommendations.push({
      icon: '🎯',
      title: '优化检索精度',
      description:
        'Precision@K较低，建议优化检索算法、调整向量维度或改进文档切分策略',
    })
  }

  if (metrics['Recall@K'] < 0.7) {
    recommendations.push({
      icon: '📊',
      title: '提升检索召回率',
      description:
        'Recall@K较低，建议增加检索结果数量、优化文档索引或使用混合检索',
    })
  }

  if (metrics['Faithfulness'] < 0.7) {
    recommendations.push({
      icon: '✨',
      title: '提高答案忠实度',
      description: 'Faithfulness较低，检查提示词设计，确保模型基于上下文回答',
    })
  }

  if (metrics['Answer Relevance'] < 0.7) {
    recommendations.push({
      icon: '💡',
      title: '增强答案相关性',
      description: 'Answer Relevance较低，优化检索查询或调整生成模型参数',
    })
  }

  if (recommendations.length === 0) {
    recommendations.push({
      icon: '🎉',
      title: '表现优秀',
      description: '所有指标都表现良好，继续保持！',
    })
  }

  return recommendations
}
</script>

<style scoped>
.evaluation-dashboard {
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 25px;
  opacity: 0.4;
}

.empty-text {
  font-size: 1.2rem;
  color: var(--text-secondary);
  margin-bottom: 30px;
}

.empty-actions {
  display: flex;
  gap: 15px;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.summary-section,
.metrics-section,
.analysis-section,
.recommendations-section {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  padding: 25px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--card-border);
}

.summary-section h3,
.metrics-section h3,
.analysis-section h3,
.recommendations-section h3 {
  font-family: var(--font-family-title);
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--primary-color);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.summary-card {
  background: rgba(22, 119, 255, 0.03);
  border-radius: var(--radius-md);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  border: 1px solid var(--border-light);
}

.summary-icon {
  font-size: 2rem;
}

.summary-info {
  flex: 1;
}

.summary-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.summary-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
  word-break: break-all;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.metric-card {
  background: rgba(22, 119, 255, 0.03);
  border-radius: var(--radius-md);
  padding: 20px;
  border: 1px solid var(--border-light);
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.metric-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
  word-break: break-word;
}

.metric-score {
  font-size: 1.2rem;
  font-weight: 700;
  padding: 4px 8px;
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

.metric-progress {
  margin-bottom: 10px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 4px;
}

.metric-grade {
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-align: center;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.analysis-card {
  background: rgba(22, 119, 255, 0.03);
  border-radius: var(--radius-md);
  padding: 20px;
  border: 1px solid var(--border-light);
}

.analysis-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.analysis-icon {
  font-size: 1.5rem;
}

.analysis-title {
  font-family: var(--font-family-title);
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.analysis-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.analysis-item {
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--primary-color);
}

.analysis-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.analysis-item-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-primary);
}

.analysis-item-score {
  font-size: 1rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

.analysis-item-desc {
  font-size: 0.8rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.no-data {
  text-align: center;
  padding: 30px;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.recommendations-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.recommendation-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: rgba(22, 119, 255, 0.03);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  transition: all 0.3s ease;
}

.recommendation-item:hover {
  background: rgba(22, 119, 255, 0.08);
  transform: translateX(3px);
}

.recommendation-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.recommendation-content {
  flex: 1;
}

.recommendation-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.recommendation-desc {
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

.actions-section {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding: 20px;
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--card-border);
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
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .analysis-grid {
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

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .actions-section {
    flex-direction: column;
  }

  .actions-section .app-button {
    width: 100%;
  }
}
</style>
