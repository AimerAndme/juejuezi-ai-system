<template>
  <div class="home-container">
    <StarryBackground />
    <div class="header">
      <div class="glitch-wrapper">
        <h1 class="glitch-title">AI超级智能体</h1>
      </div>
      <p class="subtitle">/ 探索AI的无限可能 /</p>
      <div class="cyber-line"></div>
    </div>

    <div class="apps-container">
      <div class="app-card love-card" @click="navigateTo('/love-master')">
        <div class="card-glow"></div>
        <div class="card-header">
          <div class="app-icon love-icon">❤️</div>
          <div class="app-title">AI恋爱大师</div>
        </div>
        <div class="app-info">
          <div class="app-desc">智能情感顾问，为您的恋爱之路保驾护航</div>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">💬</span>
              <span class="feature-text">情感分析与建议</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">💝</span>
              <span class="feature-text">恋爱技巧指导</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🎯</span>
              <span class="feature-text">关系问题解答</span>
            </div>
          </div>
        </div>
        <AppButton text="开始咨询" variant="primary" size="large" />
      </div>

      <div class="app-card mine-card" @click="navigateTo('/mine-agent')">
        <div class="card-glow"></div>
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

      <div class="app-card super-card" @click="navigateTo('/super-agent')">
        <div class="card-glow"></div>
        <div class="card-header">
          <div class="app-icon robot-icon">🤖</div>
          <div class="app-title">AI超级智能体</div>
        </div>
        <div class="app-info">
          <div class="app-desc">全能型AI助手，多场景智能问答与任务处理</div>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">🧠</span>
              <span class="feature-text">智能问答系统</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🔧</span>
              <span class="feature-text">多领域专业知识</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span class="feature-text">快速响应处理</span>
            </div>
          </div>
        </div>
        <AppButton text="立即使用" variant="primary" size="large" />
      </div>

      <div class="app-card file-card" @click="navigateTo('/file-center')">
        <div class="card-glow"></div>
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
        <div class="card-glow"></div>
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
    </div>

    <div class="cyber-circles">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppFooter from '../components/AppFooter.vue'
import AppButton from '../components/AppButton.vue'
import StarryBackground from '../components/StarryBackground.vue'

// 设置页面标题和元数据
useHead({
  title: 'AI超级智能体应用平台 - 首页',
  meta: [
    {
      name: 'description',
      content:
        'AI超级智能体应用平台提供AI恋爱大师、AI矿山专家和AI超级智能体服务，满足您的各种AI对话需求',
    },
    {
      name: 'keywords',
      content:
        'AI智能体,AI应用,AI恋爱大师,AI矿山专家,AI助手,智能对话,鱼皮,AI超级智能体,首页',
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
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;500;700&display=swap');

/* 全局样式变量 */
:root {
  --light-blue: #add8e6;
  --sky-blue: #87ceeb;
  --light-gray-blue: #ddeeff;
  --light-purple: #e0bbe4;
  --cyber-dark: #111122;
  --cyber-light: #edf7ff;
  --light-bg: #f5faff;
  --light-orange: #ffd580; /* 添加浅橙色 */
  --orange: #ffa500; /* 添加橙色 */
}

.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-light);
  position: relative;
  overflow: hidden;
  padding-bottom: 0;
  font-family: var(--font-family-base);
  transition: background-color 0.3s ease;
}

/* 赛博朋克风格标题 */
.header {
  padding: 70px 20px 50px;
  text-align: center;
  background-color: transparent;
  position: relative;
  z-index: 2;
}

.glitch-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

.glitch-title {
  font-family: var(--font-family-title);
  font-size: 3.2rem;
  font-weight: 700;
  color: var(--primary-color);
  letter-spacing: 2px;
  position: relative;
  animation: fadeIn 0.6s ease-in;
}

.subtitle {
  font-family: var(--font-family-base);
  font-size: 1.2rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto 20px;
  letter-spacing: 3px;
  text-transform: uppercase;
  font-weight: 500;
}

.cyber-line {
  height: 2px;
  width: 80%;
  max-width: 600px;
  margin: 0 auto;
  background: linear-gradient(
    90deg,
    transparent,
    var(--primary-color),
    transparent
  );
  position: relative;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
}

.cyber-line::before,
.cyber-line::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background-color: var(--primary-color);
  border-radius: 50%;
  transform: translateY(-50%);
  box-shadow: 0 0 10px 2px var(--primary-color);
  border: 2px solid white;
}

.cyber-line::before {
  left: 20%;
}

.cyber-line::after {
  right: 20%;
}

.apps-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 40px;
  max-width: 1400px;
  margin: 60px auto;
  padding: 0 40px;
  flex: 1;
  position: relative;
  z-index: 2;
}

.app-card {
  background: var(--bg-card);
  border-radius: 16px;
  box-shadow: var(--shadow-card);
  padding: 35px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  border: 1px solid var(--card-border);
  animation: fadeIn 0.6s ease-in;
  min-height: 320px;
  pointer-events: auto;
}

