<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" >
            <path d="M11.645 20.91l-.007-.003-.022-.012a15.247 15.247 0 01-.383-.218 25.18 25.18 0 01-4.244-3.17C4.688 15.36 2.25 12.174 2.25 8.25 2.25 5.322 4.714 3 7.688 3A5.5 5.5 0 0112 5.052 5.5 5.5 0 0116.313 3c2.973 0 5.437 2.322 5.437 5.25 0 3.925-2.438 7.111-4.739 9.256a25.175 25.175 0 01-4.244 3.17 15.247 15.247 0 01-.383.219l-.022.012-.007.004-.003.001a.752.752 0 01-.704 0l-.003-.001z" />
          </svg>
        </div>
        <h1>智能医疗系统</h1>
        <p class="subtitle">Smart Healthcare System</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="input-group">
          <label for="username">用户名</label>
          <div class="input-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="input-icon">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
            </svg>
            <input
                id="username"
                type="text"
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :class="{ 'error': errors.username }"
                autocomplete="username"
            />
          </div>
          <span v-if="errors.username" class="error-msg">{{ errors.username }}</span>
        </div>

        <div class="input-group">
          <label for="password">密码</label>
          <div class="input-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="input-icon">
              <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
            </svg>
            <input
                id="password"
                type="password"
                v-model="loginForm.password"
                placeholder="请输入密码"
                :class="{ 'error': errors.password }"
                autocomplete="current-password"
            />
          </div>
          <span v-if="errors.password" class="error-msg">{{ errors.password }}</span>
        </div>

        <div class="options-row">
          <div></div>
          <a href="#" class="forgot-password" @click.prevent="goToForgotPassword">忘记密码?</a>
        </div>

        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="!loading">登录</span>
          <span v-else class="loading-spinner">
            <svg class="spinner-icon" viewBox="0 0 50 50">
              <circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="5"></circle>
            </svg>
            登录中...
          </span>
        </button>
      </form>

      <div class="register-section">
        <p>还没有账户？ <a href="#" class="register-link" @click.prevent="goToRegister">立即注册</a></p>
      </div>
    </div>

    <div class="footer">
      <p>© 2025 智能医疗系统 | 版权所有</p>
    </div>
  </div>
</template>


<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const loginForm = ref({ username: '', password: '' })
const loading = ref(false)
const errors = reactive({
  username: '',
  password: ''
})

// 清除错误信息
const clearErrors = () => {
  errors.username = ''
  errors.password = ''
}

const handleLogin = async () => {
  // 清除之前的错误
  clearErrors()

  // 验证输入
  let isValid = true
  if (!loginForm.value.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  }
  if (!loginForm.value.password) {
    errors.password = '请输入密码'
    isValid = false
  }

  if (!isValid) return

  loading.value = true

  try {
    const url = 'http://localhost:8080/api/login'
    const response = await axios.post(url, loginForm.value)

    if (response.data.code === 200) {
      const { token, userInfo } = response.data.data || {}

      if (!token || !userInfo) {
        alert("登录失败：返回数据格式异常")
        return
      }

      localStorage.setItem('token', token)

      let user = userInfo
      user.role = user.role ? user.role.toUpperCase() : 'NULL'

      sessionStorage.setItem('token', token);
      sessionStorage.setItem('user', JSON.stringify(user));

      let targetPath
      switch(user.role) {
        case 'ADMIN': targetPath = '/admin/stats'; break
        case 'DOCTOR': targetPath = '/doctor/patients'; break
        case 'PATIENT': targetPath = '/patient/index'; break
        default: targetPath = '/login'
      }

      await router.push(targetPath)
    } else {
      alert(response.data.msg || "登录失败")
    }
  } catch (error) {
    if (error.code === 'ERR_NETWORK') {
      alert("网络错误：无法连接后端。请检查 Back-End 是否启动。")
    } else {
      console.error(error)
      alert("请求发生异常：" + (error.response?.data?.msg || error.message))
    }
  } finally {
    loading.value = false
  }
}
// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

