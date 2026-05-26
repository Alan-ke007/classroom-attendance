<template>
  <div class="register-container">
    <ParticleBackground mode="network" :color="isDark ? '#0A84FF' : '#007AFF'" />
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="register-card-wrapper">
      <div class="brand-section">
        <h2 class="brand-name">创建账号</h2>
        <p class="brand-tagline">加入智课考勤，开启高效课堂管理</p>
      </div>

      <el-form :model="registerForm" :rules="rules" ref="formRef" label-position="top" class="register-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名" class="custom-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" class="custom-input" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="至少6位密码" show-password class="custom-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入密码" show-password class="custom-input" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="角色">
          <el-radio-group v-model="registerForm.role" class="role-group">
            <el-radio-button value="student">学生</el-radio-button>
            <el-radio-button value="teacher">教师</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="registerForm.email" placeholder="选填" class="custom-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="registerForm.phone" placeholder="选填" class="custom-input" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="registerForm.captchaCode" placeholder="输入验证码" maxlength="4" class="captcha-input custom-input" />
            <img v-if="captchaImage" :src="captchaImage" class="captcha-image" @click="loadCaptcha" />
            <el-button text type="primary" @click="loadCaptcha">换一张</el-button>
          </div>
        </el-form-item>

        <el-form-item style="margin-top: 8px;">
          <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>

        <div class="login-link">
          <span>已有账号？</span>
          <el-button text type="primary" class="to-login" @click="$router.push('/login')">去登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, getCaptcha } from '@/api/auth'
import { useTheme } from '@/composables/useTheme'
import ParticleBackground from '@/components/sci-fi/ParticleBackground.vue'

const { isDark } = useTheme()

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')

const registerForm = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  role: 'student',
  email: '',
  phone: '',
  captchaId: '',
  captchaCode: ''
})

const validateConfirmPass = (rule, value, callback) => {
  if (value !== registerForm.password) callback(new Error('两次输入密码不一致'))
  else callback()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPass, trigger: 'blur' }
  ],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function loadCaptcha() {
  try {
    const res = await getCaptcha()
    registerForm.captchaId = res.data.captchaId
    captchaImage.value = res.data.imageBase64
    registerForm.captchaCode = ''
  } catch (e) { ElMessage.error('加载验证码失败') }
}

async function handleRegister() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await register({
        username: registerForm.username,
        realName: registerForm.realName,
        password: registerForm.password,
        role: registerForm.role,
        email: registerForm.email,
        phone: registerForm.phone,
        captchaId: registerForm.captchaId,
        captchaCode: registerForm.captchaCode
      })
      ElMessage.success('注册成功，请登录')
      setTimeout(() => router.push('/login'), 1500)
    } catch (e) { loadCaptcha() }
    finally { loading.value = false }
  })
}

onMounted(() => loadCaptcha())
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--c-bg);
  position: relative;
  overflow: hidden;
}

.bg-shapes { position: absolute; inset: 0; pointer-events: none; overflow: hidden; }
.shape { position: absolute; border-radius: 50%; opacity: 0.06; }
.shape-1 { width: 400px; height: 400px; background: var(--c-primary); top: -10%; right: -8%; animation: float 22s ease-in-out infinite; }
.shape-2 { width: 300px; height: 300px; background: var(--c-success); bottom: -5%; left: -5%; animation: float 18s ease-in-out infinite reverse; }
.shape-3 { width: 180px; height: 180px; background: var(--c-warning); top: 30%; left: 10%; animation: float 20s ease-in-out infinite; }

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(25px, -25px) scale(1.05); }
  66% { transform: translate(-15px, 15px) scale(0.95); }
}

.register-card-wrapper {
  max-width: 540px;
  width: 90%;
  background: var(--c-glass-bg);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--c-glass-shadow);
  padding: 40px 44px;
  position: relative;
  z-index: 1;
  animation: cardIn 0.6s ease-out;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.brand-section { text-align: center; margin-bottom: 28px; }
.brand-name { font-size: 24px; font-weight: 700; color: var(--c-text); margin: 0 0 6px; letter-spacing: 2px; }
.brand-tagline { font-size: 13px; color: var(--c-text-secondary); margin: 0; }

.register-form { margin-bottom: 0; }

:deep(.el-input__wrapper) {
  background: var(--c-fill-color, var(--c-bg-alt));
  border-radius: 8px;
  box-shadow: 0 0 0 1px var(--c-border) inset;
  transition: all 0.3s;
}
:deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px var(--c-text-tertiary) inset; }
:deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px var(--c-primary-bg) inset, 0 0 0 1px var(--c-primary) inset; }

:deep(.el-radio-button__inner) {
  border-color: var(--c-border);
  color: var(--c-text-secondary);
}

.register-btn {
  width: 100%; height: 46px; border-radius: 10px; font-size: 16px; font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, var(--c-primary), var(--c-primary-dark));
  border: none;
  transition: all 0.3s;
}
.register-btn:hover { transform: translateY(-1px); box-shadow: var(--shadow-glow); }

.role-group { display: flex; width: 100%; }
.captcha-row { display: flex; align-items: center; gap: 10px; width: 100%; }
.captcha-input { flex: 1; }
.captcha-image { width: 120px; height: 40px; border-radius: 8px; border: 1px solid var(--c-border); cursor: pointer; }
.login-link { text-align: center; font-size: 14px; color: var(--c-text-secondary); }
.to-login { font-weight: 600; margin-left: 4px; }

/* ========== 移动端 ========== */
@media (max-width: 768px) {
  .register-card-wrapper {
    padding: 28px 20px 24px;
    background: transparent;
    border: none;
    box-shadow: none;
    backdrop-filter: none;
  }
  .brand-name {
    font-size: 22px;
  }
  :deep(.el-col) {
    flex: 0 0 100%;
    max-width: 100%;
    margin-bottom: 0;
  }
  .captcha-row {
    flex-wrap: wrap;
  }
  .captcha-image {
    width: 100px;
    height: 36px;
  }
}
</style>
