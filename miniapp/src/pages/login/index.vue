<template>
  <view class="login-page">
    <view class="logo-area">
      <view class="logo-icon">🎓</view>
      <view class="app-name">智课考勤</view>
      <view class="app-desc">课堂智能考勤管理系统</view>
    </view>

    <view class="form-card">
      <view class="form-item">
        <view class="input-wrap">
          <text class="input-icon">👤</text>
          <input class="field-input" v-model="username" placeholder="请输入用户名" placeholder-style="color:#c0c4cc" @input="clearError" />
        </view>
      </view>

      <view class="form-item">
        <view class="input-wrap">
          <text class="input-icon">🔒</text>
          <input class="field-input" v-model="password" type="password" placeholder="请输入密码" placeholder-style="color:#c0c4cc" @input="clearError" />
        </view>
      </view>

      <view v-if="errorMsg" class="error-msg">{{ errorMsg }}</view>

      <button class="login-btn" :loading="loading" :disabled="loading" @tap="handleLogin">
        {{ loading ? '登录中...' : '登 录' }}
      </button>

      <view class="form-footer">
        <text class="footer-link" @tap="goRegister">还没有账号？立即注册</text>
      </view>
    </view>

    <view class="demo-hint">
      <text>管理员 admin/admin123 · 学生 202101001/123456</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { login } from '@/api/auth'
import { setToken, setUserInfo } from '@/utils/storage'

const username = ref('admin')
const password = ref('admin123')
const loading = ref(false)
const errorMsg = ref('')

function clearError() { errorMsg.value = '' }

async function handleLogin() {
  const u = username.value.trim()
  const p = password.value.trim()
  if (!u) { errorMsg.value = '请输入用户名'; return }
  if (!p) { errorMsg.value = '请输入密码'; return }
  if (p.length < 6) { errorMsg.value = '密码至少6位'; return }

  loading.value = true
  errorMsg.value = ''
  try {
    const data = await login(u, p)
    setToken(data.token)
    setUserInfo({
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      role: data.role,
      studentId: data.studentId,
      classId: data.classId
    })
    const homeUrl = (data.role === 'teacher' || data.role === 'admin')
      ? '/pages/teacher/home/index'
      : '/pages/student/home/index'
    uni.reLaunch({ url: homeUrl })
  } catch (e) {
    errorMsg.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}

function goRegister() {
  uni.redirectTo({ url: '/pages/register/index' })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #4A90D9 0%, #357ABD 40%, #f5f5f5 40%);
  display: flex; flex-direction: column;
  align-items: center; padding-top: 100rpx;
}
.logo-area { text-align: center; margin-bottom: 40rpx; }
.logo-icon { font-size: 64rpx; margin-bottom: 12rpx; }
.app-name { font-size: 40rpx; font-weight: 700; color: #fff; }
.app-desc { font-size: 24rpx; color: rgba(255,255,255,0.8); margin-top: 8rpx; }

.form-card {
  width: 630rpx; background: #fff; border-radius: 24rpx;
  padding: 48rpx 40rpx; box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.08);
}
.form-item { margin-bottom: 24rpx; }
.input-wrap {
  display: flex; align-items: center;
  background: #f8f9fb; border-radius: 16rpx; padding: 0 20rpx;
}
.field-input { flex: 1; height: 90rpx; font-size: 30rpx; color: #303133; }

.error-msg {
  background: #fef0f0; color: #F56C6C; font-size: 26rpx;
  padding: 16rpx 20rpx; border-radius: 12rpx; margin-bottom: 16rpx;
}

.login-btn {
  width: 100%; height: 90rpx; background: linear-gradient(135deg, #4A90D9, #357ABD);
  color: #fff; font-size: 32rpx; font-weight: 600;
  border-radius: 45rpx; border: none; margin-top: 16rpx;
}
.login-btn[disabled] { opacity: 0.6; }

.form-footer { text-align: center; margin-top: 28rpx; }
.footer-link { font-size: 26rpx; color: #4A90D9; }

.demo-hint { margin-top: 40rpx; }
.demo-hint text { font-size: 24rpx; color: rgba(255,255,255,0.7); }
</style>