// 跳转到忘记密码页面
const goToForgotPassword = () => {
  router.push('/forgot-password')
}


</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  padding: 20px;
  position: relative;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 420px;
  position: relative;
  border: 1px solid #eef2f7;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.12);
}

.login-header {
  text-align: center;
  margin-bottom: 35px;
}

.login-header .logo {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  box-shadow: 0 5px 15px rgba(0, 123, 255, 0.3);
}

.login-header .logo svg {
  width: 32px;
  height: 32px;
  color: white;
}

.login-header h1 {
  color: #2d3748;
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
}

.login-header .subtitle {
  color: #718096;
  margin: 0;
  font-size: 14px;
}

.input-group {
  margin-bottom: 25px;
  position: relative;
  width: 100%;
}


.input-group label {
  display: block;
  margin-bottom: 8px;
  color: #4a5568;
  font-weight: 500;
  font-size: 14px;
}

.input-wrapper {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: #a0aec0;
  z-index: 1;
}

input {
  width: 100%;
  padding: 14px 15px 14px 45px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  transition: all 0.3s ease;
  background-color: #f8fafc;
  box-sizing: border-box; /* 确保宽度包含内边距和边框 */
  max-width: 100%; /* 防止超出容器 */
}

input:focus {
  outline: none;
  border-color: #007bff;
  background-color: white;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

input.error {
  border-color: #e53e3e;
  background-color: #fff5f5;
}

.error-msg {
  color: #e53e3e;
  font-size: 13px;
  margin-top: 6px;
  display: block;
}

.options-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  font-size: 14px;
}

.checkbox-container input {
  opacity: 0;
  width: 0;
  height: 0;
}

.forgot-password {
  color: #007bff;
  text-decoration: none;
  transition: color 0.3s;
  font-size: 14px;
}

.forgot-password:hover {
  color: #0056b3;
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 123, 255, 0.2);
}

.login-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #0069d9 0%, #004085 100%);
  transform: translateY(-2px);
  box-shadow: 0 7px 14px rgba(0, 123, 255, 0.3);
}

.login-btn:disabled {
  background: #a0aec0;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner-icon {
  width: 18px;
  height: 18px;
  animation: rotate 1.4s linear infinite;
  margin-right: 8px;
}

.spinner-icon .path {
  stroke: white;
  stroke-linecap: round;
  animation: dash 1.4s ease-in-out infinite;
}

@keyframes rotate {
  100% { transform: rotate(360deg); }
}

@keyframes dash {
  0% { stroke-dasharray: 1, 150; stroke-dashoffset: 0; }
  50% { stroke-dasharray: 90, 150; stroke-dashoffset: -35; }
  100% { stroke-dasharray: 90, 150; stroke-dashoffset: -124; }
}

.divider span {
  position: relative;
  z-index: 2;
  background: white;
  padding: 0 15px;
  font-size: 14px;
}

.social-btn svg {
  width: 18px;
  height: 18px;
}

.footer {
  margin-top: 30px;
  width: 100%;
  text-align: center;
  color: #718096;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-box {
    padding: 30px 25px;
    margin: 0 10px;
  }

  .options-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .forgot-password {
    align-self: flex-end;
  }
}

.register-section {
  text-align: center;
  margin-top: 20px;
}

.register-section p {
  color: #718096;
  margin: 0;
  font-size: 14px;
}

.register-link {
  color: #007bff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.register-link:hover {
  color: #0056b3;
  text-decoration: underline;
}

/* 调整选项行的样式 */
.options-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  font-size: 14px;
}

.checkbox-container input {
  opacity: 0;
  width: 0;
  height: 0;
}

.forgot-password {
  color: #007bff;
  text-decoration: none;
  transition: color 0.3s;
  font-size: 14px;
}

.forgot-password:hover {
  color: #0056b3;
  text-decoration: underline;
}

</style>
