<script setup>
import HelloWorld from './components/HelloWorld.vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from './api/index.js'

const router = useRouter()
const userId = ref('')
const account = ref('')
const showUserMenu = ref(false)
const isDarkMode = ref(false)
const currentLanguage = ref('zh-CN')

// 主题切换
const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  document.documentElement.classList.toggle('dark-mode', isDarkMode.value)
  localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light')
}

// 初始化主题
const initTheme = () => {
  const savedTheme = localStorage.getItem('theme')
  isDarkMode.value = savedTheme === 'dark'
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark-mode')
  }
}

onMounted(() => {
  initTheme()
  userId.value = localStorage.getItem('userId') || ''
  account.value = localStorage.getItem('account') || ''
})

const handleLogout = async () => {
  if (!userId.value) {
    router.push('/login')
    return
  }

  try {
    await logout(userId.value)
    localStorage.removeItem('userId')
    localStorage.removeItem('account')
    localStorage.removeItem('userRole')
    router.push('/login')
  } catch (err) {
    console.error('登出失败:', err)
  }
}
</script>

<template>
  <div class="app-container" :class="{ 'dark-mode': isDarkMode }">
    <header class="app-header">
      <div class="header-left">
        <router-link to="/" class="logo">AI智能体平台</router-link>
      </div>
      <div class="header-right">
        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/love-master" class="nav-item">恋爱大师</router-link>
          <router-link to="/mine-agent" class="nav-item">矿山专家</router-link>
          <router-link to="/super-agent" class="nav-item"
            >超级智能体</router-link
          >
        </nav>

        <select class="language-select" v-model="currentLanguage">
          <option value="zh-CN">简体中文</option>
          <option value="en-US">English</option>
        </select>

        <a
          href="https://github.com/alibaba/spring-ai-alibaba"
          target="_blank"
          class="github-link"
          title="GitHub"
        >
          <svg width="20" height="20" viewBox="0 0 16 16" fill="currentColor">
            <path
              d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"
            ></path>
          </svg>
        </a>

        <button
          class="theme-toggle"
          @click="toggleTheme"
          :title="isDarkMode ? '切换到浅色模式' : '切换到深色模式'"
        >
          {{ isDarkMode ? '🌞' : '🌙' }}
        </button>

        <div class="user-section">
          <template v-if="userId">
            <div class="user-info" @click="showUserMenu = !showUserMenu">
              <span class="user-name">{{ account || '用户' }}</span>
              <span class="dropdown-icon">▼</span>
            </div>
            <div v-if="showUserMenu" class="user-menu">
              <div class="menu-item">ID: {{ userId.substring(0, 8) }}...</div>
              <button class="menu-item logout-btn" @click="handleLogout">
                登出
              </button>
            </div>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-login">登录</router-link>
            <router-link to="/register" class="btn-register">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <main class="app-content">
      <router-view />
    </main>
  </div>
</template>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html,
body {
  font-family: 'Inter', 'PingFang SC', -apple-system, BlinkMacSystemFont,
    sans-serif;
  font-size: 16px;
  color: var(--text-primary);
  background-color: var(--bg-light);
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.dark-mode {
  color: var(--text-primary);
  background-color: var(--bg-light);
}

#app {
  width: 100%;
  height: 100%;
}

a {
  text-decoration: none;
  color: inherit;
}

button {
  cursor: pointer;
}

.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.app-header {
  background: var(--bg-white);
  color: var(--text-primary);
  padding: 0 30px;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-light);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid var(--border-light);
  transition: all 0.3s ease;
  pointer-events: auto;
}

.dark-mode .app-header {
  background: var(--bg-white);
  border-bottom-color: var(--border-light);
}

.header-left .logo {
  font-size: 20px;
  font-weight: 600;
  color: var(--logo-color);
  text-decoration: none;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  cursor: pointer;
  pointer-events: auto;
}

.header-left .logo:hover {
  transform: scale(1.02);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-menu {
  display: flex;
  gap: 20px;
}

.nav-item {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  pointer-events: auto;
}

.nav-item:hover {
  color: var(--primary-color);
  background-color: rgba(22, 119, 255, 0.1);
  transform: scale(1.02);
}

.nav-item.router-link-active {
  color: var(--primary-color);
  font-weight: 600;
}

.language-select {
  padding: 6px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-white);
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
}

.language-select:hover {
  border-color: var(--primary-color);
}

.github-link {
  display: flex;
  align-items: center;
  color: var(--text-secondary);
  transition: all 0.3s ease;
  padding: 6px;
  border-radius: 6px;
}

.github-link:hover {
  color: var(--primary-color);
  transform: scale(1.05);
}

.theme-toggle {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-white);
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-toggle:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-medium);
  border-color: var(--primary-color);
}

.user-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  background: var(--bg-light);
  color: var(--text-primary);
  border-radius: 6px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.user-info:hover {
  background: rgba(22, 119, 255, 0.1);
  border-color: var(--primary-color);
  transform: scale(1.02);
}

.user-name {
  font-size: 14px;
}

.dropdown-icon {
  font-size: 12px;
}

.user-menu {
  position: absolute;
  top: 64px;
  right: 30px;
  background: var(--bg-white);
  border-radius: 8px;
  box-shadow: var(--shadow-heavy);
  min-width: 150px;
  z-index: 101;
  border: 1px solid var(--border-light);
  overflow: hidden;
  animation: fadeIn 0.2s ease-in;
}

.menu-item {
  padding: 10px 15px;
  color: var(--text-primary);
  font-size: 14px;
  border-bottom: 1px solid var(--border-light);
  transition: background 0.3s ease;
}

.menu-item:last-child {
  border-bottom: none;
}

.logout-btn {
  width: 100%;
  background: none;
  border: none;
  text-align: left;
  color: #d32f2f;
  cursor: pointer;
  transition: background 0.3s;
}

.logout-btn:hover {
  background-color: #ffebee;
}

.btn-login,
.btn-register {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 500;
  cursor: pointer;
  pointer-events: auto;
}

.btn-login {
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  background: transparent;
}

.btn-login:hover {
  background-color: var(--bg-light);
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: scale(1.05);
}

.btn-register {
  background-color: var(--primary-color);
  color: white;
  border: 1px solid var(--primary-color);
}

.btn-register:hover {
  background-color: var(--primary-color-hover);
  transform: scale(1.05);
  box-shadow: var(--shadow-medium);
}

.app-content {
  flex: 1;
  overflow-y: auto;
  position: relative;
  z-index: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 15px;
  }

  .header-right {
    gap: 12px;
  }

  .nav-menu {
    gap: 10px;
  }

  .nav-item {
    font-size: 12px;
    padding: 4px 8px;
  }

  .language-select,
  .github-link,
  .theme-toggle {
    display: none;
  }

  .user-menu {
    right: 15px;
  }
}

@media (max-width: 480px) {
  html,
  body {
    font-size: 14px;
  }

  .app-header {
    padding: 0 10px;
  }

  .nav-menu {
    display: none;
  }

  .header-right {
    gap: 8px;
  }

  .user-section {
    gap: 8px;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: var(--bg-light);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
  transition: background 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}
</style>
