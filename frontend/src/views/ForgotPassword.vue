<template>
  <div class="forgot-container">
    <ParticleBackground mode="network" color="#007AFF" />
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <div class="forgot-card-wrapper">
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
        <p class="brand-tagline">{{ step === 1 ? '验证身份 · 重置密码' : '设置新密码' }}</p>
      </div>

      <div class="form-section">
        <!-- 步骤1: 验证身份 -->
        <el-form v-if="step === 1" :model="verifyForm" :rules="verifyRules" ref="verifyFormRef" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="verifyForm.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="verifyForm.email"
              placeholder="注册邮箱"
              prefix-icon="Message"
              size="large"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="verifying"
              @click="handleVerify"
            >
              {{ verifying ? '验证中...' : '验证身份' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 步骤2: 重置密码 -->
        <el-form v-else :model="resetForm" :rules="resetRules" ref="resetFormRef" class="login-form">
          <el-form-item prop="newPassword">
            <el-input
              v-model="resetForm.newPassword"
              type="password"
              placeholder="新密码（至少6位）"
              prefix-icon="Lock"
              size="large"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="resetForm.confirmPassword"
              type="password"
              placeholder="确认新密码"
              prefix-icon="Lock"
              size="large"
              show-password
              class="custom-input"
              @keyup.enter="handleReset"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="resetting"
              @click="handleReset"
            >
              {{ resetting ? '重置中...' : '重置密码' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <el-button text type="primary" class="back-link" @click="$router.push('/login')">
            返回登录
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { forgotPassword, resetPassword } from '@/api/auth'
import ParticleBackground from '@/components/sci-fi/ParticleBackground.vue'

const router = useRouter()
const step = ref(1)
const verifying = ref(false)
const resetting = ref(false)

const verifyFormRef = ref(null)
const resetFormRef = ref(null)

const verifyForm = reactive({
  username: '',
  email: ''
})

const verifyRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const resetForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPass = (rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPass, trigger: 'blur' }
  ]
}

const handleVerify = async () => {
  if (!verifyFormRef.value) return
  await verifyFormRef.value.validate(async (valid) => {
    if (valid) {
      verifying.value = true
      try {
        await forgotPassword(verifyForm)
        step.value = 2
      } catch (e) {
        console.error(e)
      } finally {
        verifying.value = false
      }
    }
  })
}

const handleReset = async () => {
  if (!resetFormRef.value) return
  await resetFormRef.value.validate(async (valid) => {
    if (valid) {
      resetting.value = true
      try {
        await resetPassword({
          username: verifyForm.username,
          email: verifyForm.email,
          newPassword: resetForm.newPassword
        })
        ElMessage.success('密码重置成功，请登录')
        router.push('/login')
      } catch (e) {
        console.error(e)
      } finally {
        resetting.value = false
      }
    }
  })
}
</script>

<style scoped>
.forgot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(145deg, #f5f5f7 0%, #e8e8ed 30%, #f0f0f5 60%, #fafafc 100%);
  position: relative;
  overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

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
  background: #0055CC;
  bottom: -10%; left: -5%;
  animation: float 25s ease-in-out infinite reverse;
}
.shape-3 {
  width: 200px; height: 200px;
  background: #67C23A;
  top: 20%; left: 15%;
  animation: float 15s ease-in-out infinite;
}
.shape-4 {
  width: 150px; height: 150px;
  background: #E6A23C;
  bottom: 25%; right: 20%;
  animation: float 18s ease-in-out infinite reverse;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

.forgot-card-wrapper {
  max-width: 420px;
  width: 90%;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(74, 144, 217, 0.15), 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 48px 40px 40px;
  position: relative;
  z-index: 1;
  animation: cardIn 0.6s ease-out;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

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
.submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #007AFF, #0055CC);
  border: none;
  transition: all 0.3s;
}
.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(74, 144, 217, 0.35);
}
.submit-btn:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
  margin-top: 12px;
}
.back-link {
  font-weight: 600;
}

/* ========== 移动端 ========== */
@media (max-width: 768px) {
  .forgot-card-wrapper {
    padding: 32px 24px 28px;
  }
  .brand-name {
    font-size: 24px;
  }
}
</style>