.love-card {
  animation-delay: 0.1s;
}

.mine-card {
  animation-delay: 0.2s;
}

.super-card {
  animation-delay: 0.3s;
}

.file-card {
  animation-delay: 0.4s;
}

.data-card {
  animation-delay: 0.5s;
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle at center,
    rgba(22, 119, 255, 0.1) 0%,
    transparent 70%
  );
  opacity: 0;
  transition: opacity 0.5s;
  pointer-events: none;
}

.app-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border-color: var(--primary-color);
}

.app-card:hover .card-glow {
  opacity: 1;
}

.app-card:hover .app-icon {
  transform: scale(1.1) rotate(5deg);
}

.app-card:hover .feature-list {
  transform: translateY(0);
  opacity: 1;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 25px;
  width: 100%;
}

.app-icon {
  font-size: 2.5rem;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  position: relative;
  z-index: 1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.love-icon {
  background: linear-gradient(135deg, #ff6b9d, #c44569);
  box-shadow: 0 0 20px rgba(255, 107, 157, 0.5);
  border: 2px solid white;
}

.mine-icon {
  background: linear-gradient(
    135deg,
    var(--light-orange),
    var(--orange)
  ); /* 使用橙色系表示矿山 */
  box-shadow: 0 0 20px rgba(255, 165, 0, 0.5);
  border: 2px solid white;
}

.robot-icon {
  background: linear-gradient(135deg, #a29bfe, #6c5ce7);
  box-shadow: 0 0 20px rgba(162, 155, 254, 0.5);
  border: 2px solid white;
}

.file-icon {
  background: linear-gradient(135deg, #a8e6cf, #56c596);
  box-shadow: 0 0 20px rgba(86, 197, 150, 0.5);
  border: 2px solid white;
}

.data-icon {
  background: linear-gradient(135deg, #74b9ff, #0984e3);
  box-shadow: 0 0 20px rgba(9, 132, 227, 0.5);
  border: 2px solid white;
}

.app-title {
  font-family: var(--font-family-title);
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
}

.app-info {
  margin-bottom: 25px;
  width: 100%;
  flex: 1;
}

.app-desc {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 20px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  opacity: 0.95;
  transform: translateY(5px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(22, 119, 255, 0.05);
  border-radius: 8px;
  border-left: 3px solid var(--primary-color);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(22, 119, 255, 0.1);
  transform: translateX(5px);
}

.feature-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.feature-text {
  font-size: 0.9rem;
  color: var(--text-primary);
  font-weight: 500;
}

/* 移除旧的 .app-button 样式，使用 AppButton 组件代替 */

/* 背景圆圈动画 */
.cyber-circles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
  opacity: 0.3;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
  background: linear-gradient(
    135deg,
    rgba(147, 210, 184, 0.3),
    rgba(147, 210, 184, 0.1)
  );
  animation: float 15s infinite alternate;
}

.circle-2 {
  width: 500px;
  height: 500px;
  bottom: -200px;
  left: -200px;
  background: linear-gradient(
    135deg,
    rgba(147, 210, 184, 0.2),
    rgba(147, 210, 184, 0.05)
  );
  animation: float 20s infinite alternate-reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 40%;
  right: 15%;
  background: linear-gradient(
    135deg,
    rgba(22, 119, 255, 0.1),
    rgba(147, 210, 184, 0.1)
  );
  animation: float 12s infinite alternate;
}

/* 动画效果 */
@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  100% {
    transform: translate(50px, 50px) rotate(10deg);
  }
}

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
    gap: 30px;
    padding: 0 30px;
  }
}

@media (max-width: 768px) {
  .glitch-title {
    font-size: 2.5rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .apps-container {
    gap: 25px;
    margin: 40px auto;
    padding: 0 20px;
  }

  .app-card {
    padding: 25px;
    min-height: auto;
  }

  .card-header {
    gap: 15px;
  }

  .app-icon {
    font-size: 2rem;
    width: 60px;
    height: 60px;
  }

  .app-title {
    font-size: 1.3rem;
  }

  .feature-list {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 480px) {
  .header {
    padding: 50px 15px 40px;
  }

  .glitch-title {
    font-size: 2rem;
  }

  .subtitle {
    font-size: 0.9rem;
    letter-spacing: 2px;
  }

  .apps-container {
    margin: 30px auto;
    padding: 0 15px;
  }

  .app-card {
    padding: 20px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .app-icon {
    font-size: 2rem;
    width: 55px;
    height: 55px;
  }

  .app-title {
    font-size: 1.2rem;
  }

  .app-desc {
    font-size: 0.85rem;
  }

  .feature-item {
    padding: 6px 10px;
  }

  .feature-text {
    font-size: 0.85rem;
  }

  .app-button {
    padding: 12px 24px;
  }

  .circle-1,
  .circle-2,
  .circle-3 {
    opacity: 0.1;
  }
}
</style>
