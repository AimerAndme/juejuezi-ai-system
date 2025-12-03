<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-section">
        <div class="footer-logo">
          <h3>鱼皮AI超级智能体应用平台</h3>
        </div>
        <div class="footer-links">
          <a href="#">《用户协议》</a>
          <a href="#">《隐私政策》</a>
        </div>
      </div>

      <div class="footer-section">
        <h4>友情链接</h4>
        <div class="footer-links">
          <a href="https://www.mianshiya.com" target="_blank">面试鸭</a>
          <a href="#" target="_blank">编程导航</a>
          <a href="#" target="_blank">代码小抄</a>
        </div>
      </div>

      <div class="footer-section">
        <h4>联系我们</h4>
        <div class="footer-links">
          <a href="#">商务合作</a>
          <a href="#">站长：鱼皮</a>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <p>© {{ currentYear }} 鱼皮AI超级智能体应用平台 - 让AI为你服务</p>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'

// 计算当前年份
const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.app-footer {
  background: linear-gradient(
    135deg,
    rgba(221, 238, 255, 0.8),
    rgba(173, 216, 230, 0.8)
  );
  padding: 40px 0 20px;
  color: #3a5a7a;
  border-top: 2px dashed var(--light-gray-blue);
  width: 100%;
  margin-top: auto;
  position: relative;
  z-index: 2;
  backdrop-filter: blur(5px);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  padding: 0 20px;
  /* 模块间用浅蓝色虚线分隔 */
  border-top: 1px dashed var(--light-gray-blue);
  border-bottom: 1px dashed var(--light-gray-blue);
  padding: 20px;
}

.footer-section {
  flex: 1;
  min-width: 200px;
  margin-bottom: 30px;
  padding: 0 15px;
}

.footer-logo h3 {
  font-size: 1.25rem;
  margin-bottom: 15px;
  color: #3a5a7a;
  background: linear-gradient(45deg, var(--light-blue), #3a5a7a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-family: 'Orbitron', sans-serif;
}

.footer-section h4 {
  font-size: 1rem;
  margin-bottom: 15px;
  color: #3a5a7a;
  background: linear-gradient(45deg, var(--light-blue), #3a5a7a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-family: 'Orbitron', sans-serif;
}

.footer-links {
  display: flex;
  flex-direction: column;
}

.footer-links a {
  margin-bottom: 10px;
  color: #3a5a7a;
  text-decoration: none;
  transition: all 0.3s;
  padding: 5px 10px;
  border-radius: 12px;
  /* 轻微的浅蓝色阴影 */
  box-shadow: 0 2px 4px rgba(221, 238, 255, 0.4);
}

.footer-links a:hover {
  color: var(--light-blue);
  transform: scale(1.03);
  /* 淡蓝色微光闪烁 */
  box-shadow: 0 0 8px rgba(173, 216, 230, 0.7);
}

.qrcode {
  display: flex;
  align-items: center;
}

.qrcode-container {
  text-align: center;
}

.qrcode-placeholder {
  width: 90px;
  height: 90px;
  background: linear-gradient(135deg, var(--light-bg), var(--light-blue));
  margin: 0 auto 10px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  /* 轻微的浅蓝色阴影 */
  box-shadow: 0 4px 8px rgba(221, 238, 255, 0.4);
  border: 2px solid var(--light-gray-blue);
}

.qrcode-placeholder:after {
  content: '🤖';
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  margin-top: 20px;
  border-top: 1px dashed var(--light-gray-blue);
  color: #3a5a7a;
  font-size: 0.9rem;
  font-family: 'Orbitron', sans-serif;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
  }

  .footer-section {
    width: 100%;
    margin-bottom: 20px;
    padding: 0;
  }
}

@media (max-width: 480px) {
  .app-footer {
    padding: 30px 0 15px;
  }

  .footer-section h4 {
    font-size: 0.95rem;
  }

  .footer-links a {
    font-size: 0.9rem;
  }

  .qrcode-placeholder {
    width: 80px;
    height: 80px;
  }
}
</style>
