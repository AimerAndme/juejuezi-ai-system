<template>
  <div class="home-container">
    <div class="header">
      <div class="glitch-wrapper">
        <h1 class="glitch-title">鱼皮AI超级智能体</h1>
      </div>
      <p class="subtitle">/ 探索AI的无限可能 /</p>
      <div class="cyber-line"></div>
    </div>

    <div class="apps-container">
      <div class="app-card" @click="navigateTo('/love-master')">
        <div class="card-glow"></div>
        <div class="app-icon love-icon">❤️</div>
        <div class="app-info">
          <div class="app-title">AI恋爱大师</div>
          <div class="app-desc">智能情感顾问，帮你解答恋爱烦恼</div>
        </div>
        <div class="app-button">
          <span class="btn-text">立即体验</span>
          <span class="btn-icon">→</span>
        </div>
      </div>

      <div class="app-card" @click="navigateTo('/mine-agent')">
        <div class="card-glow"></div>
        <div class="app-icon mine-icon">⛏️</div>
        <div class="app-info">
          <div class="app-title">AI矿山专家</div>
          <div class="app-desc">专业的矿山技术顾问，提供矿业相关问题解答</div>
        </div>
        <div class="app-button">
          <span class="btn-text">立即体验</span>
          <span class="btn-icon">→</span>
        </div>
      </div>

      <div class="app-card" @click="navigateTo('/super-agent')">
        <div class="card-glow"></div>
        <div class="app-icon robot-icon">🤖</div>
        <div class="app-info">
          <div class="app-title">AI超级智能体</div>
          <div class="app-desc">全能型AI助手，解决各类专业问题</div>
        </div>
        <div class="app-button">
          <span class="btn-text">立即体验</span>
          <span class="btn-icon">→</span>
        </div>
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

// 设置页面标题和元数据
useHead({
  title: '鱼皮AI超级智能体应用平台 - 首页',
  meta: [
    {
      name: 'description',
      content:
        '鱼皮AI超级智能体应用平台提供AI恋爱大师、AI矿山专家和AI超级智能体服务，满足您的各种AI对话需求',
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
  router.push(path)
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
  background: linear-gradient(135deg, var(--light-blue), var(--sky-blue));
  position: relative;
  overflow: hidden;
  padding-bottom: 40px;
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
  font-family: 'Orbitron', sans-serif;
  font-size: 3.2rem;
  font-weight: 700;
  color: var(--light-blue);
  text-shadow: 0 0 5px rgba(173, 216, 230, 0.7),
    0 0 10px rgba(173, 216, 230, 0.5), 0 0 20px rgba(173, 216, 230, 0.3);
  letter-spacing: 2px;
  position: relative;
  animation: blueGlow 3s infinite;
  background: linear-gradient(45deg, var(--light-blue), var(--sky-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 5px rgba(173, 216, 230, 0.5));
}

.glitch-title::before,
.glitch-title::after {
  content: '鱼皮AI超级智能体';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.8;
}

.glitch-title::before {
  color: var(--light-purple);
  z-index: -1;
  animation: glitch-anim 2s infinite;
}

.glitch-title::after {
  color: var(--light-blue);
  z-index: -2;
  animation: glitch-anim-2 3s infinite;
}

.subtitle {
  font-family: 'Orbitron', sans-serif;
  font-size: 1.2rem;
  color: #1a2a3a; /* 加深副标题颜色，提高可读性 */
  max-width: 600px;
  margin: 0 auto 20px;
  letter-spacing: 3px;
  text-transform: uppercase;
  font-weight: 500;
  /* 添加文本阴影以提高在浅色背景上的可读性 */
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.7);
}

.cyber-line {
  height: 2px;
  width: 80%;
  max-width: 600px;
  margin: 0 auto;
  background: linear-gradient(
    90deg,
    transparent,
    var(--light-blue),
    transparent
  );
  position: relative;
  border-top: 1px dashed var(--light-blue);
  border-bottom: 1px dashed var(--sky-blue);
}

.cyber-line::before,
.cyber-line::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background-color: var(--light-blue);
  border-radius: 50%;
  transform: translateY(-50%);
  box-shadow: 0 0 10px 2px var(--light-blue);
  border: 2px solid white;
}

.cyber-line::before {
  left: 20%;
}

.cyber-line::after {
  right: 20%;
}

.apps-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 50px;
  max-width: 1200px;
  margin: 60px auto;
  padding: 0 20px;
  flex: 1;
  position: relative;
  z-index: 2;
}

.app-card {
  width: 340px;
  background: linear-gradient(
    135deg,
    rgba(221, 238, 255, 0.8),
    rgba(173, 216, 230, 0.8)
  );
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(173, 216, 230, 0.3),
    inset 0 0 0 1px rgba(255, 255, 255, 0.3);
  padding: 30px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
  border: 2px solid var(--light-gray-blue);
  /* 轻微的浅灰色阴影 */
  box-shadow: 0 8px 32px rgba(221, 238, 255, 0.4),
    inset 0 0 0 1px rgba(255, 255, 255, 0.3);
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle at center,
    rgba(173, 216, 230, 0.2) 0%,
    transparent 70%
  );
  opacity: 0;
  transition: opacity 0.5s;
  pointer-events: none;
}

