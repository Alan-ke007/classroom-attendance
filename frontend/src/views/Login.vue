<template>
  <div class="login-container">
    <ParticleBackground mode="network" :color="isDark ? '#0A84FF' : '#007AFF'" />

    <!-- 顶栏主题切换 -->
    <div class="login-topbar">
      <ThemeToggle />
    </div>

    <!-- 居中登录卡片 -->
    <div class="login-center">
      <div class="form-card">
        <div class="brand-top">
          <div class="brand-logo">
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
          <h1 class="brand-title">智课考勤</h1>
          <p class="brand-subtitle">智慧课堂考勤管理平台</p>
        </div>

        <div class="form-header">
          <h2 class="form-title">欢迎登录</h2>
          <p class="form-subtitle">请使用账号密码登录系统</p>
        </div>

        <el-form :model="loginForm" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
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
            <el-button text type="primary" class="footer-link" @click="$router.push('/register')">
              立即注册
            </el-button>
          </div>
          <div class="footer-row" style="margin-top: 8px;">
            <el-button text type="primary" size="small" class="footer-link" @click="$router.push('/forgot-password')">
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
import { useTheme } from '@/composables/useTheme'
import ParticleBackground from '@/components/sci-fi/ParticleBackground.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'

const { isDark } = useTheme()
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

const features = [
  { icon: '🎯', text: 'AI 智能行为分析' },
  { icon: '📊', text: '多维数据可视化' },
  { icon: '🔐', text: '安全高效管理' }
]

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
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--c-bg);
  overflow: hidden;
}

/* 顶栏 */
.login-topbar {
  position: fixed;
  top: 24px;
  right: 28px;
  z-index: 10;
}

/* 居中卡片 */
.login-center {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.form-card {
  width: 100%;
  padding: 44px 40px 40px;
  background: var(--c-glass-bg);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--c-glass-shadow);
}

/* 品牌头部 */
.brand-top {
  text-align: center;
  margin-bottom: 28px;
}

.brand-logo svg {
  width: 56px;
  height: 56px;
  filter: drop-shadow(0 0 16px rgba(0, 122, 255, 0.35));
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 3px;
  margin: 12px 0 4px;
  color: var(--c-text);
}

.brand-subtitle {
  font-size: 13px;
  color: var(--c-text-tertiary);
  margin: 0;
  letter-spacing: 0.5px;
}

/* 表单 */
.form-header {
  text-align: center;
  margin-bottom: 28px;
}

.form-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--c-text);
  margin: 0 0 4px;
}

.form-subtitle {
  font-size: 14px;
  color: var(--c-text-secondary);
  margin: 0;
}

.login-form {
  margin-bottom: 0;
}

.login-form :deep(.el-input__wrapper) {
  background: var(--c-fill-color, var(--c-bg-alt));
  border-radius: 10px;
  box-shadow: 0 0 0 1px var(--c-border) inset;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--c-text-tertiary) inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--c-primary-bg) inset, 0 0 0 1px var(--c-primary) inset;
}

.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  border: none;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-glow);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
}

.footer-row {
  display: flex;
  justify-content: center;
  align-items: center;
}

.footer-text {
  font-size: 14px;
  color: var(--c-text-secondary);
}

.footer-link {
  font-weight: 600;
  margin-left: 4px;
}

/* ========== 移动端 ========== */
@media (max-width: 768px) {
  .login-topbar {
    top: 16px;
    right: 20px;
  }

  .login-center {
    padding: 16px;
  }

  .form-card {
    padding: 32px 24px 28px;
    background: transparent;
    border: none;
    backdrop-filter: none;
    box-shadow: none;
  }

  .brand-logo svg {
    width: 48px;
    height: 48px;
  }

  .brand-title {
    font-size: 20px;
  }

  .form-title {
    font-size: 20px;
  }
}
</style>
