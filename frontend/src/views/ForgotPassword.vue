<template>
  <div class="forgot-password-container">
    <div class="forgot-password-box">
      <div class="forgot-password-header">
        <div class="logo">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="logo-icon">
            <path d="M11.645 20.91l-.007-.003-.022-.012a15.247 15.247 0 01-.383-.218 25.18 25.18 0 01-4.244-3.17C4.688 15.36 2.25 12.174 2.25 8.25 2.25 5.322 4.714 3 7.688 3A5.5 5.5 0 0112 5.052 5.5 5.5 0 0116.313 3c2.973 0 5.437 2.322 5.437 5.25 0 3.925-2.438 7.111-4.739 9.256a25.175 25.175 0 01-4.244 3.17 15.247 15.247 0 01-.383.219l-.022.012-.007.004-.003.001a.752.752 0 01-.704 0l-.003-.001z" />
          </svg>
        </div>
        <h1>忘记密码</h1>
        <p class="subtitle">输入您的邮箱以接收重置密码的链接</p>
      </div>

      <form @submit.prevent="handleForgotPassword" class="forgot-password-form">
        <div class="input-group">
          <label for="email">邮箱地址</label>
          <div class="input-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 24 24" stroke-width="1.5" class="input-icon">
              <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
            </svg>
            <input
                id="email"
                type="email"
                v-model="email"
                placeholder="请输入您的邮箱地址"
                :class="{ 'error': error }"
                autocomplete="email"
            />
          </div>
          <span v-if="error" class="error-msg">{{ error }}</span>
        </div>

        <button type="submit" class="forgot-password-btn" :disabled="loading">
          <span v-if="!loading">发送重置链接</span>
          <span v-else class="loading-spinner">
            <svg class="spinner-icon" viewBox="0 0 50 50">
              <circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="5"></circle>
            </svg>
            发送中...
          </span>
        </button>
      </form>

      <div class="back-section">
        <p><a href="#" class="back-link" @click.prevent="goToLogin">返回登录</a></p>
      </div>
    </div>

    <div class="footer">
      <p>© 2025 智能医疗系统 | 版权所有</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const email = ref('')
const loading = ref(false)
const error = ref('')

const handleForgotPassword = async () => {
  // 清除之前的错误
  error.value = ''

  // 验证输入
  if (!email.value) {
    error.value = '请输入邮箱地址'
    return
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    error.value = '请输入有效的邮箱地址'
    return
  }

  loading.value = true

  try {
    const url = 'http://localhost:8080/api/forgot-password' // 假设后端有忘记密码API
    const response = await axios.post(url, { email: email.value })

    if (response.data.code === 200) {
      alert("重置密码链接已发送到您的邮箱！")
      router.push('/login')
    } else {
      error.value = response.data.msg || "发送失败"
    }
  } catch (err) {
    if (err.code === 'ERR_NETWORK') {
      error.value = "网络错误：无法连接后端。请检查 Back-End 是否启动。"
    } else {
      console.error(err)
      error.value = err.response?.data?.msg || "请求发生异常"
    }
  } finally {
    loading.value = false
  }
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-password-container {
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

.forgot-password-box {
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

.forgot-password-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.12);
}

.forgot-password-header {
  text-align: center;
  margin-bottom: 35px;
}

.forgot-password-header .logo {
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

.forgot-password-header .logo-icon {
  width: 32px;
  height: 32px;
  color: white;
  fill: currentColor; /* 通过CSS控制fill属性 */
}

.forgot-password-header h1 {
  color: #2d3748;
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
}

.forgot-password-header .subtitle {
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
  stroke: currentColor; /* 通过CSS控制stroke颜色 */
}

input {
  width: 100%;
  padding: 14px 15px 14px 45px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  transition: all 0.3s ease;
  background-color: #f8fafc;
  box-sizing: border-box;
  max-width: 100%;
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

.forgot-password-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #ffc107 0%, #e0a800 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(255, 193, 7, 0.2);
  margin-top: 10px;
}

.forgot-password-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #e0a800 0%, #d39e00 100%);
  transform: translateY(-2px);
  box-shadow: 0 7px 14px rgba(255, 193, 7, 0.3);
}

.forgot-password-btn:disabled {
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

.back-section {
  text-align: center;
  margin-top: 20px;
}

.back-section p {
  color: #718096;
  margin: 0;
  font-size: 14px;
}

.back-link {
  color: #007bff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.back-link:hover {
  color: #0056b3;
  text-decoration: underline;
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
  .forgot-password-box {
    padding: 30px 20px;
    margin: 0 10px;
  }
}
</style>
