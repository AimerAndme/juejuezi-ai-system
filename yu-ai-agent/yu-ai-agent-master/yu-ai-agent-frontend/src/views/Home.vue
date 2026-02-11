<template>
  <div class="home-container">
    <div class="header">
      <h1 class="main-title">AI智能应用平台</h1>
      <p class="subtitle">专业AI解决方案，助力业务发展</p>
    </div>

    <div class="apps-container">
      <div class="app-card mine-card" @click="navigateTo('/mine-agent')">
        <div class="card-header">
          <div class="app-icon mine-icon">⛏️</div>
          <div class="app-title">AI矿山专家</div>
        </div>
        <div class="app-info">
          <div class="app-desc">专业矿业技术顾问，提供全方位矿山解决方案</div>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">🔍</span>
              <span class="feature-text">矿业技术咨询</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚙️</span>
              <span class="feature-text">设备选型建议</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📊</span>
              <span class="feature-text">安全生产指导</span>
            </div>
          </div>
        </div>
        <AppButton text="专家咨询" variant="primary" size="large" />
      </div>

      <div class="app-card file-card" @click="navigateTo('/file-center')">
        <div class="card-header">
          <div class="app-icon file-icon">📦</div>
          <div class="app-title">文件中心</div>
        </div>
        <div class="app-info">
          <div class="app-desc">高效文件管理系统，支持大文件传输与智能存储</div>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">⬆️</span>
              <span class="feature-text">大文件上传</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🔄</span>
              <span class="feature-text">断点续传</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span class="feature-text">秒传技术</span>
            </div>
          </div>
        </div>
        <AppButton text="进入中心" variant="primary" size="large" />
      </div>

      <div class="app-card data-card" @click="navigateTo('/data-center')">
        <div class="card-header">
          <div class="app-icon data-icon">📊</div>
          <div class="app-title">数据中心</div>
        </div>
        <div class="app-info">
          <div class="app-desc">矿山地质数据管理平台，支持多维数据统一管理</div>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">🏔️</span>
              <span class="feature-text">矿区与煤层管理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🔩</span>
              <span class="feature-text">钻孔与巷道数据</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📈</span>
              <span class="feature-text">储量统计分析</span>
            </div>
          </div>
        </div>
        <AppButton text="进入中心" variant="primary" size="large" />
      </div>

      <div class="app-card eval-card" @click="navigateTo('/evaluation')">
        <div class="card-header">
          <div class="app-icon eval-icon">🎯</div>
          <div class="app-title">RAG评估中心</div>
        </div>
        <div class="app-info">
          <div class="app-desc">
            专业的RAG系统质量评估平台，多维度评估检索生成效果
          </div>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">📊</span>
              <span class="feature-text">数据集管理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📈</span>
              <span class="feature-text">评估指标分析</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">💡</span>
              <span class="feature-text">优化建议</span>
            </div>
          </div>
        </div>
        <AppButton text="进入中心" variant="primary" size="large" />
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppFooter from '../components/layout/AppFooter.vue'
import AppButton from '../components/common/AppButton.vue'

// 设置页面标题和元数据
useHead({
  title: 'AI智能应用平台 - 首页',
  meta: [
    {
      name: 'description',
      content:
        'AI智能应用平台提供AI矿山专家、文件中心和数据中心服务，满足您的各种AI应用需求',
    },
    {
      name: 'keywords',
      content: 'AI智能体,AI应用,AI矿山专家,文件中心,数据中心,智能平台,首页',
    },
  ],
})

const router = useRouter()

const navigateTo = (path) => {
  console.log('点击跳转:', path)
  router
    .push(path)
    .then(() => {
      console.log('路由跳转成功:', path)
    })
    .catch((err) => {
      console.error('路由跳转失败:', err)
    })
}
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-light);
  padding: 0;
  font-family: var(--font-family-base);
  transition: background-color 0.3s ease;
}

/* 简约标题设计 */
.header {
  padding: 60px 20px 40px;
  text-align: center;
  background-color: var(--bg-color);
  border-bottom: 1px solid var(--border-color);
}

