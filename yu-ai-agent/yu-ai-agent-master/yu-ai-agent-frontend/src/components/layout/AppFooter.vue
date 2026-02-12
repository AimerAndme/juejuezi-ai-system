<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-section">
        <div class="footer-logo">
          <h3>AI超级智能体应用平台</h3>
        </div>
        <div class="footer-links">
          <a href="#">《用户协议》</a>
          <a href="#">《隐私政策》</a>
        </div>
      </div>

      <div class="footer-section">
        <h4>友情链接</h4>
        <div class="footer-links">
          <a href="#" target="_blank">雷克萨斯</a>
          <a href="#" target="_blank">宝马</a>
          <a href="#" target="_blank">丰田</a>
        </div>
      </div>

      <div class="footer-section">
        <h4>联系我们</h4>
        <div class="footer-links">
          <a href="#">商务合作</a>
          <a href="#">站长：吕七七</a>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <p>© {{ currentYear }} AI超级智能体应用平台 - 让AI为你服务</p>
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
  background: var(--bg-card);
  padding: 40px 0 20px;
  color: var(--text-primary);
  border-top: 1px solid var(--card-border);
  width: 100%;
  margin: 0;
  position: relative;
  z-index: 2;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  padding: 20px;
  border-top: 1px solid var(--card-border);
  border-bottom: 1px solid var(--card-border);
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
  color: var(--logo-color);
  font-family: var(--font-family-title);
  font-weight: 600;
}

.footer-section h4 {
  font-size: 1rem;
  margin-bottom: 15px;
  color: var(--text-primary);
  font-family: var(--font-family-title);
  font-weight: 600;
}

.footer-links {
  display: flex;
  flex-direction: column;
}

.footer-links a {
  margin-bottom: 10px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 5px 10px;
  border-radius: 8px;
  border: 1px solid transparent;
}

.footer-links a:hover {
  color: var(--primary-color);
  transform: scale(1.05);
  background-color: rgba(22, 119, 255, 0.1);
  border-color: var(--primary-color);
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
  background: var(--bg-card);
  margin: 0 auto 10px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--card-border);
  transition: all 0.3s ease;
}

.qrcode-placeholder:after {
  content: '🤖';
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  margin-top: 20px;
  border-top: 1px solid var(--card-border);
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-family: var(--font-family-base);
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