.app-card:hover {
  transform: translateY(-15px) scale(1.03);
  box-shadow: 0 15px 50px rgba(173, 216, 230, 0.4),
    inset 0 0 0 1px rgba(255, 255, 255, 0.5);
  /* 淡蓝色微光闪烁 */
  animation: blueGlowFlash 1s ease-in-out;
}

.app-card:hover .card-glow {
  opacity: 1;
}

.app-icon {
  font-size: 4rem;
  margin-bottom: 25px;
  width: 90px;
  height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px; /* 12-16px大圆角 */
  position: relative;
  z-index: 1;
  /* 轻微的浅灰色阴影 */
  box-shadow: 0 4px 8px rgba(221, 238, 255, 0.4);
}

.love-icon {
  background: linear-gradient(135deg, var(--light-blue), var(--sky-blue));
  box-shadow: 0 0 20px rgba(173, 216, 230, 0.5);
  border: 2px solid white;
}

.mine-icon {
  background: linear-gradient(135deg, var(--light-orange), var(--orange)); /* 使用橙色系表示矿山 */
  box-shadow: 0 0 20px rgba(255, 165, 0, 0.5);
  border: 2px solid white;
}

.robot-icon {
  background: linear-gradient(135deg, var(--light-purple), var(--light-blue));
  box-shadow: 0 0 20px rgba(224, 187, 228, 0.5);
  border: 2px solid white;
}

.app-info {
  text-align: center;
  margin-bottom: 30px;
  width: 100%;
}

.app-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 1.6rem;
  font-weight: bold;
  color: #3a5a7a;
  margin-bottom: 12px;
  text-shadow: 0 0 10px rgba(173, 216, 230, 0.3);
  background: linear-gradient(45deg, var(--light-blue), #3a5a7a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.app-desc {
  font-size: 1rem;
  color: #1a2a3a; /* 加深描述文字颜色，提高可读性 */
  line-height: 1.6;
  font-weight: 500;
  /* 添加文本阴影以提高在浅色背景上的可读性 */
  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.5);
}

.app-button {
  background: linear-gradient(90deg, var(--light-blue), var(--sky-blue));
  color: white;
  padding: 12px 28px;
  border-radius: 30px;
  font-weight: 500;
  transition: all 0.3s;
  margin-top: auto;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
  /* 轻微的浅灰色阴影 */
  box-shadow: 0 4px 8px rgba(221, 238, 255, 0.4);
}

.app-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transition: left 0.7s;
}

.app-button:hover {
  box-shadow: 0 0 15px rgba(173, 216, 230, 0.7);
  transform: scale(1.05);
  /* 点击反馈为软弹动效 */
  animation: softBounce 0.3s ease-in-out;
}

.app-button:hover::before {
  left: 100%;
}

.btn-text {
  margin-right: 8px;
  letter-spacing: 1px;
}

.btn-icon {
  font-size: 1.2rem;
  transition: transform 0.3s;
}

.app-button:hover .btn-icon {
  transform: translateX(4px);
}

/* 背景圆圈动画 */
.cyber-circles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
  background: linear-gradient(135deg, var(--light-blue), var(--sky-blue));
  animation: float 15s infinite alternate;
}

.circle-2 {
  width: 500px;
  height: 500px;
  bottom: -200px;
  left: -200px;
  background: linear-gradient(135deg, var(--sky-blue), var(--light-purple));
  animation: float 20s infinite alternate-reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 40%;
  right: 15%;
  background: linear-gradient(135deg, var(--light-purple), var(--light-blue));
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

@keyframes blueGlow {
  0% {
    text-shadow: 0 0 5px rgba(173, 216, 230, 0.7),
      0 0 10px rgba(173, 216, 230, 0.5);
  }
  50% {
    text-shadow: 0 0 5px rgba(173, 216, 230, 0.7),
      0 0 10px rgba(173, 216, 230, 0.5), 0 0 20px rgba(173, 216, 230, 0.3);
  }
  100% {
    text-shadow: 0 0 5px rgba(173, 216, 230, 0.7),
      0 0 10px rgba(173, 216, 230, 0.5);
  }
}

@keyframes blueGlowFlash {
  0% {
    box-shadow: 0 15px 50px rgba(173, 216, 230, 0.4),
      inset 0 0 0 1px rgba(255, 255, 255, 0.5);
  }
  50% {
    box-shadow: 0 15px 50px rgba(173, 216, 230, 0.7),
      inset 0 0 0 1px rgba(255, 255, 255, 0.8);
  }
  100% {
    box-shadow: 0 15px 50px rgba(173, 216, 230, 0.4),
      inset 0 0 0 1px rgba(255, 255, 255, 0.5);
  }
}

@keyframes softBounce {
  0% {
    transform: scale(1.05);
  }
  50% {
    transform: scale(0.95);
  }
  100% {
    transform: scale(1.05);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .glitch-title {
    font-size: 2.5rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .apps-container {
    gap: 30px;
    margin: 40px auto;
  }

  .app-card {
    width: 100%;
    max-width: 420px;
    padding: 25px;
  }

  .app-icon {
    font-size: 3.5rem;
    width: 80px;
    height: 80px;
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

  .app-icon {
    font-size: 3rem;
    margin-bottom: 20px;
    width: 70px;
    height: 70px;
  }

  .app-title {
    font-size: 1.4rem;
  }

  .app-desc {
    font-size: 0.9rem;
  }

  .circle-1,
  .circle-2,
  .circle-3 {
    opacity: 0.1;
  }
}
</style>
