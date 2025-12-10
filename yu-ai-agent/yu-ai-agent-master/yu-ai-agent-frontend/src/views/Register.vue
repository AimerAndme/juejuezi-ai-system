<template>
  <div class="register-container">
    <div class="register-box">
      <h1>用户注册</h1>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="account">账号：</label>
          <input
            id="account"
            v-model="form.account"
            type="text"
            placeholder="请输入账号（作为登录账号）"
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

        <div class="form-group">
          <label for="confirmPassword">确认密码：</label>
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            required
          />
        </div>

        <div class="form-group">
          <label for="userName">用户名：</label>
          <input
            id="userName"
            v-model="form.userName"
            type="text"
            placeholder="请输入用户名"
            required
          />
        </div>

        <div class="form-group">
          <label for="userRole">角色选择：</label>
          <select id="userRole" v-model="form.userRole" required>
            <option value="">请选择角色</option>
            <option value="miner">矿工</option>
            <option value="technician">技术员</option>
            <option value="manager">经理</option>
          </select>
        </div>

        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="loading" class="loading-message">注册中...</div>

        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="register-footer">
        <p>已有账号？<router-link to="/login">点击登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/index.js'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const form = ref({
      account: '',
      password: '',
      confirmPassword: '',
      userName: '',
      userRole: '',
    })
    const error = ref('')
    const loading = ref(false)

    const handleRegister = async () => {
      // 校验
      if (!form.value.account) {
        error.value = '账号不能为空'
        return
      }
      if (!form.value.password) {
        error.value = '密码不能为空'
        return
      }
      if (form.value.password !== form.value.confirmPassword) {
        error.value = '两次输入的密码不一致'
        return
      }
      if (!form.value.userName) {
        error.value = '用户名不能为空'
        return
      }
      if (!form.value.userRole) {
        error.value = '请选择角色'
        return
      }

      loading.value = true
      error.value = ''

      try {
        const response = await register(
          form.value.account,
          form.value.password,
          form.value.userName,
          form.value.userRole
        )

        if (response.data.code === 200) {
          // 注册成功，跳转到登录页
          alert('注册成功，请登录')
          router.push('/login')
        } else {
          error.value = response.data.msg || '注册失败'
        }
      } catch (err) {
        error.value = '注册失败，请检查服务器连接'
        console.error('注册错误:', err)
      } finally {
        loading.value = false
      }
    }

    return {
      form,
      error,
      loading,
      handleRegister,
    }
  },
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-box {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
}

.register-box h1 {
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

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus {
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

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.register-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
