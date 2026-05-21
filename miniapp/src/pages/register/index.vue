<template>
  <view class="page">
    <view class="logo-area">
      <view class="logo-icon">🎓</view>
      <view class="app-name">注册账号</view>
      <view class="app-desc">加入课堂智能考勤系统</view>
    </view>

    <view class="form-card">
      <view class="form-item">
        <view class="input-wrap">
          <text class="input-icon">👤</text>
          <input class="field-input" v-model="form.username" placeholder="用户名" placeholder-style="color:#c0c4cc" />
        </view>
      </view>
      <view class="form-item">
        <view class="input-wrap">
          <text class="input-icon">📛</text>
          <input class="field-input" v-model="form.realName" placeholder="真实姓名" placeholder-style="color:#c0c4cc" />
        </view>
      </view>
      <view class="form-item">
        <view class="input-wrap">
          <text class="input-icon">🔒</text>
          <input class="field-input" v-model="form.password" type="password" placeholder="密码（至少6位）" placeholder-style="color:#c0c4cc" />
        </view>
      </view>
      <view class="form-item">
        <view class="input-wrap">
          <text class="input-icon">🔒</text>
          <input class="field-input" v-model="confirmPassword" type="password" placeholder="确认密码" placeholder-style="color:#c0c4cc" />
        </view>
      </view>
      <view class="form-item">
        <view class="role-row">
          <view class="role-opt" :class="{ active: form.role === 'student' }" @tap="form.role = 'student'">
            <text>👨‍🎓 学生</text>
          </view>
          <view class="role-opt" :class="{ active: form.role === 'teacher' }" @tap="form.role = 'teacher'">
            <text>👨‍🏫 教师</text>
          </view>
        </view>
      </view>

      <view v-if="errorMsg" class="error-msg">{{ errorMsg }}</view>

      <button class="submit-btn" :loading="loading" :disabled="loading" @tap="handleRegister">
        {{ loading ? '注册中...' : '注 册' }}
      </button>

      <view class="form-footer">
        <text class="footer-link" @tap="goLogin">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { register } from '@/api/auth'

const form = ref({
  username: '',
  realName: '',
  password: '',
  role: 'student'
})
const confirmPassword = ref('')
const loading = ref(false)
const errorMsg = ref('')

function goLogin() {
  uni.redirectTo({ url: '/pages/login/index' })
}

async function handleRegister() {
  const f = form.value
  if (!f.username.trim()) { errorMsg.value = '请输入用户名'; return }
  if (!f.realName.trim()) { errorMsg.value = '请输入真实姓名'; return }
  if (!f.password.trim() || f.password.length < 6) { errorMsg.value = '密码至少6位'; return }
  if (f.password !== confirmPassword.value) { errorMsg.value = '两次密码不一致'; return }

  loading.value = true
  errorMsg.value = ''
  try {
    await register({
      username: f.username.trim(),
      realName: f.realName.trim(),
      password: f.password,
      role: f.role
    })
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    setTimeout(() => uni.redirectTo({ url: '/pages/login/index' }), 1200)
  } catch (e) {
    errorMsg.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #4A90D9 0%, #357ABD 40%, #f5f5f5 40%);
  display: flex; flex-direction: column;
  align-items: center; padding-top: 80rpx;
}
.logo-area { text-align: center; margin-bottom: 40rpx; }
.logo-icon { font-size: 60rpx; margin-bottom: 8rpx; }
.app-name { font-size: 38rpx; font-weight: 700; color: #fff; }
.app-desc { font-size: 24rpx; color: rgba(255,255,255,0.8); margin-top: 6rpx; }

.form-card {
  width: 630rpx; background: #fff; border-radius: 24rpx;
  padding: 48rpx 40rpx; box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.08);
}
.form-item { margin-bottom: 20rpx; }
.input-wrap {
  display: flex; align-items: center;
  background: #f8f9fb; border-radius: 16rpx; padding: 0 20rpx;
}
.input-icon { font-size: 32rpx; margin-right: 12rpx; }
.field-input { flex: 1; height: 88rpx; font-size: 28rpx; color: #303133; }

.role-row { display: flex; gap: 16rpx; }
.role-opt {
  flex: 1; text-align: center; padding: 20rpx 0;
  background: #f8f9fb; border-radius: 16rpx; font-size: 28rpx; color: #909399;
  border: 2rpx solid transparent;
}
.role-opt.active { background: #ecf5ff; color: #4A90D9; border-color: #4A90D9; font-weight: 600; }

.error-msg {
  background: #fef0f0; color: #F56C6C; font-size: 26rpx;
  padding: 16rpx 20rpx; border-radius: 12rpx; margin-bottom: 16rpx;
}

.submit-btn {
  width: 100%; height: 88rpx; background: linear-gradient(135deg, #4A90D9, #357ABD);
  color: #fff; font-size: 32rpx; font-weight: 600;
  border-radius: 44rpx; border: none; margin-top: 12rpx;
}
.submit-btn[disabled] { opacity: 0.6; }

.form-footer { text-align: center; margin-top: 28rpx; }
.footer-link { font-size: 26rpx; color: #4A90D9; }
</style>
