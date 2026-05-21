<template>
  <div class="login-container">
    <!-- 粒子网络背景 -->
    <ParticleBackground mode="network" color="#007AFF" />

    <!-- 背景装饰 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <div class="login-card-wrapper">
      <!-- 品牌区 -->
      <div class="brand-section">
        <div class="brand-icon">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="48" height="48" rx="12" fill="url(#gradient)" />
            <path d="M24 12C18.48 12 14 16.48 14 22s4.48 10 10 10 10-4.48 10-10-4.48-10-10-10zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z" fill="#fff" opacity="0.9"/>
            <path d="M24 16c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm0 9c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3z" fill="#fff"/>
            <circle cx="24" cy="22" r="2" fill="#fff" opacity="0.6"/>
            <defs>
              <linearGradient id="gradient" x1="0" y1="0" x2="48" y2="48">
                <stop offset="0%" stop-color="#007AFF"/>
                <stop offset="100%" stop-color="#0055CC"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="brand-name">智课考勤</h1>
        <p class="brand-tagline">课堂考勤管理平台</p>
      </div>

      <!-- 表单区 -->
      <div class="form-section">
        <el-form :model="loginForm" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              size="large"
              show-password
              class="custom-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <div class="footer-row">
            <span class="footer-text">还没有账号？</span>
            <el-button text type="primary" class="register-link" @click="$router.push('/register')">
              立即注册
            </el-button>
          </div>
          <div class="footer-row" style="margin-top: 8px;">
            <el-button text type="primary" size="small" class="forgot-link" @click="$router.push('/forgot-password')">
              忘记密码？
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import ParticleBackground from '@/components/sci-fi/ParticleBackground.vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        ElMessage.success('登录成功')
        const target = res.data.role === 'student' ? '/student/home' : '/dashboard'
        router.push(target)
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(145deg, #f5f5f7 0%, #e8e8ed 30%, #f0f0f5 60%, #fafafc 100%);
  position: relative;
  overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 背景装饰 */
.bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}
.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.12;
}
.shape-1 {
  width: 500px; height: 500px;
  background: #007AFF;
  top: -15%; right: -10%;
  animation: float 20s ease-in-out infinite;
}
.shape-2 {
  width: 400px; height: 400px;
  background: #5856D6;
  bottom: -10%; left: -5%;
  animation: float 25s ease-in-out infinite reverse;
}
.shape-3 {
  width: 200px; height: 200px;
  background: #34C759;
  top: 20%; left: 15%;
  animation: float 15s ease-in-out infinite;
}
.shape-4 {
  width: 150px; height: 150px;
  background: #FF9500;
  bottom: 25%; right: 20%;
  animation: float 18s ease-in-out infinite reverse;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

/* 卡片容器 */
.login-card-wrapper {
  width: 420px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 122, 255, 0.12), 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 48px 40px 40px;
  position: relative;
  z-index: 1;
  animation: cardIn 0.6s ease-out;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 品牌区 */
.brand-section {
  text-align: center;
  margin-bottom: 36px;
}
.brand-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}
.brand-icon svg {
  width: 56px;
  height: 56px;
}
.brand-name {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 6px;
  letter-spacing: 2px;
}
.brand-tagline {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* 表单 */
.login-form {
  margin-bottom: 0;
}
.custom-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: all 0.3s;
}
.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}
.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(74, 144, 217, 0.25) inset;
}
.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #007AFF, #0055CC);
  border: none;
  transition: all 0.3s;
}
.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(74, 144, 217, 0.35);
}
.login-btn:active {
  transform: translateY(0);
}

/* 底部 */
.form-footer {
  text-align: center;
}
.footer-row {
  display: flex;
  justify-content: center;
  align-items: center;
}
.footer-text {
  font-size: 14px;
  color: #909399;
}
.register-link {
  font-weight: 600;
  margin-left: 4px;
}
.forgot-link {
  font-size: 13px;
  color: #909399;
}
</style>
