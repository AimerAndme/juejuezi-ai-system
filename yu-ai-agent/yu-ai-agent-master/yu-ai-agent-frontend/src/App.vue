<script setup>
import HelloWorld from './components/HelloWorld.vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from './api/index.js'

const router = useRouter()
const userId = ref('')
const account = ref('')
const showUserMenu = ref(false)

onMounted(() => {
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
    router.push('/login')
  } catch (err) {
    console.error('登出失败:', err)
  }
}
</script>

<template>
  <div class="app-container">
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
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Helvetica,
    Arial, sans-serif;
  font-size: 16px;
  color: #333;
  background-color: #f0f2f5;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 30px;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left .logo {
  font-size: 20px;
  font-weight: bold;
  color: white;
  text-decoration: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 40px;
}

.nav-menu {
  display: flex;
  gap: 20px;
}

.nav-item {
  color: white;
  text-decoration: none;
  font-size: 14px;
  transition: opacity 0.3s;
}

.nav-item:hover {
  opacity: 0.8;
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
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.3);
}

.user-name {
  font-size: 14px;
}

.dropdown-icon {
  font-size: 12px;
}

.user-menu {
  position: absolute;
  top: 60px;
  right: 30px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 150px;
  z-index: 101;
}

.menu-item {
  padding: 10px 15px;
  color: #333;
  font-size: 14px;
  border-bottom: 1px solid #eee;
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
  padding: 6px 16px;
  border-radius: 4px;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s;
}

.btn-login {
  color: white;
  border: 1px solid white;
}

.btn-login:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.btn-register {
  background-color: white;
  color: #667eea;
  font-weight: 500;
}

.btn-register:hover {
  opacity: 0.9;
}

.app-content {
  flex: 1;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 15px;
  }

  .header-right {
    gap: 20px;
  }

  .nav-menu {
    gap: 10px;
  }

  .nav-item {
    font-size: 12px;
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
    gap: 10px;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}
</style>