.main-title {
  font-family: var(--font-family-title);
  font-size: 3rem;
  font-weight: 700;
  color: var(--primary-color);
  margin: 0 0 16px;
  animation: fadeIn 0.6s ease-in;
}

.subtitle {
  font-family: var(--font-family-base);
  font-size: 1.1rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
  font-weight: 400;
  animation: fadeIn 0.6s ease-in 0.2s both;
}

.apps-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 30px;
  flex: 1;
}

.app-card {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 30px;
  cursor: pointer;
  transition: all var(--transition-normal) ease;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--card-border);
  animation: fadeInUp 0.6s ease-out;
  min-height: 300px;
  position: relative;
  overflow: hidden;
}

.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  transform: scaleX(0);
  transform-origin: left;
  transition: transform var(--transition-normal) ease;
}

.mine-card {
  animation-delay: 0.1s;
}

.file-card {
  animation-delay: 0.2s;
}

.data-card {
  animation-delay: 0.3s;
}

.eval-card {
  animation-delay: 0.4s;
}

.app-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-xl);
  border-color: var(--primary-color);
}

.app-card:hover::before {
  transform: scaleX(1);
}

.app-card:hover .app-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: var(--shadow-lg);
}

.app-card:hover .app-title {
  color: var(--primary-color);
}

.app-card:hover .feature-item {
  transform: translateX(6px);
  background: rgba(22, 119, 255, 0.1);
}

.app-card:active {
  transform: translateY(-2px);
}

/* 卡片内容动画 */
.app-card .app-info {
  transition: transform var(--transition-normal) ease;
}

.app-card:hover .app-info {
  transform: translateY(-4px);
}

/* 卡片按钮动画 */
.app-card .app-button {
  transition: all var(--transition-normal) ease;
}

.app-card:hover .app-button {
  transform: translateY(4px);
  box-shadow: var(--shadow-md);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 22px;
  width: 100%;
}

.app-icon {
  font-size: 2.2rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.mine-icon {
  background: linear-gradient(135deg, var(--light-orange), var(--orange));
  color: white;
  border: 2px solid var(--card-bg);
}

.file-icon {
  background: linear-gradient(135deg, #56c596, #43b97f);
  color: white;
  border: 2px solid var(--card-bg);
}

.data-icon {
  background: linear-gradient(135deg, #0984e3, #0876cc);
  color: white;
  border: 2px solid var(--card-bg);
}

.eval-icon {
  background: linear-gradient(135deg, #a29bfe, #6c5ce7);
  color: white;
  border: 2px solid var(--card-bg);
}

.app-title {
  font-family: var(--font-family-title);
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
}

.app-info {
  margin-bottom: 20px;
  width: 100%;
  flex: 1;
}

.app-desc {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 18px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(22, 119, 255, 0.05);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--primary-color);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(22, 119, 255, 0.1);
  transform: translateX(3px);
}

.feature-icon {
  font-size: 1.1rem;
  flex-shrink: 0;
}

.feature-text {
  font-size: 0.88rem;
  color: var(--text-primary);
  font-weight: 500;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .apps-container {
    grid-template-columns: 1fr;
    gap: 25px;
    padding: 0 25px;
  }
}

@media (max-width: 768px) {
  .main-title {
    font-size: 2.2rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .apps-container {
    gap: 20px;
    margin: 30px auto;
    padding: 0 20px;
  }

  .app-card {
    padding: 24px;
    min-height: auto;
  }

  .card-header {
    gap: 14px;
  }

  .app-icon {
    font-size: 1.8rem;
    width: 54px;
    height: 54px;
  }

  .app-title {
    font-size: 1.25rem;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 40px 15px 30px;
  }

  .main-title {
    font-size: 1.8rem;
  }

  .subtitle {
    font-size: 0.9rem;
  }

  .apps-container {
    margin: 25px auto;
    padding: 0 15px;
  }

  .app-card {
    padding: 20px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .app-desc {
    font-size: 0.9rem;
  }

  .feature-item {
    padding: 7px 10px;
  }

  .feature-text {
    font-size: 0.85rem;
  }
}
</style>
