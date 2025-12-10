<template>
  <div class="login-container">
    <div class="login-box">
      <h1>用户登录</h1>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="account">账号：</label>
          <input
            id="account"
            v-model="form.account"
            type="text"
            placeholder="请输入账号"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">密码：</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            required
          />
        </div>

        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="loading" class="loading-message">登录中...</div>

        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="login-footer">
        <p>还没有账号？<router-link to="/register">点击注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/index.js'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const form = ref({
      account: '',
      password: '',
    })
    const error = ref('')
    const loading = ref(false)

    const handleLogin = async () => {
      if (!form.value.account || !form.value.password) {
        error.value = '账号和密码不能为空'
        return
      }

      loading.value = true
      error.value = ''

      try {
        const response = await login(form.value.account, form.value.password)

        if (response.data.code === 200) {
          const userId = response.data.data
          // 保存用户ID到localStorage
          localStorage.setItem('userId', userId)
          localStorage.setItem('account', form.value.account)

          // 跳转到首页
          router.push('/')
        } else {
          error.value = response.data.msg || '登录失败'
        }
      } catch (err) {
        error.value = '登录失败，请检查服务器连接'
        console.error('登录错误:', err)
      } finally {
        loading.value = false
      }
    }

    return {
      form,
      error,
      loading,
      handleLogin,
    }
  },
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-box h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.error-message {
  color: #d32f2f;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #ffebee;
  border-radius: 4px;
  font-size: 14px;
}

.loading-message {
  color: #1976d2;
  margin-bottom: 15px;
  text-align: center;
  font-size: 14px;
}

.btn-submit {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
  margin-top: 10px;
}

.btn-submit:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.login-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>
